package ru.scoltech.openran.speedtest.parser;

import static android.content.Context.MODE_PRIVATE;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import ru.scoltech.openran.speedtest.ApplicationConstants;
import ru.scoltech.openran.speedtest.R;
import ru.scoltech.openran.speedtest.domain.StageConfiguration;


public class StageConfigurationParser {

    private <T> void saveToPrefs( Context context, Integer id,  String field, T data){
        SharedPreferences.Editor prefEditor = context.getSharedPreferences(context.getString(id),MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        prefEditor.putString(field, json);
        prefEditor.apply();
    }

    public void saveStageToPrefs( Context context, List<StageConfiguration> map) {
        saveToPrefs(context,R.string.pipeline_shared_storage_name,"data",map);
    }

    public void saveFirstTimeToPrefs( Context context) {
        saveToPrefs(context,R.string.pipeline_shared_storage_name,"firstTime",true);
    }

    private Type getArrayListType(){
        return new TypeToken<ArrayList<StageConfiguration>>(){}.getType();
    }

    private Type getBooleanType(){
        return new TypeToken<Boolean>(){}.getType();
    }

    public <T> T getFromPrefs(Context context,Integer id,  String field, Class classType){
        Gson gson = new Gson();
        String json =
                context.getSharedPreferences(context.getString(id), MODE_PRIVATE).getString(
                        field,
                        null
                );
        Integer in = 5;
        Type type = null;
        if (classType == Boolean.class)
            type = getBooleanType();
        if (classType == ArrayList.class)
            type = getArrayListType();
        T out = gson.fromJson(json,type);
        return out;
    }

    public ArrayList<StageConfiguration> getStageFromPrefs( Context context){
        ArrayList<StageConfiguration> map = getFromPrefs(context,R.string.pipeline_shared_storage_name,"data", ArrayList.class);
        if (map != null)
            return map;
        else {
            ArrayList<StageConfiguration> configurations = new ArrayList<StageConfiguration>(0);
            Boolean isFirstTime = getFromPrefs(context,R.string.pipeline_shared_storage_name,"firstTime",Boolean.class);
            if (isFirstTime == null) {
                saveFirstTimeToPrefs(context);
                configurations.add(new StageConfiguration("Download Speed Test", context.getString(R.string.download_server_iperf_args), context.getString(R.string.download_device_iperf_args)));
                configurations.add(new StageConfiguration("Upload Speed Test", context.getString(R.string.upload_server_iperf_args), context.getString(R.string.upload_device_iperf_args)));
                saveStageToPrefs(context,configurations);
            }
            return configurations;
        }
    }
}
