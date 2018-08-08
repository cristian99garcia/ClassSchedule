package com.cristiangarcia.classschedule;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

public class TimePreference extends DialogPreference {

    private int lastHour = 0;
    private int lastMinute = 0;
    private TimePicker picker = null;
    View v;

    public static int getHour(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[0]);
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[1]);
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);
        //setPositiveButtonText("Set");
        //setNegativeButtonText("Cancel");
    }

    @Override
    protected View onCreateDialogView() {
        //picker = new TimePicker(getContext());
        //return picker;

        v = new Button(getContext());
        return v;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return ""; //(a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    }
}
