package ru.fivegst.speedtest.task.impl

import ru.fivegst.speedtest.backend.IperfException
import ru.fivegst.speedtest.backend.IperfRunner
import ru.fivegst.speedtest.task.FatalException
import ru.fivegst.speedtest.task.Task
import ru.fivegst.speedtest.task.impl.model.ApiClientHolder
import ru.fivegst.speedtest.util.Equalizer
import ru.fivegst.speedtest.util.IdleTaskKiller
import ru.fivegst.speedtest.util.Promise
import ru.fivegst.speedtest.util.SkipThenAverageEqualizer
import ru.fivegst.speedtest.util.TaskKiller
import java.util.LongSummaryStatistics
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class StartUdpUploadIperfTask(
    private val writableDir: String,
    private val args: String,
    private val speedEqualizer: SkipThenAverageEqualizer,
    private val idleTimeoutMillis: Long,
    private val onSpeedUpdate: (LongSummaryStatistics, Long) -> Unit,
    private val onLog: (String, String, Exception?) -> Unit,
) : Task<ApiClientHolder, ApiClientHolder> {
    private val speedStatistics: LongSummaryStatistics = LongSummaryStatistics()
    private val lock = ReentrantLock()

    override fun prepare(
        argument: ApiClientHolder,
        killer: TaskKiller
    ): Promise<(ApiClientHolder) -> Unit, (String, Exception?) -> Unit> = Promise { onSuccess, _ ->
        onLog(LOG_TAG, "Preparing udp upload iperf task", null)

        val idleTaskKiller = IdleTaskKiller()
        val measurementPinger = IperfMeasurementPinger(argument, onLog) { results ->
            lock.withLock {
                for (probeResult in results) {
                    if (speedEqualizer.accept(probeResult.toLong())) {
                        speedStatistics.accept(probeResult.toLong())
                    }
                }
                val equalizedSpeed = try {
                    speedEqualizer.getEqualized()
                } catch (e: Equalizer.NoValueException) {
                    return@IperfMeasurementPinger
                }
                onSpeedUpdate(speedStatistics, equalizedSpeed.toLong())
            }
        }
        val thread = Thread(measurementPinger)
        val processor = IperfOutputProcessor(idleTaskKiller, thread) {
            onSuccess?.invoke(argument)
        }

        val iperfRunner = IperfRunner.Builder(writableDir)
            .stdoutLinesHandler(processor::onIperfStdoutLine)
            .stderrLinesHandler(processor::onIperfStderrLine)
            .onFinishCallback(processor::onIperfFinish)
            .build()


        thread.start()

        while (true) {
            try {

                // TODO validate not to have -c and -p in command
                iperfRunner.start(
                    "-c ${argument.iperfAddress.host} -p ${argument.iperfAddress.port} $args"
                )

                val task = {
                    try {
                        iperfRunner.sendSigKill()
                    } catch (e: IperfException) {
                        onLog(LOG_TAG, "Could not stop iPerf", e)
                    }
                }
                idleTaskKiller.registerBlocking(idleTimeoutMillis, task)
                killer.register(task)
                break
            } catch (e: InterruptedException) {
                onLog(LOG_TAG, "Interrupted iPerf start. Ignoring...", e)
            } catch (e: IperfException) {
                throw FatalException("Could not start iPerf", e)
            }
        }
    }

    private inner class IperfOutputProcessor(
        private val idleTaskKiller: IdleTaskKiller,
        private val thread: Thread,
        private val onFinish: () -> Unit,
    ) {

        fun onIperfStdoutLine(line: String) {
            onLog("iPerf stdout", line, null)
            idleTaskKiller.updateTaskState()
        }

        fun onIperfStderrLine(line: String) {
            onLog("iPerf stderr", line, null)
            idleTaskKiller.updateTaskState()
        }

        fun onIperfFinish() {
            thread.interrupt()
            onFinish()
        }
    }

    companion object {
        const val LOG_TAG = "StartUdpUploadIperfTask"
    }
}