package ru.scoltech.openran.speedtest.task.impl

import android.location.GnssMeasurement
import android.util.Log
import org.apache.commons.collections.list.SynchronizedList
import ru.scoltech.openran.speedtest.backend.IperfException
import ru.scoltech.openran.speedtest.backend.IperfRunner
import ru.scoltech.openran.speedtest.parser.IperfOutputParser
import ru.scoltech.openran.speedtest.task.FatalException
import ru.scoltech.openran.speedtest.task.Task
import ru.scoltech.openran.speedtest.task.impl.model.ApiClientHolder
import ru.scoltech.openran.speedtest.task.impl.model.ServerAddress
import ru.scoltech.openran.speedtest.util.Equalizer
import ru.scoltech.openran.speedtest.util.IdleTaskKiller
import ru.scoltech.openran.speedtest.util.Promise
import ru.scoltech.openran.speedtest.util.SkipThenAverageEqualizer
import ru.scoltech.openran.speedtest.util.TaskKiller
import java.io.IOException
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
    private val onConnectionWait: (Boolean) -> Unit,
) : Task<ApiClientHolder, ApiClientHolder> {
    private val speedStatistics: LongSummaryStatistics = LongSummaryStatistics()
    private val lock = ReentrantLock()
    override fun prepare(
        argument: ApiClientHolder,
        killer: TaskKiller
    ): Promise<(ApiClientHolder) -> Unit, (String, Exception?) -> Unit> = Promise { onSuccess, _ ->
        val idleTaskKiller = IdleTaskKiller()
        val measurementPinger = IperfMeasurementPinger(argument, onLog, onConnectionWait) { data ->
            lock.withLock {
                for (el in data) {
                    if (speedEqualizer.accept(el.toLong())) {
                        speedStatistics.accept(el.toLong())
                        val equalizedSpeed = try {
                            speedEqualizer.getEqualized()
                        } catch (e: Equalizer.NoValueException) {
                            onLog(LOG_TAG, "Equalizer $e", e)
                            return@IperfMeasurementPinger
                        }
                        onSpeedUpdate(speedStatistics, equalizedSpeed.toLong())
                    }
                }
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


        while (true) {
            try {

                thread.start()
                onConnectionWait(true)
                Thread.sleep(1000)

                // TODO validate not to have -c and -p in command
                iperfRunner.start(
                    "-c ${argument.iperfAddress.host} -p ${argument.iperfAddress.port} $args"
                )

                val task = {
                    try {
                        iperfRunner.sendSigKill()
                        thread.interrupt()
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
            idleTaskKiller.updateTaskState()
            onLog("iPerf stdout", line, null)
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