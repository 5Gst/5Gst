package ru.scoltech.openran.speedtest.task.impl

import android.location.GnssMeasurement
import android.util.Log
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

class StartUdpUploadIperfTask (
    private val writableDir: String,
    private val args: String,
    private val speedParser: IperfOutputParser,
    private val speedEqualizer: SkipThenAverageEqualizer,
    private val idleTimeoutMillis: Long,
    private val onSpeedUpdate: (LongSummaryStatistics, Long) -> Unit,
    private val onLog: (String, String, Exception?) -> Unit,
) : Task<ApiClientHolder, ApiClientHolder> {


    private val speedStatistics = LongSummaryStatistics()

    override fun prepare(
        argument: ApiClientHolder,
        killer: TaskKiller
    ): Promise<(ApiClientHolder) -> Unit, (String, Exception?) -> Unit> = Promise { onSuccess, _ ->
        val idleTaskKiller = IdleTaskKiller()
        val api = GetDataByURL(argument)
        val thread = Thread(api)
        val processor = IperfOutputProcessor(idleTaskKiller, speedEqualizer.copy(), api, thread) {
            onSuccess?.invoke(argument)
        }

        val iperfRunner = IperfRunner.Builder(writableDir)
            .stdoutLinesHandler(processor::onIperfStdoutLine)
            .stderrLinesHandler(processor::onIperfStderrLine)
            .onFinishCallback(processor::onIperfFinish)
            .build()

        Log.i("started","started")

        while (true) {
            try {

                thread.start()

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

    public fun getStatistics() : LongSummaryStatistics{
        return speedStatistics
    }

    private inner class IperfOutputProcessor(
        private val idleTaskKiller: IdleTaskKiller,
        private val speedEqualizer: SkipThenAverageEqualizer,
        private val measurementResults: GetDataByURL,
        private val thread: Thread,
        private val onFinish: () -> Unit,
    ) {
        private val lock = ReentrantLock()

        fun onIperfStdoutLine(line: String) {
            idleTaskKiller.updateTaskState()
            val speed = try {
                speedParser.parseSpeed(line)
            } catch (e: IOException) {
                onLog("Speed parser", "Invalid stdout format", e)
                return
            }

            lock.withLock {
                val results = measurementResults.results
                if (speedEqualizer.accept(speed)) {
                    for (i in 0 until measurementResults.results.size){
                        if (speedEqualizer.accept(results[i].toLong())) {
                            speedStatistics.accept(results[i].toLong())
                            onSpeedUpdate(speedStatistics, results[i].toLong())
                        }
                    }
                }
            }
        }

        fun onIperfStderrLine(line: String) {
            onLog("iPerf stderr", line, null)
            idleTaskKiller.updateTaskState()
        }

        fun onIperfFinish() {
            measurementResults.stop()
            thread.interrupt()
            onFinish()
        }
    }


    companion object {
        const val LOG_TAG = "StartIperfTask"
    }
}