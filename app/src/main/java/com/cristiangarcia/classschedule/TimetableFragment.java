package com.cristiangarcia.classschedule;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;

public class TimetableFragment extends Fragment {

    private int firstHour = 6;
    private int lastHour = 18;

    private TimeRow[] timeRows;
    private ClassData[] data = new ClassData[] {};
    private ClassData[] waitingData = new ClassData[] {};
    private ClassData[] waitingDeleteData = new ClassData[] {};

    private BroadcastReceiver tickReceiver;

    public static TimetableFragment newInstance() {
        return new TimetableFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    timeLineTick();
                }
            }
        };

        if (getActivity() != null)
            getActivity().registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        SharedPreferences preferences = getActivity().getSharedPreferences(
                SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);

        String hours = preferences.getString(getResources().getString(R.string.key_hours), getResources().getString(R.string.default_visible_hours));
        firstHour = Integer.parseInt(hours.split(":")[0]);
        lastHour = Integer.parseInt(hours.split(":")[1]);

        if (lastHour < firstHour)
            lastHour = firstHour + 1;

        if (timeRows == null)
            timeRows = new TimeRow[lastHour - firstHour];

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (getActivity() != null)
            getActivity().unregisterReceiver(tickReceiver);
    }

    private void timeLineTick() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        if (hour < firstHour || hour > lastHour - 1)
            return;

        if (timeRows == null || timeRows.length == 0)
            return;

        TimeCell cell = timeRows[hour - firstHour].getTimeCell(day);
        if (cell != null)
            cell.setMinuteLine(minute);

        // Hide the minute line in case the app was opened in the previous hour
        if (hour - 1 >= firstHour && hour - 1 <= lastHour) {
            cell = timeRows[hour - firstHour - 1].getTimeCell(day);
            if (cell != null && cell.getShowMinuteLine())
                // Evit calling hideMinuteLine when it isn't necessary, because that method
                // call to redraw all the TimeCell
                cell.hideMinuteLine();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (inflater == null)
            return null;

        return inflater.inflate(R.layout.timetable_fragment, container, false);
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null)
            return;

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        FloatingActionButton editButton = getActivity().findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditActivity.class);
                intent.putExtra(getResources().getString(R.string.put_json), Pojo.classDataToJSON(data));

                Context context = getContext();
                if (context != null)
                    context.startActivity(intent);
            }
        });

        TableLayout tlTable = getActivity().findViewById(R.id.table_calendar);

        HeaderRow headerRow = getActivity().findViewById(R.id.header_row);
        headerRow.setSize(size.x / 3);

        HourColumn hourColumn = getActivity().findViewById(R.id.hour_column);
        hourColumn.setSize(size.x / 3);

        Context context = getActivity().getBaseContext();

        TimeRow row;
        int i;

        for (i=firstHour; i<lastHour; i++) {
            row = new TimeRow(context, headerRow, i,i + 1, size.x / 3);
            timeRows[i - firstHour] = row;
            tlTable.addView(row);

            for (TimeCell cell: row.getTimeCells()) {
                if (cell == null) continue;

                cell.setOnTouchListener(new View.OnTouchListener() {
                    float x;
                    float y;
                    float difference = 300;

                    public boolean onTouch(View timeCell, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                x = event.getX();
                                y = event.getY();
                                break;

                            case MotionEvent.ACTION_UP:
                                if (Math.sqrt(Math.pow(x - event.getX(), 2) + Math.pow(x - event.getY(), 2)) <= difference) {
                                    ClassData d = ((TimeCell)timeCell).getDataAtPoint(event.getY());
                                    if (d != null) {
                                        Intent intent = Pojo.prepareIntentToEditClass(getContext(), d);

                                        Context context = getContext();
                                        if (context == null)
                                            return true;

                                        intent.putExtra(getContext().getResources().getString(R.string.put_json), Pojo.classDataToJSON(data));  // IMPORTANT: load data at clicking time
                                        getContext().startActivity(intent);
                                    }

                                    timeCell.performClick();
                                }

                        }
                        return true;
                    }
                });
            }
        }

        timeLineTick();
        loadWaitingData();
        loadWaitingDeleteData();
    }

    public void saveClassInfo(ClassData classData) {
        ClassData[] _data = new ClassData[data.length + 1];

        for (int i=0; i<data.length; i++) {
            _data[i] = data[i];
        }

        _data[data.length] = classData;
        this.data = _data.clone();
    }

    public ClassData[] getData() {
        return data;
    }

    public ClassData[] getWaitingData() {
        return waitingData;
    }

    public void loadData(String data) {
        // JSON data format:
        /*
        [
            {
                "name": CLASSNAME,
                "additional": ADDITIONAL DATA,
                "color": COLOR,
                "day": "mon|tue|wed|thu|fri|sat|sun",
                "starts": "HH:mm",
                "ends": "HH:mm"
            }
        ]
         */

        JSONArray reader;

        try {
            reader = new JSONArray(data);
        } catch (org.json.JSONException e) {
            Pojo.addLog(getContext(), e.getMessage());
            return;
        }

        JSONObject object;
        ClassData classData = null;

        for (int i=0; i<reader.length(); i++) {
            try {
                object = reader.getJSONObject(i);
                classData = new ClassData();
                classData.setStartTime(object.getString(Pojo.JSON_STARTS))
                         .setEndTime(object.getString(Pojo.JSON_ENDS))
                         .setName(object.getString(Pojo.JSON_NAME))
                         .setAdditionalData(object.getString(Pojo.JSON_ADDITIONAL))
                         .setColor(object.getString(Pojo.JSON_COLOR))
                         .setDay(object.getString(Pojo.JSON_DAY));

                this.addClassData(classData);
            } catch (org.json.JSONException e) {
                Log.d("Error Parsing JSON", e.getMessage());
            }
        }
    }

    private boolean _collide(ClassData d1, ClassData d2) {
        return !Pojo.contains(waitingDeleteData, d2) && !d1.equalsTo(d2) && d1.collide(d2);
    }

    @Nullable
    public ClassData collide(ClassData classd, @Nullable ClassData ignore) {
        for (ClassData _classd: data) {
            if (_collide(classd, _classd) && !_classd.equalsTo(ignore))
                return _classd;
        }

        for (ClassData _classd: waitingData) {
            if (_collide(classd, _classd) && !_classd.equalsTo(ignore))
                return _classd;
        }

        return null;
    }

    public void addWaitingData(ClassData data) {
        if (Pojo.contains(this.waitingDeleteData, data))
            return;

        ClassData[] array = new ClassData[waitingData.length + 1];
        for (int i=0; i<waitingData.length; i++) {
            array[i] = waitingData[i];
        }

        array[waitingData.length] = data;
        waitingData = array.clone();
    }

    public void addWaitingForDeleteData(ClassData data) {
        if (Pojo.contains(this.waitingData, data) || Pojo.contains(this.waitingDeleteData, data)) {
            // if data is waiting to be added, don't add it to 'waiting to delete' queue
            //Log.d("AWFDDATA", Pojo.contains(this.waitingData, data) + " " + Pojo.contains(this.waitingDeleteData, data));
            return;
        }

        ClassData[] array = new ClassData[waitingDeleteData.length + 1];
        for (int i=0; i<waitingDeleteData.length; i++) {
            array[i] = waitingDeleteData[i];
        }

        array[waitingDeleteData.length] = data;
        waitingDeleteData = array.clone();
    }

    private void loadWaitingData() {
        for (ClassData data: waitingData)
            this.addClassData(data);

        waitingData = new ClassData[] {};
    }

    private void loadWaitingDeleteData() {
        for (ClassData data: waitingDeleteData)
            this.deleteClassData(data);

        waitingDeleteData = new ClassData[] {};
    }

    public void addClassData(ClassData data) {
        if (timeRows == null) {
            addWaitingData(data);
            return;
        }

        this.saveClassInfo(data);

        for (TimeRow row: timeRows) {
            if (row != null)
                row.addClassData(data);
        }
    }

    public void deleteClassData(ClassData data) {
        if (data.equalsTo(null))
            return;

        if (timeRows == null || timeRows.length == 0) {
            addWaitingForDeleteData(data);
            return;
        }

        if (timeRows[0].getTimeCells() == null) {
            addWaitingForDeleteData(data);
            return;
        }

        ClassData[] cellData;

        for (TimeRow timeRow: timeRows) {
            if (timeRow == null)
                continue;

            for (TimeCell cell: timeRow.getTimeCells()) {
                if (cell == null)
                    continue;

                cellData = cell.getClasses();
                if (cellData == null)
                    continue;

                for (ClassData _value: cellData) {
                    if (_value.equalsTo(data)) {
                        while (Pojo.contains(this.data, _value)) {
                            this.data = Pojo.removeValue(this.data, _value);
                        }

                        cell.removeClass(data);
                    }
                }
            }
        }
    }
}
