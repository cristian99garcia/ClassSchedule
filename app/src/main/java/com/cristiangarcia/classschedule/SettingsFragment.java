package com.cristiangarcia.classschedule;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;

public class SettingsFragment extends PreferenceFragmentCompat implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    private final static String TAG = SettingsFragment.class.getName();
    public final static String SETTINGS_SHARED_PREFERENCES_FILE_NAME = TAG + ".txt";

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
            dialogFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        preferenceFragmentCompat.setPreferenceScreen(preferenceScreen);
        return true;
    }
}
