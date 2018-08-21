package com.cristiangarcia.classschedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;

public class HeaderRow extends LinearLayout {

    private boolean[] visibleDays;

    private int columnWidth;

    public HeaderRow(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);

        SharedPreferences preferences = context.getSharedPreferences(SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        visibleDays = new boolean[] {
                preferences.getBoolean(getResources().getString(R.string.key_sunday), false),
                preferences.getBoolean(getResources().getString(R.string.key_monday), true),
                preferences.getBoolean(getResources().getString(R.string.key_tuesday), true),
                preferences.getBoolean(getResources().getString(R.string.key_wednesday), true),
                preferences.getBoolean(getResources().getString(R.string.key_thursday), false),
                preferences.getBoolean(getResources().getString(R.string.key_friday), true),
                preferences.getBoolean(getResources().getString(R.string.key_saturday), false)
        };
    }

    private void createDaysTextViews() {
        LayoutParams lp = new LayoutParams(columnWidth / 2, LayoutParams.WRAP_CONTENT);
        //lp.width = (int)getResources().getDimension(R.dimen.time_cell_size);
        //lp.width = columnWidth / 2;

        TextView tv = new TextView(getContext());  // An empty view for the first column
        tv.setLayoutParams(lp);
        this.addView(tv);

        lp = new LayoutParams(columnWidth, LayoutParams.WRAP_CONTENT);
        //lp.width = columnWidth;

        for (int i=0; i<visibleDays.length; i++) {
            if (!visibleDays[i]) continue;

            tv = new TextView(getContext());
            tv.setText(Pojo.getDayAbbreviation(getResources(), i + 1));
            tv.setLayoutParams(lp);

            if (Build.VERSION.SDK_INT >= 17)
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            else
                tv.setGravity(Gravity.CENTER_HORIZONTAL);

            if (Build.VERSION.SDK_INT >= 23)
                tv.setTextColor(getResources().getColor(R.color.table_headers_text, null));

            this.addView(tv);
        }
    }

    public void setSize(int size) {
        columnWidth = size;
        createDaysTextViews();
    }

    public boolean[] getVisibleDays() {
        return this.visibleDays;
    }
}
