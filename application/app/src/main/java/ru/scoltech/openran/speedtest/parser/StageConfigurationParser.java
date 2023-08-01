package ru.scoltech.openran.speedtest.parser;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.scoltech.openran.speedtest.ApplicationConstants;
import ru.scoltech.openran.speedtest.R;
import ru.scoltech.openran.speedtest.domain.StageConfiguration;


public class StageConfigurationParser {
    private final Gson gson = new Gson();

    public void saveStageToPrefs(Context context, List<StageConfiguration> list) {
        SharedPreferences.Editor prefEditor = context.getSharedPreferences(context.getString(R.string.pipeline_shared_storage_name), MODE_PRIVATE).edit();
        String json = gson.toJson(list);
        prefEditor.putString(context.getString(R.string.stage_field_name), json);
        prefEditor.apply();
    }

    public List<StageConfiguration> getStageFromPrefs(Context context) {
        if (context.getSharedPreferences(context.getString(R.string.pipeline_shared_storage_name), MODE_PRIVATE).contains(context.getString(R.string.stage_field_name))) {
            String json = context.getSharedPreferences(context.getString(R.string.pipeline_shared_storage_name), MODE_PRIVATE).getString(
                    context.getString(R.string.stage_field_name),
                    null
            );
            return gson.fromJson(json, new TypeToken<ArrayList<StageConfiguration>>(){}.getType());
        }
        return new ArrayList<StageConfiguration>(Arrays.asList(
                new StageConfiguration("Download Speed Test", context.getString(R.string.download_server_iperf_args), context.getString(R.string.download_device_iperf_args)),
                new StageConfiguration("Upload Speed Test", context.getString(R.string.upload_server_iperf_args), context.getString(R.string.upload_device_iperf_args))
        ));
    }
}
