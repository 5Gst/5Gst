package ru.fivegst.speedtest.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.core.view.ViewGroupKt;

import ru.fivegst.speedtest.R;
import ru.fivegst.speedtest.domain.SpeedTestResult;

public class ResultView extends TableLayout {

    private TableLayout resultTable;
    private TextView pingTV;

    public ResultView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.result_layout, this);

        init();
    }

    private void init() {
        resultTable = findViewById(R.id.result_table);
        pingTV = findViewById(R.id.ping_value_res);
    }

    public void setSpeedTestResult(final SpeedTestResult speedTestResult) {
        resultTable.removeViews(1, resultTable.getChildCount() - 1);
        for (SpeedTestResult.Entry resultEntry : speedTestResult.getEntries()) {
            inflate(getContext(), R.layout.result_row_template, resultTable);
            final View entryView = resultTable.getChildAt(resultTable.getChildCount() - 1);
            ((TextView) entryView.findViewById(R.id.result_entry_name))
                    .setText(resultEntry.getStageConfiguration().getName());
            ((TextView) entryView.findViewById(R.id.result_entry_value))
                    .setText(resultEntry.getMeasurementResult());
        }
    }

    public void setPing(String ping) {
        pingTV.setText(ping);
    }
}
