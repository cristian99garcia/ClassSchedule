package com.cristiangarcia.classschedule;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.RestrictTo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Pojo {

    public static String JSON_FILE = "data.json";
    public static String LOG_FILE = "log.txt";

    public static String JSON_NAME = "name";
    public static String JSON_ADDITIONAL = "additional";
    public static String JSON_COLOR = "color";
    public static String JSON_STARTS = "starts";
    public static String JSON_ENDS = "ends";
    public static String JSON_DAY = "day";

    public static boolean contains(String[] array, String value) {
        for (String v: array) {
            if (v != null && v.equals(value)) return true;
        }

        return false;
    }

    public static boolean contains(ClassData[] array, ClassData value) {
        for (ClassData v: array) {
            if (v != null && v.equalsTo(value)) return true;
        }

        return false;
    }

    public static String colorToString(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static ClassData[] addValue(ClassData[] array, ClassData value) {
        ClassData[] _array = new ClassData[array.length + 1];
        for (int i=0; i<array.length; i++) {
            array[i] = array[i];
        }

        _array[array.length] = value;
        return _array.clone();
    }

    public static ClassData[] removeValue(ClassData[] data, ClassData value) {
        ClassData[] newData = new ClassData[data.length - 1];

        int i = 0;
        for (ClassData _value: data) {
            if (!_value.equalsTo(value)) {
                newData[i] = _value;
                i++;
            }
        }

        return newData;
    }

    public static ClassData[] removeValueAt(ClassData[] data, int index) {
        return removeValue(data, data[index]);
    }

    public static ClassData[] joinArrays(ClassData[] array1, ClassData[] array2) {
        if (array1.length == 0) return array2;
        if (array2.length == 0) return array1;

        // I'm going to assume that no element of array1 is in array2 too
        ClassData[] array3 = new ClassData[array1.length + array2.length];

        for (int i=0; i<array1.length; i++)
            array3[i] = array1[i];

        for (int i=0; i<array2.length; i++)
            array3[i + array1.length - 1] = array2[i];

        return array3;
    }

    public static long getElapsedTime(String time1, String time2) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        try {
            Date d1 = sdf.parse(time1);
            Date d2 = sdf.parse(time2);

            return d2.getTime() - d1.getTime();
        } catch (java.text.ParseException e) {
            return 0;
        }
    }

    public static long getElapsedTime(String time1, int time2) {
        return getElapsedTime(time1, time2 + ":00");
    }

    public static long getElapsedTime(int time1, String time2) {
        return getElapsedTime(time1 + ":00", time2);
    }

    public static String addTime(String time, long milliseconds) {
        int hour = Integer.parseInt(time.split(":")[0]);
        int minute = Integer.parseInt(time.split(":")[1]);

        minute += (milliseconds / 60000);
        while (minute >= 60) {
            minute -= 60;
            hour += 1;
        }

        // This throws a warning...
        // return hour + ":" + String.format(":%02d", minute);

        String m = minute + "";
        if (m.length() == 1)
            m = "0" + m;

        return hour + ":" + m;
    }

    public static String getMiddleTime(String time1, String time2) {
        if (getElapsedTime(time1, time2) < 0)  // time2 was before time1
            return getMiddleTime(time2, time1);

        long elapsed = getElapsedTime(time1, time2);
        return addTime(time1, elapsed / 2);
    }

    public static int getDayId(String day) {
        switch (day) {
            case "sun":
                return Calendar.SUNDAY;

            case "mon":
                return Calendar.MONDAY;

            case "tue":
                return Calendar.TUESDAY;

            case "wed":
                return Calendar.WEDNESDAY;

            case "thu":
                return Calendar.THURSDAY;

            case "fri":
                return Calendar.FRIDAY;

            case "sat":
                return Calendar.SATURDAY;

            default:
                return -1;
        }
    }

    public static String getDayAbbreviation(Resources resources, int id) {
        switch (id) {
            case Calendar.SUNDAY:
                return resources.getString(R.string.sun);

            case Calendar.MONDAY:
                return resources.getString(R.string.mon);

            case Calendar.TUESDAY:
                return resources.getString(R.string.tue);

            case Calendar.WEDNESDAY:
                return resources.getString(R.string.wed);

            case Calendar.THURSDAY:
                return resources.getString(R.string.thu);

            case Calendar.FRIDAY:
                return resources.getString(R.string.fri);

            case Calendar.SATURDAY:
                return resources.getString(R.string.sat);

            default:
                return "";
        }
    }

    static Intent prepareIntentToEditClass(Context context, ClassData data) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra(context.getResources().getString(R.string.put_class_name), data.getName())
                .putExtra(context.getResources().getString(R.string.put_class_additional_data), data.getAdditionalData())
                .putExtra(context.getResources().getString(R.string.put_class_start_time), data.getStartTime())
                .putExtra(context.getResources().getString(R.string.put_class_end_time), data.getEndTime())
                .putExtra(context.getResources().getString(R.string.put_class_days), data.getDay())
                .putExtra(context.getResources().getString(R.string.put_class_color), data.getColor());

        return intent;
    }

    public static String loadSavedJSON(Context context) {
        String line;

        try {
            FileInputStream file = context.openFileInput(JSON_FILE);
            InputStreamReader reader = new InputStreamReader(file);
            BufferedReader bReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();

            while ((line = bReader.readLine()) != null) {
                // In Java, doing += to a String will create a new Object
                builder.append(line);
                builder.append("\n");
            }

            file.close();

            // Log.d("JSON", builder.toString());
            return builder.toString();
        } catch (Exception e) {
            Pojo.addLog(context, e.getMessage());
            return "[]";
        }
    }

    public static String classDataToJSON(ClassData[] data) {
        JSONArray array = new JSONArray();
        JSONObject object;

        for (ClassData d: data) {
            if (d == null) continue;

            object = new JSONObject();

            try {
                object.put(JSON_NAME, d.getName());
                object.put(JSON_ADDITIONAL, d.getAdditionalData());
                object.put(JSON_COLOR, colorToString(d.getColor()));
                object.put(JSON_STARTS, d.getStartTime());
                object.put(JSON_ENDS, d.getEndTime());
                object.put(JSON_DAY, d.getDay());
                array.put(object);
            } catch (org.json.JSONException e) {
                Log.d("Error saving data", e.getMessage());
            }
        }

        return array.toString();
    }

    static void addLog(Context context, String log) {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        String content = format.format(date) + "\n" + log;

        // Log.d("Pojo.addLog", content);
        try {
            FileOutputStream file = context.openFileOutput(Pojo.LOG_FILE, Context.MODE_PRIVATE);
            file.write(content.getBytes());
            file.close();
        } catch (Exception e) {
            Log.d("addLog", "ERROR SAVING LOG:");
            e.printStackTrace();
        }
    }
}
