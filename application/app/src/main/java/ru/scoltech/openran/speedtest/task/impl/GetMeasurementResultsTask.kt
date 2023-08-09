package ru.scoltech.openran.speedtest.task.impl

import android.util.Log
import com.squareup.okhttp.Call
import ru.scoltech.openran.speedtest.client.service.ApiCallback
import ru.scoltech.openran.speedtest.client.service.model.IperfSpeedResults
import ru.scoltech.openran.speedtest.task.impl.model.ApiClientHolder
import java.util.LongSummaryStatistics

class GetMeasurementResultsTask (
    private val speedStatistics: LongSummaryStatistics,
    private val onFinish: (LongSummaryStatistics) -> Unit,
) : AbstractServiceRequestTask<ApiClientHolder, IperfSpeedResults, ApiClientHolder>() {
    override fun sendRequest(
        argument: ApiClientHolder,
        callback: ApiCallback<IperfSpeedResults>
    ): Call {
        return argument.serviceApiClient.iperfSpeedResultsAsync(speedStatistics.count.toString(), callback)
    }

    override fun processApiResult(
        argument: ApiClientHolder, apiResult: IperfSpeedResults
    ): ApiClientHolder {
        for (i in 0 until apiResult.results.size)
            speedStatistics.accept(apiResult.results[i].toLong())
        onFinish(speedStatistics)
        return argument
    }


}