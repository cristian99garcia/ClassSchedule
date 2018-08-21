package com.cristiangarcia.classschedule;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;

import java.util.Calendar;

public class SettingsFragment extends PreferenceFragmentCompat implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = SettingsFragment.class.getName();
    public final static String SETTINGS_SHARED_PREFERENCES_FILE_NAME = TAG + ".txt";

    public boolean nested;
    PreferenceFragmentCompat fragment;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES_FILE_NAME);

        int[] keys = new int[] {
                R.string.key_sunday,
                R.string.key_monday,
                R.string.key_tuesday,
                R.string.key_wednesday,
                R.string.key_thursday,
                R.string.key_friday,
                R.string.key_saturday
        };

        SwitchPreferenceCompat preference;
        String key;

        SharedPreferences sp = preferenceManager.getSharedPreferences();
        if (!sp.getBoolean("settingsSavedOnce", false)) {
            sp.edit().putBoolean("settingsSavedOnce", true).apply();

            if (getContext() == null)
                return;

            SharedPreferences _default = PreferenceManager.getDefaultSharedPreferences(getContext());
            String name;
            for (int id: keys) {
                name = getResources().getString(id);
                sp.edit().putBoolean(name, _default.getBoolean(name, true)).apply();
            }
        }

        for (int id: keys) {
            key = getResources().getString(id);
            preference = (SwitchPreferenceCompat) findPreference(key);
            preference.setChecked(sp.getBoolean(key, true));
        }

        HourPreference hPreference = (HourPreference) findPreference(getResources().getString(R.string.key_hours));
        hPreference.setHours(sp.getString(getResources().getString(R.string.key_hours), getResources().getString(R.string.default_visible_hours)));

        CheckBoxPreference notifyTests = (CheckBoxPreference) findPreference(getResources().getString(R.string.key_notify_tests));
        notifyTests.setChecked(sp.getBoolean(getResources().getString(R.string.key_notify_tests), false));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof HourPreference)
            dialogFragment = HourPreferenceFragmentCompat.newInstance(preference.getKey());

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            if (getFragmentManager() != null)
                dialogFragment.show(getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else
            super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        setPreferenceScreen(preferenceScreen);
        nested = true;

        setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (getActivity() != null && Pojo.getDayFromKey(getResources(), key) == Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
            Pojo.updateWidgets(getActivity());
    }

    public void setDisplayHomeAsUpEnabled(boolean show) {
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        if (activity == null)
            return;

        ActionBar bar = activity.getSupportActionBar();
        if (bar == null)
            return;

        bar.setDisplayHomeAsUpEnabled(show);
    }

    public void showMainPreferenceScreen() {
        if (!nested)
            return;

        nested = false;

        setDisplayHomeAsUpEnabled(false);
        setPreferencesFromResource(R.xml.settings, null);
    }

    @Override
    public void setDivider(Drawable divider) {
        super.setDivider(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void setDividerHeight(int height) {
        super.setDividerHeight(0);
    }
}
