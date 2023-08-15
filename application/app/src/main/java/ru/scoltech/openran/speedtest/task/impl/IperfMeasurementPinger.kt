package ru.scoltech.openran.speedtest.task.impl

import ru.scoltech.openran.speedtest.client.service.ApiException
import ru.scoltech.openran.speedtest.task.impl.model.ApiClientHolder
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

class IperfMeasurementPinger(
    private val apiClientHolder: ApiClientHolder,
    private val onLog: (String, String, Exception?) -> Unit,
    private val saveMeasurement: (List<Int>) -> Unit
) : Runnable {

    private var waiting = AtomicBoolean(true)
    private val fromFrame = AtomicInteger(0)
    override fun run() {
        while (true) {
            try {
                val results =  apiClientHolder.serviceApiClient.getIperfSpeedProbes(fromFrame.get()).probes
                fromFrame.set(fromFrame.get() + results.size)
                waiting.set(false)
                saveMeasurement(results.stream().map { el -> el.bitsPerSecond }.collect(Collectors.toList()))
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                onLog(LOG_TAG, "thread interrupted while running $e", e)
                return
            } catch (e: ApiException){
                onLog(LOG_TAG, "ApiException (mb caused by interrupting thread) $e", e)
                return
            }
        }
    }

    public fun isConnecting() : Boolean{
        return waiting.get()
    }

    companion object {
        const val LOG_TAG = "IperfMeasurementPinger"
    }

}