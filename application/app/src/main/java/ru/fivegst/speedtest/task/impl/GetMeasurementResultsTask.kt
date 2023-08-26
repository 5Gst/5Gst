package ru.fivegst.speedtest.task.impl

import com.squareup.okhttp.Call
import ru.fivegst.speedtest.client.service.ApiCallback
import ru.fivegst.speedtest.client.service.model.IperfMeasurement
import ru.fivegst.speedtest.task.impl.model.ApiClientHolder
import java.util.LongSummaryStatistics

class GetMeasurementResultsTask(
    private val onFinish: (LongSummaryStatistics) -> Unit,
) : AbstractServiceRequestTask<ApiClientHolder, IperfMeasurement, ApiClientHolder>() {
    override fun sendRequest(
        argument: ApiClientHolder,
        callback: ApiCallback<IperfMeasurement>
    ): Call {
        return argument.serviceApiClient.getIperfSpeedProbesAsync(0, callback)
    }

    override fun processApiResult(
        argument: ApiClientHolder, apiResult: IperfMeasurement
    ): ApiClientHolder {
        val speedStatistics = LongSummaryStatistics()
        for (i in 0 until apiResult.probes.size) {
            speedStatistics.accept(apiResult.probes[i].bitsPerSecond)
        }
        onFinish(speedStatistics)
        return argument
    }


}