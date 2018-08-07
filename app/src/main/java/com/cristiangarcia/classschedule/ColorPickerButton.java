package com.cristiangarcia.classschedule;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class ColorPickerButton extends View {

    // I got this color list from https://www.materialui.co/colors
    // (Material Design header colors)
    int[] colors = new int[] {
            0xFFF44336,
            0xFFF44336,
            0xFFE91E63,
            0xFF9C27B0,
            0xFF673AB7,
            0xFF3F51B5,
            0xFF2196F3,
            0xFF03A9F4,
            0xFF00BCD4,
            0xFF009688,
            0xFF4CAF50,
            0xFF8BC34A,
            0xFFCDDC39,
            0xFFFFEB3B,
            0xFFFFC107,
            0xFFFF9800,
            0xFFFF5722,
            0xFF795548,
            0xFF9E9E9E,
            0xFF607D8B
    };

    private Random colorGenerator;
    private int selectedColor = 0;

    Rect rect;
    Paint paint = new Paint();

    public ColorPickerButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        colorGenerator = new Random(System.currentTimeMillis());

        selectedColor = getRandomColor();
        paint.setStyle(Paint.Style.FILL);
        rect = new Rect();
    }

    public int getRandomColor() {
        // Really Java? I need to do all these things to get a random integer?
        // random formula: generator.nextInt((max - min) + 1) + min
        int num = colorGenerator.nextInt(colors.length);  // Random number between 0 and classes.length - 1
        return this.colors[num];
    }

    @Override
    public void onDraw(Canvas canvas) {
        // For some reason, Integer.min requires API level 24, so I do it manually
        int size = this.getHeight();
        if (this.getWidth() < size) size = this.getWidth();

        rect.left = 0;
        rect.top = 0;
        if (rect.width() == 0) {
            rect.bottom = size;
            rect.right = size;
        }

        paint.setColor(selectedColor);
        canvas.drawRect(rect, paint);
    }

    public int getSelectedColor() {
        return this.selectedColor;
    }

    public void setSelectedColor(int color) {
        this.selectedColor = color;
        invalidate();
    }
}
