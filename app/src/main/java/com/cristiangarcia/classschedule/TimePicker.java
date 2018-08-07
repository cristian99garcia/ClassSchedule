package com.cristiangarcia.classschedule;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

public class TimePicker extends AppCompatTextView {

    private TimePickerDialog dialog;

    private String time;

    public interface TimeChangedListener {
        void onTimeChanged(String time);
    }

    private TimeChangedListener onTimeChangedListener;

    public TimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        setTypeface(Typeface.DEFAULT_BOLD);
        createDialog();

        setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showTimeDialog();
            }
        });
    }

    public TimePicker setTime(String time) {
        this.time = time;
        this.setText(time);

        if (onTimeChangedListener != null)
            onTimeChangedListener.onTimeChanged(time);

        return this;
    }

    public TimePicker setTime(String time, boolean set) {
        this.time = time;
        this.setText(time);

        int hour = Integer.parseInt(time.split(":")[0]);
        int minute = Integer.parseInt(time.split(":")[1]);
        dialog.updateTime(hour, minute);

        if (onTimeChangedListener != null)
            onTimeChangedListener.onTimeChanged(time);

        return this;
    }

    public String getTime() {
        return this.time;
    }

    public void showTimeDialog() {
        dialog.show();
    }

    private void createDialog() {
        if (dialog == null) {
            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                private String timeToString(int hour, int minute) {
                    if ((""+minute).length() == 1)
                        return hour + ":0" + minute;
                    else
                        return hour + ":" + minute;
                }

                @Override
                public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
                    setTime(timeToString(hourOfDay, minute));
                }
            };

            dialog = new TimePickerDialog(getContext(), listener, 6, 0, false);

            if (Build.VERSION.SDK_INT >= 21)
                dialog.create();
            // else
            //      I dont know
        }
    }

    public TimeChangedListener getOnTimeChangedListener() {
        return onTimeChangedListener;
    }

    public void setOnTimeChangedListener(TimeChangedListener listener) {
        onTimeChangedListener = listener;
    }
}
