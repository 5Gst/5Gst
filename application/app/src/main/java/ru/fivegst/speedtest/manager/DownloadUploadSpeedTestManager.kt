package ru.fivegst.speedtest.manager

import android.content.Context
import com.squareup.okhttp.HttpUrl
import ru.fivegst.speedtest.R
import ru.fivegst.speedtest.domain.StageConfiguration
import ru.fivegst.speedtest.parser.MultithreadedIperfOutputParser
import ru.fivegst.speedtest.task.TaskChain
import ru.fivegst.speedtest.task.TaskChainBuilder
import ru.fivegst.speedtest.task.TaskConsumer
import ru.fivegst.speedtest.task.impl.BalancerApi
import ru.fivegst.speedtest.task.impl.BalancerApiBuilder
import ru.fivegst.speedtest.task.impl.DelayTask
import ru.fivegst.speedtest.task.impl.FiveGstLoginTask
import ru.fivegst.speedtest.task.impl.FiveGstLogoutTask
import ru.fivegst.speedtest.task.impl.GetMeasurementResultsTask
import ru.fivegst.speedtest.task.impl.ObtainServiceAddressTask
import ru.fivegst.speedtest.task.impl.ParseAddressTask
import ru.fivegst.speedtest.task.impl.PingAddressTask
import ru.fivegst.speedtest.task.impl.StartIperfTask
import ru.fivegst.speedtest.task.impl.StartServiceIperfTask
import ru.fivegst.speedtest.task.impl.StartServiceSessionTask
import ru.fivegst.speedtest.task.impl.StartUdpUploadIperfTask
import ru.fivegst.speedtest.task.impl.StopServiceIperfTask
import ru.fivegst.speedtest.task.impl.StopServiceSessionTask
import ru.fivegst.speedtest.task.impl.model.ApiClientHolder
import ru.fivegst.speedtest.task.withArgumentExtracted
import ru.fivegst.speedtest.task.withArgumentKeptDoUnstoppableTask
import ru.fivegst.speedtest.util.SkipThenAverageEqualizer
import java.util.LongSummaryStatistics
import java.util.concurrent.locks.ReentrantLock
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.LongConsumer
import kotlin.concurrent.withLock

