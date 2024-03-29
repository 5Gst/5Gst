package ru.fivegst.speedtest.task.impl

import ru.fivegst.speedtest.client.service.ApiException
import ru.fivegst.speedtest.task.impl.model.ApiClientHolder
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

class IperfMeasurementPinger(
    private val apiClientHolder: ApiClientHolder,
    private val onLog: (String, String, Exception?) -> Unit,
    private val saveMeasurement: (List<Int>) -> Unit
) : Runnable {

    private val fromFrame = AtomicInteger(0)
    private val delayBetweenPing: Long = 100
    override fun run() {
        while (!Thread.currentThread().isInterrupted) {
            try {
                val results =  apiClientHolder.serviceApiClient.getIperfSpeedProbes(fromFrame.get()).probes
                fromFrame.set(fromFrame.get() + results.size)
                if (results.size!=0) {
                    saveMeasurement(results.stream().map { el -> el.bitsPerSecond }.collect(Collectors.toList()))
                }
                Thread.sleep(delayBetweenPing)
            } catch (e: InterruptedException) {
                onLog(LOG_TAG, "thread interrupted while running", e)
                return
            } catch (e: ApiException){
                onLog(LOG_TAG, "ApiException (mb caused by interrupting thread)", e)
                return
            }
        }
    }
    companion object {
        const val LOG_TAG = "IperfMeasurementPinger"
    }

}