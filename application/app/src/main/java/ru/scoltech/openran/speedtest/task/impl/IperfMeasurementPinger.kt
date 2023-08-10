package ru.scoltech.openran.speedtest.task.impl

import ru.scoltech.openran.speedtest.client.service.ApiException
import ru.scoltech.openran.speedtest.task.impl.model.ApiClientHolder
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

class IperfMeasurementPinger(
    private val apiClientHolder: ApiClientHolder,
    private val onLog: (String, String, Exception?) -> Unit,
    private val process: (List<Int>) -> Unit
) : Runnable {

    private val fromFrame = AtomicInteger(0)
    private var results = Collections.synchronizedList(ArrayList<Int>())

    override fun run() {
        while (true) {
            try {
                results = Collections.synchronizedList(
                    apiClientHolder.serviceApiClient.iperfSpeedResults(fromFrame.toString()).results
                )
                fromFrame.set(fromFrame.get() + results.size)
                process(Collections.synchronizedList(results))
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                onLog(LOG_TAG, "thread interrupted while running $e", e)
                return
            } catch (e: ApiException){
                onLog(LOG_TAG, "ApiException (mb caused by interrupting thread) $e", e)
                return
            }
        }
    }

    companion object {
        const val LOG_TAG = "GetDataByURL"
    }

}