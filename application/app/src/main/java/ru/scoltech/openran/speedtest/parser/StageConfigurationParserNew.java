package ru.scoltech.openran.speedtest.parser;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.scoltech.openran.speedtest.ApplicationConstants;
import ru.scoltech.openran.speedtest.R;
import ru.scoltech.openran.speedtest.domain.StageConfiguration;


public class StageConfigurationParserNew {

    public void saveSuggestToPrefs( Context context, Map<String, StageConfiguration> map){
        SharedPreferences.Editor prefEditor = context.getSharedPreferences(context.getString(R.string.pipeline_shared_storage_name),MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(map);
        prefEditor.putString("data", json);
        prefEditor.apply();

    }

    public ArrayList<StageConfiguration> getSuggestFromPrefs( Context context){
        Gson gson = new Gson();
        String json =
                context.getSharedPreferences(context.getString(R.string.pipeline_shared_storage_name), MODE_PRIVATE).getString(
                        "data",
                        null
                );
        Type type = new TypeToken<HashMap<String, StageConfiguration>>(){}.getType();
        HashMap<String,StageConfiguration> map = gson.fromJson(json,type);
        if (map != null)
            return new ArrayList<StageConfiguration>(map.values());
        else
            return new ArrayList<StageConfiguration>(0);
    }
}
