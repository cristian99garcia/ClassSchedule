package com.cristiangarcia.classschedule;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

public class TimeRow extends TableRow {

    private int size;

    private TextView tvLabel;
    private TimeCell[] timeCells = { null, null, null, null, null, null, null };

    private int starts;
    private int ends;

    private HeaderRow header;

    public TimeRow(Context context) {
        super(context);
    }

    public TimeRow(Context context, HeaderRow header, int starts, int ends, int size) {
        super(context);

        this.size = size;
        this.header = header;

        this.configure();
        this.setStartTime(starts);
        this.setEndTime(ends);
        this.createRow(context);

        int id = this.starts;
        String suffix = "am";

        if (id > 12) {
            id -= 12;
            suffix = "pm";
        }

        String text = getResources().getString(getResources().getIdentifier("time" + id + suffix, "string", context.getPackageName()));
        setTimeText(text);
    }

    private void configure() {
        this.setMinimumHeight(size / 2);
    }

    public void addView(View view, boolean label) {
        if (label)
            super.addView(view, size / 2, size / 2);
        else
            super.addView(view, size, size / 2);
    }

    private void createRow(Context context) {
        tvLabel = new TextView(context);
        tvLabel.setText("");
        tvLabel.setTextColor(ContextCompat.getColor(context, R.color.time_label));

        if (Build.VERSION.SDK_INT >= 17)
            tvLabel.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        else
            tvLabel.setGravity(Gravity.CENTER_HORIZONTAL);

        this.addView(tvLabel, true);

        TimeCell tc;

        for (int day: header.getVisibleDays()) {
            if (day == 0) continue;

            tc = new TimeCell(getContext(), starts, ends);
            tc.setSize(size);
            this.addView(tc, false);
            this.timeCells[day - 1] = tc;
        }
    }

    public String getTimeText() {
        return (String)tvLabel.getText();
    }

    public void setTimeText(String text) {
        tvLabel.setText(text);
    }

    public int getStartTime() {
        return this.starts;
    }

    public void setStartTime(int time) {
        this.starts = time;
    }

    public int getEndTime() {
        return this.ends;
    }

    public void setEndTime(int time) {
        this.ends = time;
    }

    public TimeCell[] getTimeCells() {
        return this.timeCells;
    }

    public void addClassData(ClassData data) {
        int dayId = Pojo.getDayId(data.getDay());
        if (dayId <= 0) return;

        TimeCell cell = this.timeCells[dayId - 1];
        if (cell != null) {
            cell.addClass(data);
        }
    }

    public TimeCell getTimeCell(int day) {
        return timeCells[day - 1];
    }
}
