package com.cristiangarcia.classschedule;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

public class TimeCell extends View {

    private Paint paint = new Paint();
    private Rect bgRect;
    private Rect fgRect;
    private Rect rect = new Rect(0, 0, 0, 0);
    private Rect bounds = new Rect(0, 0, 0, 0);
    float fontSize;

    private ClassData[] classes = null;

    private int starts;
    private int ends;

    // public boolean debug = false;

    private int[] size = new int[] { 1, 1 };

    private int minuteLine;
    private boolean showMinuteLine = false;

    public TimeCell(Context context, int starts, int ends) {
        super(context);

        this.fontSize = getResources().getDimension(R.dimen.timetable_font_size);

        this.starts = starts;
        this.ends = ends;
    }

    public TimeCell(Context context) {
        super(context);

        this.fontSize = getResources().getDimension(R.dimen.timetable_font_size);
    }

    @Override
    public void onDraw(Canvas canvas) {
        // Foreground
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(fgRect, paint);

        // Background
        paint.setStrokeWidth(1);
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(bgRect, paint);

        // Data
        paint.setStyle(Paint.Style.FILL);
        this.drawClasses(canvas);
        this.drawClassesNames(canvas);

        // Minute line
        this.drawMinuteLine(canvas);
    }

    private void drawClasses(Canvas canvas) {
        if (classes == null || classes.length == 0)
            return;

        float by, ey;
        long elapsed;

        for (ClassData data: classes) {
            if (data == null)
                continue;

            elapsed = Pojo.getElapsedTime(this.getStartTime(), data.getStartTime());
            if (elapsed <= 0)  // La clase empieza antes que esta célula
                by = 0;
            else
                by = size[1] / 60 * (float) (elapsed / 60000);  // (elapsed ms) / 1000 / 60 = (elapsed min)

            elapsed = Pojo.getElapsedTime(data.getEndTime(), this.getEndTime());
            if (elapsed <= 0) {  // La clase termina después de esta clase
                ey = size[1];
            } else {
                ey = size[1] - size[1] / 60 * (float) (elapsed / 60000);  // (elapsed ms) / 1000 / 60 = (elapsed min)
            }

            rect.top = (int) by;
            rect.bottom = (int) ey;
            paint.setColor(data.getColor());
            canvas.drawRect(rect, paint);
        }
    }

    private void drawMinuteLine(Canvas canvas) {
        if (!showMinuteLine)
            return;

        int y = size[1] * minuteLine / 60;
        paint.setStrokeWidth(bgRect.height() / 30);  // Should be 60, but 30 looks better
        paint.setColor(Color.RED);
        canvas.drawLine(0, y, size[0], y, paint);
    }

    private void drawClassesNames(Canvas canvas) {
        if (classes == null || classes.length == 0)
            return;

        String middle;
        long elapsed;
        int y, x;

        int margin = 10;

        int[] rgba;
        double luminance;

        for (ClassData data: this.classes) {
            if (data == null)
                continue;

            middle = Pojo.getMiddleTime(data.getStartTime(), data.getEndTime());
            paint.getTextBounds(data.getName(), 0, data.getName().length(), bounds);

            while (true) {
                paint.setTextSize(fontSize);
                paint.getTextBounds(data.getName(), 0, data.getName().length(), bounds);

                if (size[0] - 2 * margin >= bounds.width()) {
                    x = (size[0] - bounds.width()) / 2 - margin;
                    break;
                } else
                    fontSize -= 1;
            }

            paint.setTextSize(fontSize);

            // Counting the perceptive luminance - human eye favors green color...
            rgba = Pojo.colorToRGBA(data.getColor());
            luminance = (0.299 * rgba[0] + 0.587 * rgba[1] + 0.114 * rgba[2]) / 255;

            if (luminance > 0.58)
                paint.setColor(Color.BLACK);
            else
                paint.setColor(Color.WHITE);

            elapsed = Pojo.getElapsedTime(this.starts, middle);
            y = (int)(size[1] / 60.0 * (float) (elapsed / 60000.0));

            // if it isn't a correct cell, the text will not renderer, so...
            canvas.drawText(data.getName(), x, (bounds.height()) / 2 + y, paint);
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

    public ClassData[] getClasses() {
        return this.classes;
    }

    public void setClasses(ClassData[] classes) {
        if (classes == null) {
            this.classes = null;
            return;
        }

        this.classes = classes.clone();
        int max = this.classes.length;

        for (int i = 0; i < max; i++) {
            if (this.classes[i] == null || !isContained(this.classes[i])) {
                this.removeClassAt(i);
                max -= 1;
                i -= 1;
            }
        }
    }

    public boolean isContained(@Nullable ClassData data) {
        return data != null &&
               Pojo.getElapsedTime(data.getEndTime(), this.getStartTime()) < 0 &&  // The class ends before this cell
               Pojo.getElapsedTime(this.getEndTime(), data.getStartTime()) < 0;    // The class starts before this cell's end
    }

    public void removeClassAt(int index) {
        this.classes = Pojo.removeValueAt(this.classes, index);
    }

    public void removeClass(ClassData data) {
        while (Pojo.contains(this.classes, data)) {
            this.classes = Pojo.removeValue(this.classes, data);
        }
    }

    public void addClass(ClassData data) {
        if (!isContained(data))
            return;

        if (this.classes != null) {
            ClassData[] _classes = new ClassData[this.classes.length + 1];
            for (int i = 0; i < this.classes.length; i++) {
                _classes[i] = this.classes[i];
            }

            _classes[_classes.length - 1] = data;
            this.classes = _classes.clone();
        } else
            this.classes = new ClassData[]{ data };

        //invalidate();
    }

    public void setSize(int size) {
        this.size[0] = size;
        this.size[1] = size / 2;

        int border = 2;
        bgRect = new Rect(0, 0, this.size[0], this.size[1]);
        fgRect = new Rect(border, border, this.size[0] - border, this.size[1] - border);

        rect.right = size;
    }

    public ClassData getDataAtPoint(float y) {
        if (classes == null) return null;

        int minute = (int)(y * 60 / size[1]);
        String time = this.starts + ":";
        if (minute < 10)
            time += "0" + minute;
        else
            time += minute;

        for (ClassData data: classes) {
            if (Pojo.getElapsedTime(data.getStartTime(), time) > 0 &&
                Pojo.getElapsedTime(data.getEndTime(), time) < 0)
                return data;
        }

        return null;
    }

    public int getMinuteLine() {
        return this.minuteLine;
    }

    public void setMinuteLine(int minute) {
        this.minuteLine = minute;
        this.showMinuteLine = (this.minuteLine > 0 || this.minuteLine < 60);
        invalidate();
    }

    public boolean getShowMinuteLine() {
        return this.showMinuteLine;
    }

    public void hideMinuteLine() {
        this.showMinuteLine = false;
        this.minuteLine = -1;
        invalidate();
    }

    @Override
    public boolean performClick() {
        // TODO: I don't know if I should do something else right here...
        return super.performClick();
    }
}
