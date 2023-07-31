package ru.scoltech.openran.speedtest.activities;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import kotlin.Unit;
import kotlin.collections.SetsKt;
import ru.scoltech.openran.speedtest.ApplicationConstants;
import ru.scoltech.openran.speedtest.R;
import ru.scoltech.openran.speedtest.SpeedManager;
import ru.scoltech.openran.speedtest.Wave;
import ru.scoltech.openran.speedtest.customButtons.ActionButton;
import ru.scoltech.openran.speedtest.customButtons.SaveButton;
import ru.scoltech.openran.speedtest.customButtons.ShareButton;
import ru.scoltech.openran.speedtest.customViews.CardView;
import ru.scoltech.openran.speedtest.customViews.HeaderView;
import ru.scoltech.openran.speedtest.customViews.ResultView;
import ru.scoltech.openran.speedtest.customViews.SubResultView;
import ru.scoltech.openran.speedtest.domain.SpeedTestResult;
import ru.scoltech.openran.speedtest.manager.DownloadUploadSpeedTestManager;
import ru.scoltech.openran.speedtest.parser.StageConfigurationParser;


public class SpeedActivity extends AppCompatActivity {

    private Wave cWave;
    private CardView mCard;
    private SubResultView mSubResults; // in progress result
    private HeaderView mHeader;
    private ResultView mResults; // after finishing

    //action elem
    private ActionButton actionBtn;
    private ShareButton shareBtn;
    private SaveButton saveBtn;

    private SpeedManager sm;
    private DownloadUploadSpeedTestManager speedTestManager;
    private final StageConfigurationParser stageConfigurationParser = new StageConfigurationParser();

