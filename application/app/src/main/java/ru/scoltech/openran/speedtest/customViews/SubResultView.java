package ru.scoltech.openran.speedtest.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.scoltech.openran.speedtest.R;

public class SubResultView extends LinearLayout {

    private TextView previousStageNameTextView;
    private TextView previousStageSpeedTextView;
    private TextView currentStageNameTextView;
    private TextView currentStageSpeedTextView;

    public SubResultView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        inflate(getContext(), R.layout.subresult_layout, this);

        init();
    }

    private void init() {
        previousStageNameTextView = findViewById(R.id.label_previous);
        previousStageSpeedTextView = findViewById(R.id.value_previous);
        currentStageNameTextView = findViewById(R.id.label_current);
        currentStageSpeedTextView = findViewById(R.id.value_current);
    }

    public void setCurrentStageSpeed(final String speed) {
        currentStageSpeedTextView.setText(speed);
    }

    public void setPreviousStageSpeed(final String speed) {
        previousStageSpeedTextView.setText(speed);
    }

    public void addNewStage(final String newStageName) {
        previousStageNameTextView.setText(currentStageNameTextView.getText());
        previousStageSpeedTextView.setText(currentStageSpeedTextView.getText());
        currentStageNameTextView.setText(newStageName);
        currentStageSpeedTextView.setText(getContext().getString(R.string.empty));
    }

    public void setEmpty() {
        previousStageNameTextView.setText(getContext().getString(R.string.empty));
        previousStageSpeedTextView.setText(getContext().getString(R.string.empty));
        currentStageNameTextView.setText(getContext().getString(R.string.empty));
        currentStageSpeedTextView.setText(getContext().getString(R.string.empty));
    }
}
