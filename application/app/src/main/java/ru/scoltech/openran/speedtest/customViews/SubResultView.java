package ru.scoltech.openran.speedtest.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.scoltech.openran.speedtest.R;

public class SubResultView extends LinearLayout {

    private ViewGroup previousStageInfo;
    private ViewGroup currentStageInfo;
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
        previousStageInfo = findViewById(R.id.block_previous);
        currentStageInfo = findViewById(R.id.block_current);
        previousStageNameTextView = findViewById(R.id.label_previous);
        previousStageSpeedTextView = findViewById(R.id.value_previous);
        currentStageNameTextView = findViewById(R.id.label_current);
        currentStageSpeedTextView = findViewById(R.id.value_current);
    }

    public void setCurrentStageSpeed(final String speed) {
        currentStageSpeedTextView.setText(speed);
    }

    public void addNewStage(final String newStageName) {
        if (currentStageInfo.getVisibility() != ViewGroup.VISIBLE) {
            currentStageInfo.setVisibility(ViewGroup.VISIBLE);
        } else {
            previousStageInfo.setVisibility(ViewGroup.VISIBLE);
        }

        previousStageNameTextView.setText(currentStageNameTextView.getText());
        previousStageSpeedTextView.setText(currentStageSpeedTextView.getText());
        currentStageNameTextView.setText(newStageName);
        currentStageSpeedTextView.setText(getContext().getString(R.string.empty));
    }

    public void setEmpty() {
        currentStageInfo.setVisibility(ViewGroup.GONE);
        previousStageInfo.setVisibility(ViewGroup.GONE);
        previousStageNameTextView.setText(getContext().getString(R.string.empty));
        previousStageSpeedTextView.setText(getContext().getString(R.string.empty));
        currentStageNameTextView.setText(getContext().getString(R.string.empty));
        currentStageSpeedTextView.setText(getContext().getString(R.string.empty));
    }
}