class DownloadUploadSpeedTestManager
private constructor(
    private val context: Context,
    private val onPingUpdate: (Long) -> Unit,
    private val onStageStart: (StageConfiguration) -> Unit,
    private val onStageSpeedUpdate: (LongSummaryStatistics, Long) -> Unit,
    private val onStageFinish: (StageConfiguration, LongSummaryStatistics) -> Unit,
    private val onFinish: () -> Unit,
    private val onStop: () -> Unit,
    private val onLog: (String, String, Exception?) -> Unit,
    private val onFatalError: (String, Exception?) -> Unit,
) {
    private val lock = ReentrantLock()
    private var taskChain: TaskChain<*>? = null

    fun start(
        useBalancer: Boolean,
        mainAddress: String,
        idleBetweenTasksMelees: Long,
        stageConfigurations: List<StageConfiguration>,
    ) {
        val localTaskChain = if (useBalancer) {
            buildChainUsingBalancer(idleBetweenTasksMelees, stageConfigurations)
        } else {
            buildDirectIperfChain()
        }

        lock.withLock {
            taskChain = localTaskChain.apply { start(mainAddress) }
        }
    }

    fun buildChainUsingBalancer(
        idleBetweenTasksMelees: Long,
        stageConfigurations: List<StageConfiguration>,
    ): TaskChain<String> {
        val chainBuilder = TaskChainBuilder<String>().onFatalError(onFatalError).onStop(onStop)
        val taskConsumer = chainBuilder.initializeNewChain()
            .andThen(ParseAddressTask())
            .andThenUnstoppable { balancerAddress ->
                val balancerBasePath = HttpUrl.Builder()
                    .scheme("http")  // TODO https
                    .host(balancerAddress.host)
                    .port(balancerAddress.port)
                    .build()
                    .toString()
                    .dropLast(1)  // drops trailing '/'

                BalancerApiBuilder()
                    .setConnectTimeout(DEFAULT_TIMEOUT)
                    .setReadTimeout(DEFAULT_TIMEOUT)
                    .setWriteTimeout(DEFAULT_TIMEOUT)
                    .setBasePath(balancerBasePath)
                    .let { BalancerApi(it) }
            }
            .andThenTry(FiveGstLoginTask()) {
                val obtainServiceAddressTask = ObtainServiceAddressTask(
                    DEFAULT_TIMEOUT,
                    DEFAULT_TIMEOUT,
                    DEFAULT_TIMEOUT,
                )

                initializeNewChain()
                    .andThen(obtainServiceAddressTask)
                    .andThenTry {
                        initializeNewChain()
                            .andThen(StartServiceSessionTask())
                            .let {
                                addStagesToChain(it, idleBetweenTasksMelees, stageConfigurations)
                            }
                    }
                    .andThenFinally(StopServiceSessionTask())
            }
            .andThenFinally(FiveGstLogoutTask())

        return chainBuilder.finishChainCreation(taskConsumer) {
            onFinish()
        }
    }

    private fun addStagesToChain(
        taskConsumer: TaskConsumer<ApiClientHolder>,
        idleBetweenTasksMelees: Long,
        stageConfigurations: List<StageConfiguration>,
    ): TaskConsumer<ApiClientHolder> {
        val immutableDeviceArgsPrefix = context.getString(R.string.immutable_device_args)
        val immutableServerArgsPrefix = context.getString(R.string.immutable_server_args)

        var mutableTaskConsumer = taskConsumer
        stageConfigurations.forEach { stageConfiguration ->
            if (stageConfiguration == StageConfiguration.EMPTY) {
                return@forEach
            }

            val startServiceIperfTask = StartServiceIperfTask(
                "$immutableServerArgsPrefix ${stageConfiguration.serverArgs}",
            )
            val stopServiceIperfTask = StopServiceIperfTask()

            // TODO parse arguments with some library without self-written regexes
            val args = "$immutableDeviceArgsPrefix ${stageConfiguration.deviceArgs}"
            if (args.contains(UDP_UPLOAD_ARGS_CONTAINS_REGEX) &&
                !args.contains(UDP_UPLOAD_ARGS_NOT_CONTAINS_REGEX)
            ) {
                mutableTaskConsumer = addRemoteMeasurementStagesToChain(
                    mutableTaskConsumer,
                    immutableDeviceArgsPrefix,
                    stageConfiguration,
                    startServiceIperfTask,
                    stopServiceIperfTask,
                    idleBetweenTasksMelees
                )
            } else {
                mutableTaskConsumer = addLocalMeasurementStagesToChain(
                    mutableTaskConsumer,
                    immutableDeviceArgsPrefix,
                    stageConfiguration,
                    startServiceIperfTask,
                    stopServiceIperfTask,
                    idleBetweenTasksMelees
                )
            }
        }
        return mutableTaskConsumer
    }

    private fun addRemoteMeasurementStagesToChain(
        mutableTaskConsumer: TaskConsumer<ApiClientHolder>,
        immutableDeviceArgsPrefix: String,
        stageConfiguration: StageConfiguration,
        startServiceIperfTask: StartServiceIperfTask,
        stopServiceIperfTask: StopServiceIperfTask,
        idleBetweenTasksMelees: Long,
    ): TaskConsumer<ApiClientHolder> {
        val startUdpUploadIperfTask = StartUdpUploadIperfTask(
            context.filesDir.absolutePath,
            "$immutableDeviceArgsPrefix ${stageConfiguration.deviceArgs}",
            SkipThenAverageEqualizer(
                DEFAULT_EQUALIZER_DOWNLOAD_VALUES_SKIP,
                DEFAULT_EQUALIZER_MAX_STORING
            ),
            DEFAULT_TIMEOUT.toLong(),
            onStageSpeedUpdate,
            onLog,
        )
        return mutableTaskConsumer
            .withArgumentExtracted { it.iperfAddress }
            .doTask(PingAddressTask(DEFAULT_TIMEOUT.toLong(), onPingUpdate))
            .andThenTry {
                initializeNewChain()
                    .andThen(startServiceIperfTask)
                    .withArgumentKeptDoUnstoppableTask { onStageStart(stageConfiguration) }
                    .andThen(startUdpUploadIperfTask)
            }
            .andThenFinally(stopServiceIperfTask)
            .andThen(
                GetMeasurementResultsTask { onStageFinish(stageConfiguration, it) }
            )
            .andThen(DelayTask(idleBetweenTasksMelees))
    }

    private fun addLocalMeasurementStagesToChain(
        mutableTaskConsumer: TaskConsumer<ApiClientHolder>,
        immutableDeviceArgsPrefix: String,
        stageConfiguration: StageConfiguration,
        startServiceIperfTask: StartServiceIperfTask,
        stopServiceIperfTask: StopServiceIperfTask,
        idleBetweenTasksMelees: Long,
    ): TaskConsumer<ApiClientHolder> {
        val startIperfTask = StartIperfTask(
            context.filesDir.absolutePath,
            "$immutableDeviceArgsPrefix ${stageConfiguration.deviceArgs}",
            MultithreadedIperfOutputParser(),
            SkipThenAverageEqualizer(
                DEFAULT_EQUALIZER_DOWNLOAD_VALUES_SKIP,
                DEFAULT_EQUALIZER_MAX_STORING
            ),
            DEFAULT_TIMEOUT.toLong(),
            onStageSpeedUpdate,
            { onStageFinish(stageConfiguration, it) },
            onLog,
        )
        return mutableTaskConsumer
            .withArgumentExtracted { it.iperfAddress }
            .doTask(PingAddressTask(DEFAULT_TIMEOUT.toLong(), onPingUpdate))
            .andThenTry {
                initializeNewChain()
                    .andThen(startServiceIperfTask)
                    .withArgumentKeptDoUnstoppableTask { onStageStart(stageConfiguration) }
                    .withArgumentExtracted { it.iperfAddress }
                    .doTask(startIperfTask)
            }
            .andThenFinally(stopServiceIperfTask)
            .andThen(DelayTask(idleBetweenTasksMelees))
    }

    fun buildDirectIperfChain(): TaskChain<String> {
        val deviceArgs = context.getString(R.string.immutable_device_args) +
                " " +
                context.getString(R.string.download_device_iperf_args)

        val stageConfiguration = StageConfiguration("Direct iperf stage", "?", deviceArgs)
        val chainBuilder = TaskChainBuilder<String>().onFatalError(onFatalError).onStop(onStop)
        val taskConsumer = chainBuilder.initializeNewChain()
            .andThen(ParseAddressTask())
            .andThen(PingAddressTask(DEFAULT_TIMEOUT.toLong(), onPingUpdate))
            .andThenUnstoppable {
                onStageStart(stageConfiguration)
                it
            }
            .andThen(
                StartIperfTask(
                    context.filesDir.absolutePath,
                    deviceArgs,
                    MultithreadedIperfOutputParser(),
                    SkipThenAverageEqualizer(
                        DEFAULT_EQUALIZER_DOWNLOAD_VALUES_SKIP,
                        DEFAULT_EQUALIZER_MAX_STORING
                    ),
                    DEFAULT_TIMEOUT.toLong(),
                    onStageSpeedUpdate,
                    { onStageFinish(stageConfiguration, it) },
                    onLog,
                )
            )
        return chainBuilder.finishChainCreation(taskConsumer) {
            onFinish()
        }
    }

    fun stop() {
        lock.withLock {
            taskChain?.stop()
        }
    }

    class Builder(private val context: Context) {
        private var onPingUpdate: LongConsumer = LongConsumer {}
        private var onStageStart: Consumer<StageConfiguration> = Consumer {}
        private var onStageSpeedUpdate: BiConsumer<LongSummaryStatistics, Long> =
            BiConsumer { _, _ -> }
        private var onStageFinish: BiConsumer<StageConfiguration, LongSummaryStatistics> =
            BiConsumer { _, _ -> }
        private var onFinish: Runnable = Runnable {}
        private var onStop: Runnable = Runnable {}
        private var onLog: (String, String, Exception?) -> Unit = { _, _, _ -> }
        private var onFatalError: BiConsumer<String, Exception?> = BiConsumer { _, _ -> }

        fun build(): DownloadUploadSpeedTestManager {
            return DownloadUploadSpeedTestManager(
                context,
                onPingUpdate::accept,
                onStageStart::accept,
                onStageSpeedUpdate::accept,
                onStageFinish::accept,
                onFinish::run,
                onStop::run,
                onLog::invoke,
                onFatalError::accept,
            )
        }

        fun onPingUpdate(onPingUpdate: LongConsumer): Builder {
            this.onPingUpdate = onPingUpdate
            return this
        }

        fun onStageStart(onStageStart: Consumer<StageConfiguration>): Builder {
            this.onStageStart = onStageStart
            return this
        }

        fun onStageSpeedUpdate(onStageSpeedUpdate: BiConsumer<LongSummaryStatistics, Long>): Builder {
            this.onStageSpeedUpdate = onStageSpeedUpdate
            return this
        }

        fun onStageFinish(onStageFinish: BiConsumer<StageConfiguration, LongSummaryStatistics>): Builder {
            this.onStageFinish = onStageFinish
            return this
        }

        fun onFinish(onFinish: Runnable): Builder {
            this.onFinish = onFinish
            return this
        }

        fun onStop(onStop: Runnable): Builder {
            this.onStop = onStop
            return this
        }

        fun onLog(onLog: (String, String, Exception?) -> Unit): Builder {
            this.onLog = onLog
            return this
        }

        fun onFatalError(onFatalError: BiConsumer<String, Exception?>): Builder {
            this.onFatalError = onFatalError
            return this
        }
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 5_000
        private const val DEFAULT_EQUALIZER_MAX_STORING = 4
        private const val DEFAULT_EQUALIZER_DOWNLOAD_VALUES_SKIP = 0

        private val UDP_UPLOAD_ARGS_CONTAINS_REGEX = Regex("(^|[^-])(-u|--udp)(\$|\\s)")
        private val UDP_UPLOAD_ARGS_NOT_CONTAINS_REGEX = Regex("(^|[^-])(-R|--reverse)(\$|\\s)")
    }
}
