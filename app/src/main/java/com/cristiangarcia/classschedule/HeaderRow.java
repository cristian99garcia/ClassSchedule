package com.cristiangarcia.classschedule;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;

public class HeaderRow extends TableRow {

    private int[] defaultVisibleDays = {
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY
    };

    private int[] visibleDays = new int[7];

    private int columnWidth;

    public HeaderRow(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.HeaderRow,
                0, 0);

        try {
            String text = array.getString(R.styleable.HeaderRow_visibleDays);
            String[] _days = text.split(" ");

            for (int i = 0; i < _days.length; i++) {
                visibleDays[i] = Pojo.getDayId(_days[i]);  // context.getString(id)
            }
        } finally {
            if (visibleDays.length == 0) {
                visibleDays = defaultVisibleDays;
            }

            array.recycle();
        }
    }

    private void createDaysTextViews() {
        LayoutParams lp = new LayoutParams();
        //lp.width = (int)getResources().getDimension(R.dimen.time_cell_size);
        lp.width = columnWidth / 2;

        TextView tv = new TextView(getContext());  // An empty view for the first column
        tv.setLayoutParams(lp);
        this.addView(tv);

        lp = new LayoutParams();
        lp.width = columnWidth;

        for (int day: this.visibleDays) {
            if (day == 0) continue;

            tv = new TextView(getContext());
            tv.setText(Pojo.getDayAbbreviation(getResources(), day));
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

    public int[] getVisibleDays() {
        return this.visibleDays;
    }
}
