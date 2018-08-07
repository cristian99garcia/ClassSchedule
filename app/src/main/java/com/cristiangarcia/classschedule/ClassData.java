package com.cristiangarcia.classschedule;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;

public class ClassData {

    private String name;
    private String additionalData;
    private String starts;
    private String ends;
    private int color = Color.RED;
    private String day;

    public ClassData() {}

    public ClassData(String starts, String ends) {
        this.starts = starts;
        this.ends = ends;
    }

    public ClassData(String starts, String ends, String name) {
        this.starts = starts;
        this.ends = ends;
        this.name = name;
    }

    public ClassData(String starts, String ends, String name, int color, String day) {
        this.starts = starts;
        this.ends = ends;
        this.name = name;
        this.color = color;
        this.day = day;
    }

    public ClassData(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public ClassData setName(String name) {
        this.name = name;
        return this;
    }

    public String getAdditionalData() {
        return this.additionalData;
    }

    public ClassData setAdditionalData(String data) {
        this.additionalData = data;
        return this;
    }

    public String getStartTime() {
        return this.starts;
    }

    public ClassData setStartTime(String time) {
        this.starts = time;
        return this;
    }

    public String getEndTime() {
        return this.ends;
    }

    public ClassData setEndTime(String time) {
        this.ends = time;
        return this;
    }

    public int getColor() {
        return color;
    }

    public ClassData setColor(int color) {
        this.color = color;
        return this;
    }

    public ClassData setColor(String color) {
        return this.setColor(Color.parseColor(color));
    }

    public String getDay() {
        return this.day;
    }

    public ClassData setDay(String day) {
        this.day = day;
        return this;
    }

    public String toString() {
        // For debug purposes
        return this.getName() + " (" + this.day + "): " + this.getStartTime() + " - " + this.getEndTime();
    }

    public String toStringFancy() {
        return this.getName() + ": " + this.getStartTime() + " - " + this.getEndTime();
    }

    public boolean equalsTo(@Nullable ClassData data) {
        if (data == null)
            return day == null &&
                    name == null &&
                    additionalData == null &&
                    starts == null &&
                    ends == null;

        return day.equals(data.getDay()) &&
               name.equals(data.getName()) &&
               additionalData.equals(data.getAdditionalData()) &&
               starts.equals(data.getStartTime()) &&
               ends.equals(data.getEndTime()) &&
               color == data.getColor();
    }

    public boolean collide(ClassData data) {
        // if (data.getDay().equals(day)) {
        //     Log.d("COLLIDE CHECK",  "(" + this.starts + "-" + this.ends + ") (" + data.getStartTime() + "-" + data.getEndTime() + ") " + (Pojo.getElapsedTime(starts, data.getStartTime()) > 0 && Pojo.getElapsedTime(data.getEndTime(), ends) > 0) + " " + (Pojo.getElapsedTime(data.getStartTime(), starts) > 0 && Pojo.getElapsedTime(ends, data.getEndTime()) > 0));
        // }

        return day.equals(data.getDay()) &&
                (Pojo.getElapsedTime(starts, data.getStartTime()) > 0 &&
                 Pojo.getElapsedTime(data.getEndTime(), ends) > 0) ||

                (Pojo.getElapsedTime(data.getStartTime(), starts) > 0 &&
                 Pojo.getElapsedTime(ends, data.getEndTime()) > 0);
    }
}
