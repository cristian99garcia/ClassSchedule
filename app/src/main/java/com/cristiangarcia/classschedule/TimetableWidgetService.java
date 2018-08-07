package com.cristiangarcia.classschedule;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class TimetableWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TimetableWidgetFactory();
    }

    private class TimetableWidgetFactory implements RemoteViewsFactory {

        private JSONObject[] jsons = new JSONObject[] {};

        public void onCreate() {
        }

        public void onDataSetChanged() {
            //Log.d("TimetableWidgetService", "onDataSetChanged");
            loadSavedData();
            if (getCount() > 0)
                hideEmptyView();
            else
                showEmptyView();
        }

        public void onDestroy() {
        }

        public RemoteViews getViewAt(int position) {
            if (jsons.length <= 0)
                return null;

            RemoteViews rv = null;
            JSONObject json = jsons[position];

            if (json != null) {
                rv = new RemoteViews(getPackageName(), R.layout.timetable_widget_row);

                try {
                    rv.setTextViewText(R.id.class_name_textview, json.getString(Pojo.JSON_NAME));
                    rv.setTextViewText(R.id.class_from_textview, json.getString(Pojo.JSON_STARTS));
                    rv.setTextViewText(R.id.class_to_textview, json.getString(Pojo.JSON_ENDS));

                    if (!json.getString(Pojo.JSON_ADDITIONAL).equals(""))
                        rv.setTextViewText(R.id.class_additional_textview, "(" + json.getString(Pojo.JSON_ADDITIONAL) + ")");
                    else
                        rv.setTextViewText(R.id.class_additional_textview, "");

                } catch (org.json.JSONException e) {
                    Pojo.addLog(getApplicationContext(), e.getMessage());
                }
            }

            return rv;
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return jsons.length;
        }

        public RemoteViews getLoadingView() {
            return null;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public boolean hasStableIds() {
            return true;
        }

        private void loadSavedData() {
            String content = "", line;
            JSONArray array = new JSONArray();
            JSONArray _jsons;
            JSONObject _obj;

            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            try {
                FileInputStream file = openFileInput("data.json");
                InputStreamReader reader = new InputStreamReader(file);
                BufferedReader bReader = new BufferedReader(reader);

                while ((line = bReader.readLine()) != null) {
                    content += line + "\n";  // It should be one single line
                }

                file.close();
                _jsons = new JSONArray(content);

                for (int i=0; i<_jsons.length(); i++) {
                    _obj = (JSONObject) _jsons.get(i);

                    if (Pojo.getDayId((String)_obj.get("day")) == day) {
                        array.put(_obj);
                    }
                }

                // Sort data by time
                ArrayList<JSONObject> list = new ArrayList<>();
                for (int i=0; i<array.length(); i++) {
                    list.add((JSONObject)array.get(i));
                }

                Collections.sort(list, new Comparator<JSONObject>() {
                    @Override
                    public int compare(final JSONObject obj1, JSONObject obj2) {
                        // return 1 if obj2 should be before than ob1
                        // return -1 if obj1 should be before than obj2
                        // return 0 otherwise

                        try {
                            return (int)Math.signum(Pojo.getElapsedTime(obj2.getString("starts"), obj1.getString("starts")));
                        } catch (org.json.JSONException e) {
                            Pojo.addLog(getApplicationContext(), e.getMessage());
                        }
                        return 1;
                    }
                });

                jsons = new JSONObject[list.size()];
                jsons = list.toArray(jsons);
            } catch (Exception e) {
                Pojo.addLog(getBaseContext(), e.getMessage());
            }
        }

        private void showEmptyView() {
            RemoteViews rv = new RemoteViews(getPackageName(), R.layout.timetable_widget);
            rv.setViewVisibility(R.id.empty_view, View.VISIBLE);

            ComponentName w = new ComponentName(getApplicationContext(), TimetableWidget.class);
            (AppWidgetManager.getInstance(getApplicationContext())).updateAppWidget(w, rv);
        }

        private void hideEmptyView() {
            RemoteViews rv = new RemoteViews(getPackageName(), R.layout.timetable_widget);
            rv.setViewVisibility(R.id.empty_view, View.INVISIBLE);

            ComponentName w = new ComponentName(getApplicationContext(), TimetableWidget.class);
            (AppWidgetManager.getInstance(getApplicationContext())).updateAppWidget(w, rv);
        }
    }
}