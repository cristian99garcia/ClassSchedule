package com.cristiangarcia.classschedule;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class HourPreferenceFragmentCompat extends PreferenceDialogFragmentCompat implements NumberPicker.OnValueChangeListener {

    private android.widget.NumberPicker startPicker;
    private android.widget.NumberPicker endPicker;

    private Button positiveButton;

    public static HourPreferenceFragmentCompat newInstance(String key) {
        final Bundle args = new Bundle(1);

        args.putString(ARG_KEY, key);

        HourPreferenceFragmentCompat fragment = new HourPreferenceFragmentCompat();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onValueChange(NumberPicker picker, int previous, int current) {
        if (positiveButton != null)
            positiveButton.setEnabled(startPicker.getValue() < endPicker.getValue());
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog dialog = (AlertDialog) getDialog();

        if (dialog != null)
            positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
    }

    @Override
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);

        startPicker = view.findViewById(R.id.hour_start_picker);
        startPicker.setOnValueChangedListener(this);

        endPicker = view.findViewById(R.id.hour_end_picker);
        endPicker.setOnValueChangedListener(this);

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
            int start = startPicker.getValue();
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
