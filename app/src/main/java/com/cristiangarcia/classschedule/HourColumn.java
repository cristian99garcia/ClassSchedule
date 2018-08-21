package com.cristiangarcia.classschedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HourColumn extends LinearLayout {

    public int firstHour;
    public int lastHour;

    public HourColumn(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.VERTICAL);

        SharedPreferences preferences = context.getSharedPreferences(SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        String hours = preferences.getString(getResources().getString(R.string.key_hours), getResources().getString(R.string.default_visible_hours));

        firstHour = Integer.parseInt(hours.split(":")[0]);
        lastHour = Integer.parseInt(hours.split(":")[1]);
    }

    public void setSize(int size) {
        TextView tv;
        RelativeLayout.LayoutParams params;

        int id;
        String suffix = "am";

        int start = firstHour + 1;
        if (start == lastHour)
            start = firstHour;

        for (int i = start; i < lastHour; i++) {
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.height = size / 2;
            params.width = size / 2;

            tv = new TextView(getContext());

            if (i == start && (firstHour + 1) != lastHour)
                params.topMargin = (int)(size / 2 - tv.getTextSize() / 2);

            if (Build.VERSION.SDK_INT >= 17)
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            else
                tv.setGravity(Gravity.CENTER_HORIZONTAL);

            id = i;
            if (id > 12) {
                id -= 12;
                suffix = "pm";
            }

            // FIXME: Esto está horrible hecho:
            String text = getResources().getString(getResources().getIdentifier("time" + id + suffix, "string", getContext().getPackageName()));
            tv.setText(text);

            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.time_label));
            tv.setLayoutParams(params);
            addView(tv);
        }
    }
}
