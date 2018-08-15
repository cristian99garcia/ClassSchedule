package com.cristiangarcia.classschedule;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private String savedData = "";
    private TimetableFragment fg;

    private enum VisibleFragment {
        TIMETABLE,
        TESTS,
        SETTINGS,
        NULL
    }

    private VisibleFragment currentFragment = VisibleFragment.NULL;

    private void showTimetable() {
        if (currentFragment == VisibleFragment.TIMETABLE)
            return;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        fg = TimetableFragment.newInstance();
        fg.loadData(savedData);

        if (currentFragment != VisibleFragment.NULL)
            transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);

        transaction.replace(R.id.frame_layout, fg);
        transaction.commit();

        currentFragment = VisibleFragment.TIMETABLE;
    }

    private void showTests() {
        if (currentFragment == VisibleFragment.TESTS)
            return;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (fg != null)
            savedData = Pojo.classDataToJSON(fg.getData());

        fg = null;
        Fragment fragment = TestsFragment.newInstance();

        if (currentFragment == VisibleFragment.TIMETABLE)
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        else if (currentFragment != VisibleFragment.NULL)
            transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);

        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();

        currentFragment = VisibleFragment.TESTS;
    }

    private void showSettings() {
        if (currentFragment == VisibleFragment.SETTINGS)
            return;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (fg != null)
            savedData = Pojo.classDataToJSON(fg.getData());

        fg = null;
        PreferenceFragmentCompat fragment = SettingsFragment.newInstance();

        if (currentFragment != VisibleFragment.NULL)
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);

        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();

        currentFragment = VisibleFragment.SETTINGS;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_timetable:
                    showTimetable();
                    break;

                case R.id.navigation_tests:
                    showTests();
                    break;

                case R.id.navigation_settings:
                    showSettings();
                    break;

                default:
                    return false;
            }

            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        showTimetable();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String json;
        if (intent.hasExtra(getResources().getString(R.string.put_json)) && bundle != null)
            json = bundle.getString(getResources().getString(R.string.put_json));
        else
            json = Pojo.loadSavedJSON(this);

        // Procedure:
        //   Save delete class data
        //   Load previous data
        //   Add class new data

        if (intent.hasExtra(getResources().getString(R.string.delete_class)) && bundle != null && bundle.getBoolean(getResources().getString(R.string.delete_class))) {
            ClassData data = new ClassData();
            String[] arr = bundle.getStringArray(getResources().getString(R.string.delete_class_days));
            String s = (arr != null)? arr[0]: "";

            data.setName(bundle.getString(getResources().getString(R.string.delete_class_name)))
                    .setAdditionalData(bundle.getString(getResources().getString(R.string.delete_class_additional_data)))
                    .setStartTime(bundle.getString(getResources().getString(R.string.delete_class_start_time)))
                    .setEndTime(bundle.getString(getResources().getString(R.string.delete_class_end_time)))
                    .setColor(bundle.getInt(getResources().getString(R.string.delete_class_color)))
                    .setDay(s);

            fg.deleteClassData(data);
        }

        fg.loadData(json);

        if (intent.hasExtra(getResources().getString(R.string.new_class)) && bundle != null && bundle.getBoolean(getResources().getString(R.string.new_class))) {
            String[] days = bundle.getStringArray(getResources().getString(R.string.add_class_days));

            if (days == null) {
                TimetableWidget.scheduleNextUpdate(this);
                saveData();
                return;
            }

            for (String day: days) {
                ClassData data = new ClassData();
                data.setName(bundle.getString(getResources().getString(R.string.add_class_name)))
                        .setAdditionalData(bundle.getString(getResources().getString(R.string.add_class_additional_data)))
                        .setStartTime(bundle.getString(getResources().getString(R.string.add_class_start_time)))
                        .setEndTime(bundle.getString(getResources().getString(R.string.add_class_end_time)))
                        .setColor(bundle.getInt(getResources().getString(R.string.add_class_color)))
                        .setDay(day);

                // Log.d("New class data", data.toString() + " " + fg.collide(data));
                ClassData collidesWith = fg.collide(data);

                final ClassData _d = data;
                final String js = json;
                if (collidesWith != null) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.frame_layout), getResources().getString(R.string.collides_with) + ": " + _d.toStringFancy(), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getResources().getString(R.string.edit), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = Pojo.prepareIntentToEditClass(view.getContext(), _d);
                                    intent.putExtra(view.getContext().getResources().getString(R.string.put_json), js);
                                    view.getContext().startActivity(intent);
                                }
                            });
                    snackbar.show();
                }

                fg.addClassData(data);
            }
        }

        /*
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean monday = sharedPref.getBoolean(getString(R.string.key_monday), false);
        Log.d("onCreate", "Show monday? " + monday);
        */

        TimetableWidget.scheduleNextUpdate(this);
        saveData();
    }

    @Override
    public void onPause() {
        saveData();
        super.onPause();
    }

    private void saveData() {
        String content;  // = "[]";

        if (fg != null)
            content = Pojo.classDataToJSON(Pojo.joinArrays(fg.getData(), fg.getWaitingData()));
        else
            content = savedData;

        FileOutputStream file;
        try {
            file = openFileOutput(Pojo.JSON_FILE, Context.MODE_PRIVATE);
            file.write(content.getBytes());
            file.close();
        } catch (Exception e) {
            Pojo.addLog(getApplicationContext(), e.getMessage());
        }

        updateWidgets();
    }

    private void updateWidgets() {
        AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
        final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(this, TimetableWidget.class));
        for (int id : appWidgetIds)
            manager.notifyAppWidgetViewDataChanged(id, R.id.widget_listview);
    }

}