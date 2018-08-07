package com.cristiangarcia.classschedule;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

public class HourPreference extends DialogPreference {

    private int hourStart = 8;
    private int hourEnd = 22;

    public HourPreference(Context context) {
        this(context, null);
    }

    public HourPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public HourPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public HourPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getHourStart() {
        return hourStart;
    }

    public void setHourStart(int hour) {
        this.hourStart = hour;
        persistString(hour + ":" + this.hourEnd);
    }

    public int getHourEnd() {
        return hourEnd;
    }

    public void setHourEnd(int hour) {
        this.hourEnd = hour;
        persistString(this.hourStart + ":" + hour);
    }

    public void setHours(String data) {
        this.hourStart = Integer.parseInt(data.split(":")[0]);
        this.hourEnd = Integer.parseInt(data.split(":")[1]);
        persistString(data);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        int start, end;
        if (restorePersistedValue) {
            setHourStart(this.hourStart);
            setHourEnd(this.hourEnd);
        } else {
            start = Integer.parseInt(defaultValue.toString().split(":")[0]);
            end = Integer.parseInt(defaultValue.toString().split(":")[1]);
            setHours(getPersistedString(start + ":" + end));
        }
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.hour_preference;
    }
}
