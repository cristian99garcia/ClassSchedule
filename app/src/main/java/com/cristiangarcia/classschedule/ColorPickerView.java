package com.cristiangarcia.classschedule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class ColorPickerView extends View {

    private int selectedColor = 0;
    private int[] size = new int[] { 0, 0 };
    private int[] cursor = new int[] { 0, 0 };
    private boolean drawCursor = false;

    private int circleX;
    private int circleY;
    private int circleR;
    private int littleCircleR;
    int margin = 50;
    int lineWidth = 40;

    private boolean pendingLoadColor = true;
    boolean ignoreMoves = false;

    private int[] colors = {
            0xFFFF0000,  // Red
            0xFFFF00FF,  // Violet
            0xFF0000FF,  // Blue
            0xFF00FFFF,  // Sky blue
            0xFF00FF00,  // Green
            0xFFFFFF00,  // Yellow
            0xFFFF0000,  // Red
    };

    private Paint paint1 = new Paint();
    private Paint paint2 = new Paint();

    private Shader shader;
    Bitmap bitmap;

    public ColorPickerView(Context context) {
        super(context);

        paint1.setStyle(Paint.Style.FILL);

        this.setDrawingCacheEnabled(true);
    }

    private void configure() {
        size[0] = getWidth();
        size[1] = getHeight();

        int s = size[0] / 2;
        if (size[1] / 2 < size[0] / 2)
            s = size[1] / 2;

        circleX = size[0] / 2;
        circleY = s + margin / 2;
        circleR = s - margin * 2;

        littleCircleR = circleR / 5 * 2;

        shader = new SweepGradient(circleX, circleY, colors, null);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (size[0] == size[1] && size[1] == 0) {
            configure();
        }

        paint2.setShader(shader);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(lineWidth);
        canvas.drawCircle(circleX, circleY, circleR, paint2);

        if (bitmap == null)
            bitmap = this.getDrawingCache(true);

        if (pendingLoadColor) {
            loadColor(cursor[0], cursor[1]);
            pendingLoadColor = false;
        }

        if (drawCursor) {
            paint1.setColor(Color.WHITE);
            canvas.drawCircle(cursor[0], cursor[1], margin + 10, paint1);

            paint1.setColor(selectedColor);
            canvas.drawCircle(cursor[0], cursor[1], margin, paint1);
        }

        paint1.setColor(selectedColor);
        canvas.drawCircle(circleX, circleY, littleCircleR, paint1);
    }

    @Override
    public boolean performClick() {
        // I really don't know what to do here
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE) {
            if (action == MotionEvent.ACTION_UP) {
                ignoreMoves = false;  // reset
            }

            performClick();
            return true;
        }

        // Circle inequality:
        //   P(x, y) belongs to C if:
        //   (x - Cx)^2 + (y - Cy)^2 <= r^2
        int x = (int)event.getX();
        int y = (int)event.getY();

        double sqrt = Math.sqrt(Math.pow(x - circleX, 2) + Math.pow(y - circleY, 2));
        boolean onRing = !(action == MotionEvent.ACTION_DOWN &&
                (sqrt > circleR + lineWidth || sqrt < littleCircleR));

        if (!onRing) {
            // Ignore future MotionEvent.ACTION_MOVE
            ignoreMoves = true;
            return false;  // Important!
        } else {
            // find the line equation between P and (Cx, Cy) and after that find the intersections
            // between that line and the outline circumference
            //
            // r: y - y1 = (y2 - y1) / (x2 - x1)
            // C: (x - Cx)^2 + (y - Cy)^2 = r^2
            //
            // In a fastest way:
            // V = (P - C) => Answer = C + V / |V| * R;
            // where V is the closest point that belongs to the circumference

            ignoreMoves = false;
            drawCursor = true;

            loadColor(x, y);

            invalidate();
        }

        return true;
    }

    public int getSelectedColor() {
        return this.selectedColor;
    }

    public void setSelectedColor(int color) {
        this.selectedColor = color;
        this.drawCursor = false;
    }

    private void loadColor(int x, int y) {
        int Vx = x - circleX;
        int Vy = y - circleY;

        do {
            Vx += 1;
            this.cursor[0] = (int) (circleX + Vx * circleR / Math.sqrt(Math.pow(Vx, 2) + Math.pow(Vy, 2)));
            this.cursor[1] = (int) (circleY + Vy * circleR / Math.sqrt(Math.pow(Vy, 2) + Math.pow(Vx, 2)));

            selectedColor = bitmap.getPixel(cursor[0], cursor[1]);
        } while (selectedColor == Color.BLACK || selectedColor == Color.WHITE);
    }

    public int[] getCursor() {
        return cursor;
    }

    public void setCursor(int[] cursor) {
        this.cursor = cursor;
        drawCursor = true;

        if (bitmap == null)
            pendingLoadColor = true;
    }
}
