package ru.fivegst.speedtest.customViews;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ru.fivegst.speedtest.ApplicationConstants;
import ru.fivegst.speedtest.R;
import ru.fivegst.speedtest.activities.OptionsActivity;
import ru.fivegst.speedtest.domain.LogMessage;


public class HeaderView extends LinearLayout {

    private Button returnBtn;
    private Button historyBtn;
    private Button modeBtn;

    private TextView sectionNameTV;
    Gson gson =  new Gson();

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(getContext(), R.layout.header_layout, this);

        init();

        parseAttrs(context, attrs);

        returnBtn.setOnClickListener(v -> goToStart(v.getContext()));

        historyBtn.setOnClickListener(v -> goToHistory(v.getContext()));

        modeBtn.setOnClickListener(v -> goToDev(v.getContext()));
    }

    private void init() {
        sectionNameTV = findViewById(R.id.section_name);

        returnBtn = findViewById(R.id.return_btn);
        historyBtn = findViewById(R.id.history_go_btn);
        modeBtn = findViewById(R.id.mode_switch_btn);
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeaderView);
        int count = typedArray.getIndexCount();

        try {
            for (int i = 0; i < count; ++i) {

                int attr = typedArray.getIndex(i);

                if (attr == R.styleable.HeaderView_is_active_back) {
                } else if (attr == R.styleable.HeaderView_is_active_button_group) {
                    boolean isActiveButtonGroup = typedArray.getBoolean(attr, false);

                    changeStateButtonGroup(isActiveButtonGroup);

                } else if (attr == R.styleable.HeaderView_section_name) {
                    String sectionNameStr = typedArray.getString(attr);

                    setSectionName(sectionNameStr);
                }
            }
        } finally {
            typedArray.recycle();
        }

    }

    //TODO global: check if it efficient way to go to main menu, especially from the same activity
    private void goToStart(Context context) {
        AppCompatActivity activity = (AppCompatActivity) context;
        activity.finish();
    }

    private void goToHistory(Context context) {
        String jsonGet = getContext()
                .getSharedPreferences(getContext().getString(R.string.globalSharedPreferences), MODE_PRIVATE)
                .getString(
                        ApplicationConstants.UI_LOGS_KEY,
                        gson.toJson(new ArrayList<LogMessage>())
                );
        Type type = new TypeToken<List<LogMessage>>() {
        }.getType();
        List<LogMessage> logData = gson.fromJson(jsonGet, type);
        if (logData == null) {
            logData = Collections.emptyList();
        }

        StringBuilder logBuilder = new StringBuilder();
        for(int i = logData.size() - 1; i >= 0; i--){
            logBuilder.append(logData.get(i).toString());
            logBuilder.append("\n\n");
        }

        AlertDialog logs = new AlertDialog.Builder(context)
                .setTitle("app logs")
                .setMessage(logBuilder.toString())
                .setPositiveButton(android.R.string.ok, null)
                .create();
        Objects.requireNonNull(logs.getWindow())
                .setWindowAnimations(R.style.AlertDialogAnimation);
        logs.show();
    }

    private void goToDev(Context context) {
        Log.d("HEADER", "goToDev: pressed btn");
        Intent intent = new Intent(context, OptionsActivity.class);
        context.startActivity(intent);
    }

    private void changeStateButtonGroup(boolean flag) {
        if (flag) {
            enableButtonGroup();
        } else {
            disableButtonGroup();
        }
    }

    public void hideOptionsButton() {
        modeBtn.setEnabled(false);
        modeBtn.setAlpha(0.5f);
    }

    public void disableButtonGroup() {
        historyBtn.setEnabled(false);
        historyBtn.setAlpha(0.5f);

        hideOptionsButton();
    }

    public void enableButtonGroup() {
        historyBtn.setEnabled(true);
        historyBtn.setAlpha(1f);

        modeBtn.setEnabled(true);
        modeBtn.setAlpha(1f);
    }

    public void setSectionName(String sectionName) {
        sectionNameTV.setText(sectionName);
    }

    public void showReturnBtn() {
        returnBtn.setVisibility(View.VISIBLE);
    }

}