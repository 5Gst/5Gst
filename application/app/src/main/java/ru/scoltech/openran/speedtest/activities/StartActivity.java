package ru.scoltech.openran.speedtest.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import kotlin.collections.MapsKt;
import kotlin.text.StringsKt;
import ru.scoltech.openran.speedtest.ApplicationConstants;
import ru.scoltech.openran.speedtest.R;


public class StartActivity extends AppCompatActivity {
    private static final String TAG = StartActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // begin block for hand mode switcher
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        @SuppressWarnings("unchecked")
        Map<Integer, String> themeLogMessage = MapsKt.mapOf(
                new kotlin.Pair<>(Configuration.UI_MODE_NIGHT_NO, "onCreate: Light Theme"),
                new kotlin.Pair<>(Configuration.UI_MODE_NIGHT_YES, "onCreate: Dark Theme"),
                new kotlin.Pair<>(Configuration.UI_MODE_NIGHT_UNDEFINED, "onCreate: Undefined Theme")
        );

        Log.d(TAG, themeLogMessage.get(currentNightMode));
        // end block

        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_start);

        init();
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

    private void updateSuggest() {
        LinearLayout suggestList = findViewById(R.id.main_address__suggest_list);
        suggestList.removeAllViews();

        EditText mainAddress = findViewById(R.id.main_address);
        String searchValue = String.valueOf(mainAddress.getText());

        HashMap<String, Date> suggestMap = getSuggestFromPrefs();

        List<String> suggestCandidates = suggestMap.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().contains(searchValue.toLowerCase()))
                .filter(entry -> !entry.getKey().equals(searchValue))
                .sorted(Comparator.comparing(entry -> ((Map.Entry<String, Date>)entry)
                                .getKey().toLowerCase().indexOf(searchValue.toLowerCase()))
                        .thenComparing(Comparator.comparing(entry -> ((Map.Entry<String, Date>)entry)
                                .getValue()).reversed()))
                .map(Map.Entry::getKey)
                .limit(3)
                .collect(Collectors.toList());

        updateSuggestBlockVisibility(suggestCandidates.isEmpty());
        for (int i = 0; i < suggestCandidates.size(); i++) {
            TextView suggestLine = (TextView) getLayoutInflater().inflate(R.layout.suggest_line,
                    suggestList,
                    false);

            suggestLine.setText(suggestCandidates.get(i));
            if (i % 2 == 1) {
                suggestLine.setBackgroundColor(ContextCompat.getColor(this, R.color.neutral_10));
            }

            suggestList.addView(suggestLine, 0);
            suggestLine.setOnClickListener(v -> mainAddress.setText(suggestLine.getText()));
        }
    }

    private void updateSuggestBlockVisibility(boolean isSuggestEmpty) {
        findViewById(R.id.main_address__suggest_block).setVisibility(isSuggestEmpty ? View.INVISIBLE : View.VISIBLE);
    }

    private void init() {

        final EditText mainAddress = findViewById(R.id.main_address);
        mainAddress.setText(
                getSharedPreferences(getString(R.string.globalSharedPreferences),MODE_PRIVATE).getString(
                        ApplicationConstants.MAIN_ADDRESS_KEY,
                        getString(R.string.default_main_address)
                )
        );
        updateSuggest();
        mainAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no operations
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no operations
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                final CharSequence newMainAddress = StringsKt.isBlank(s)? getString(R.string.default_main_address) : s;

                SharedPreferences.Editor preferences = getSharedPreferences(getString(R.string.globalSharedPreferences),MODE_PRIVATE).edit();
                preferences.putString(
                        ApplicationConstants.MAIN_ADDRESS_KEY,
                        newMainAddress.toString()
                );
                preferences.apply();
                updateSuggest();
            }
        });

        final RadioGroup modeRadioGroup = findViewById(R.id.mode_radio_group);
        modeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor preferences = getSharedPreferences(getString(R.string.globalSharedPreferences),MODE_PRIVATE).edit();
            preferences.putBoolean(
                    ApplicationConstants.USE_BALANCER_KEY,
                    checkedId == R.id.balancer_mode
            );
            preferences.apply();
        });

        final boolean useBalancer = getSharedPreferences(getString(R.string.globalSharedPreferences),MODE_PRIVATE)
                                    .getBoolean(ApplicationConstants.USE_BALANCER_KEY, true);
        if (useBalancer) {
            this.<RadioButton>findViewById(R.id.balancer_mode).setChecked(true);
        } else {
            this.<RadioButton>findViewById(R.id.direct_mode).setChecked(true);
        }

        boolean checkPrivacy = getSharedPreferences(getString(R.string.globalSharedPreferences),MODE_PRIVATE).getBoolean(ApplicationConstants.PRIVACY_SHOWN, false);
        Log.d(TAG,"PRIVACY pref="+checkPrivacy);
        if (!checkPrivacy) {
            SharedPreferences.Editor preferencesEditor = getSharedPreferences(getString(R.string.globalSharedPreferences),MODE_PRIVATE).edit();
            preferencesEditor.putBoolean(ApplicationConstants.PRIVACY_SHOWN, true);
            preferencesEditor.apply();
            findViewById(R.id.main_layout).post(this::showPrivacyPopUp);
        }
    }

    private void showPrivacyPopUp() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.policy_title))
                .setMessage(R.string.policy_content)
                .setPositiveButton(android.R.string.ok, null)
                .create();
        alert.show();

        ((TextView) Objects.requireNonNull(alert.findViewById(android.R.id.message)))
                .setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void showSuggest(View v) {
        findViewById(R.id.main_address__suggest).setVisibility(View.VISIBLE);
        findViewById(R.id.show_suggest_button).setVisibility(View.INVISIBLE);
    }

    public void onClick(View v) {
        Intent intent = new Intent(this, SpeedActivity.class);
        startActivity(intent);
    }
}