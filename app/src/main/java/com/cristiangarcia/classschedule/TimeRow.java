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
        this.createRow();
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

    private void createRow() {
        TimeCell tc;

        boolean[] days = header.getVisibleDays();
        for (int i=0; i<days.length; i++) {
            if (!days[i]) continue;

            tc = new TimeCell(getContext(), starts, ends);
            tc.setSize(size);
            this.addView(tc, false);
            this.timeCells[i] = tc;
        }
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
