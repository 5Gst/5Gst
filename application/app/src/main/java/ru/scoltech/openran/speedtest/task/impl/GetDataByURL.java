package ru.scoltech.openran.speedtest.task.impl;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import kotlin.Unit;
import ru.scoltech.openran.speedtest.client.service.ApiException;
import ru.scoltech.openran.speedtest.task.impl.model.ApiClientHolder;

public class GetDataByURL implements Runnable {

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ApiClientHolder apiClientHolder;
    private AtomicInteger fromFrame = new AtomicInteger(0);
    private List<Integer> results = Collections.synchronizedList(new ArrayList<Integer>());

    public GetDataByURL(ApiClientHolder apiClientHolder) {
        this.apiClientHolder = apiClientHolder;
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                getData();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e("GetDataByURL - Exception", "thread interrupted while running");
            }
        }
    }

    public void getData() {
        try {
            results = Collections.synchronizedList(apiClientHolder.getServiceApiClient().iperfSpeedResults(fromFrame.toString()).getResults());
            fromFrame.set(fromFrame.get() + results.size());
        } catch (ApiException e) {
            Log.e("GetDataByURL - Exception","ApiException " +e.toString());
        }

    }

    public void stop() {
        running.set(false);
    }

    public List<Integer> getResults() {
        return results;
    }
}