    private final static String LOG_TAG = SpeedActivity.class.getSimpleName();
    private final static int TASK_DELAY = 2500;
    private final static int SUGGEST_STORAGE_LIMIT = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_speed);

        sm = SpeedManager.getInstance();

        init();
    }

    private void init() {
        mHeader = findViewById(R.id.header);

        actionBtn = findViewById(R.id.action_btn);

        mCard = findViewById(R.id.card);
        cWave = mCard.getWave();

        mSubResults = findViewById(R.id.subresult);
        mResults = findViewById(R.id.result);

        shareBtn = findViewById(R.id.share_btn);
        saveBtn = findViewById(R.id.save_btn);


        speedTestManager = new DownloadUploadSpeedTestManager.Builder(this)
                .onPingUpdate((ping) -> runOnUiThread(() -> mCard.setPing((int) ping)))
                .onStageStart((stageConfiguration) -> runOnUiThread(() -> {
                    mCard.setInstantSpeed(0, 0);
                    mSubResults.addNewStage(stageConfiguration.getName());

                    cWave.start();
                    cWave.attachSpeed(0);
                }))
                .onStageSpeedUpdate((statistics, speedBitsPS) -> runOnUiThread(() -> {
                    Pair<Integer, Integer> instSpeed = sm.getSpeedWithPrecision(speedBitsPS.intValue(), 2);
                    mCard.setInstantSpeed(instSpeed.first, instSpeed.second);

                    //animation
                    cWave.attachSpeed(instSpeed.first);
                }))
                .onStageFinish((stageConfiguration, statistics) -> runOnUiThread(() -> {
                    final String speedString = getSpeedString(sm.getAverageSpeed(statistics));
                    mSubResults.setCurrentStageSpeed(speedString);
                    sm.addStageResult(stageConfiguration, speedString);
                    cWave.stop();
                }))
                .onFinish(() -> runOnUiThread(() -> {
                    actionBtn.setPlay();

                    final SpeedTestResult speedTestResult = sm.flushResults();
                    String ping = mCard.getPing();
                    onResultUI(speedTestResult, ping);
                }))
                .onStop(() -> runOnUiThread(() -> {
                    onStopUI();
                    actionBtn.setPlay();
                }))
                .onFatalError((s, exception) -> runOnUiThread(() -> {
                    Log.e(LOG_TAG, s, exception);

                    onStopUI();
                    actionBtn.setPlay();
                }))
                .onLog((tag, message, exception) -> {
                    if (exception == null) {
                        Log.v(tag, message);
                    } else {
                        Log.v(tag, message + "; " + exception.getClass() + ": " + exception.getMessage());
                    }

                    return Unit.INSTANCE;
                })
                .build();

        onPlayUI();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.action_btn) {
            if (SetsKt.setOf("start", "play").contains(actionBtn.getContentDescription().toString()))
                onPlayUI();
            else if (actionBtn.getContentDescription().toString().equals("stop"))
                onStopUI();
        }
    }


    private String getSpeedString(Pair<Integer, Integer> speed) {
        return String.format(Locale.ENGLISH, "%d.%d", speed.first, speed.second);
    }

    private void onResultUI(SpeedTestResult speedTestResult, String ping) {

        mSubResults.setVisibility(View.GONE);

        mResults.setVisibility(View.VISIBLE);

        mCard.setEmptyCaptions();
        mCard.setMessage("Done");

        mResults.setSpeedTestResult(speedTestResult);
        mResults.setPing(ping);
        mHeader.setSectionName("Results");

        actionBtn.setRestart();

        mHeader.showReturnBtn();

        shareBtn.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);

    }
    public void saveSuggestToPrefs(HashMap<String, Date> map){
        SharedPreferences.Editor prefEditor = getSharedPreferences(getString(R.string.globalSharedPreferences),MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(map);
        prefEditor.putString(ApplicationConstants.MAIN_ADDRESS_SUGGEST_KEY, json);
        prefEditor.apply();

    }

    public HashMap<String, Date> getSuggestFromPrefs(){
        Gson gson = new Gson();
        String json =
                getSharedPreferences(getString(R.string.globalSharedPreferences), MODE_PRIVATE).getString(
                        ApplicationConstants.MAIN_ADDRESS_SUGGEST_KEY,
                        null
                );
        Type type = new TypeToken<HashMap<String, Date>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void onPlayUI() {
        mCard.setDefaultCaptions();

        cWave.attachColor(getColor(R.color.mint));

        mSubResults.setVisibility(View.VISIBLE);
        mSubResults.setEmpty();
        mResults.setVisibility(View.GONE);

        mHeader.setSectionName("Measuring");
        mHeader.disableButtonGroup();

        actionBtn.setStop();

        shareBtn.setVisibility(View.GONE);
        saveBtn.setVisibility(View.GONE);

        sm.flushResults();
        String mainAddress =
                getSharedPreferences(getString(R.string.globalSharedPreferences), MODE_PRIVATE).getString(
                    ApplicationConstants.MAIN_ADDRESS_KEY,
                    getString(R.string.default_main_address)
                );

        HashMap<String, Date> suggestMap = getSuggestFromPrefs();

        suggestMap.put(mainAddress, new Date());
        while (suggestMap.size() > 10) {
            Date oldestDate = null;
            String oldestKey = null;
            for(Map.Entry<String, Date> entry : suggestMap.entrySet()) {
                if (oldestDate == null || oldestDate.after(entry.getValue())) {
                    oldestDate = entry.getValue();
                    oldestKey = entry.getKey();
                }
            }
            suggestMap.remove(oldestKey);
        }

        saveSuggestToPrefs(suggestMap);

        speedTestManager.start(
                getSharedPreferences(getString(R.string.globalSharedPreferences), MODE_PRIVATE).getBoolean(
                        ApplicationConstants.USE_BALANCER_KEY,
                        true
                ),
                mainAddress,
                TASK_DELAY,
                stageConfigurationParser
                        .parseFromPreferences(
                                getSharedPreferences("iperf_args_pipeline", MODE_PRIVATE),
                                this::getString
                        )
                        .stream()
                        .map(StageConfigurationParser.FromPreferencesStageConfiguration::getStageConfiguration)
                        .collect(Collectors.toList())
        );
    }

    public void onStopUI() {
        mHeader.enableButtonGroup();

        cWave.stop();
        actionBtn.setPlay();

        speedTestManager.stop();
    }

}
