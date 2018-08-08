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
    public final static String SETTINGS_SHARED_PREFERENCES_FILE_NAME = TAG + ".SETTINGS_SHARED_PREFERENCES_FILE_NAME";

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES_FILE_NAME);

        int[] keys = new int[] {
                R.string.key_sunday,
                R.string.key_monday,
                R.string.key_tuesday,
                R.string.key_thursday,
                R.string.key_friday,
                R.string.key_saturday
        };

        PreferenceManager preferenceManager = getPreferenceManager();
        SwitchPreferenceCompat preference;
        String key;

        for (int i=0; i<keys.length; i++) {
            key = getResources().getString(keys[0]);
            preference = (SwitchPreferenceCompat) findPreference(key);
            preference.setChecked(preferenceManager.getSharedPreferences().getBoolean(key, false));
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
