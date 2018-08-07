package com.cristiangarcia.classschedule;

import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;

public class HourPreferenceFragmentCompat extends PreferenceDialogFragmentCompat {

    private android.widget.NumberPicker startPicker;
    private android.widget.NumberPicker endPicker;

    public static HourPreferenceFragmentCompat newInstance(String key) {
        final Bundle args = new Bundle(1);

        args.putString(ARG_KEY, key);

        HourPreferenceFragmentCompat fragment = new HourPreferenceFragmentCompat();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);

        startPicker = (android.widget.NumberPicker) view.findViewById(R.id.hour_start_picker);
        endPicker = (android.widget.NumberPicker) view.findViewById(R.id.hour_end_picker);

        int start = 1;
        int end = 1;
        DialogPreference preference = getPreference();
        if (preference instanceof HourPreference)
            start = ((HourPreference)preference).getHourStart();

        if (preference instanceof HourPreference)
            end = ((HourPreference)preference).getHourEnd();

        startPicker.setMaxValue(1);
        startPicker.setMaxValue(24);
        startPicker.setValue(start);

        endPicker.setMaxValue(1);
        endPicker.setMaxValue(24);
        endPicker.setValue(end);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int start = startPicker.getValue();// mNumberPicker.getHour();
            int end = endPicker.getValue();

            DialogPreference preference = getPreference();
            if (preference instanceof HourPreference) {
                HourPreference hPreference = (HourPreference)preference;
                if (hPreference.callChangeListener(start))
                    hPreference.setHourStart(start);

                if (hPreference.callChangeListener(end))
                    hPreference.setHourEnd(end);
            }
        }
    }
}
