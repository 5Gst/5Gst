package ru.fivegst.speedtest.customButtons;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.content.res.AppCompatResources;

import ru.fivegst.speedtest.R;

public class ActionButton extends androidx.appcompat.widget.AppCompatButton {
    //current state is at the content description attr

    public ActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setStop() {
        this.setContentDescription("stop");
        this.setForeground(AppCompatResources.getDrawable(getContext(), R.drawable.ic_stop));
    }

    public void setPlay() { // when continue after stopping
        this.setContentDescription("play");
        this.setForeground(AppCompatResources.getDrawable(getContext(), R.drawable.ic_play));
    }

    public void setStart() { // when start from main menu
        this.setContentDescription("start");
        this.setForeground(AppCompatResources.getDrawable(getContext(), R.drawable.ic_play));
    }

    public void setRestart() {
        this.setContentDescription("start");
        this.setForeground(AppCompatResources.getDrawable(getContext(), R.drawable.ic_replay));
    }


}
