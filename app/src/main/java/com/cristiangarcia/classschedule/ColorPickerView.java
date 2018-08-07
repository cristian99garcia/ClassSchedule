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
    private int margin = 50;

    private boolean searchColor = true;
    private boolean ignoreMoves = false;

    private int[] colors = {
            0xFFFF0000,  // Red
            0xFFFF00FF,  // Violet
            0xFF0000FF,  // Blue
            0xFF00FFFF,  // Sky blue
            0xFF00FF00,  // Green
            0xFFFFFF00,  // Yellow
            0xFFFF0000,  // Red
    };

    Paint paint1 = new Paint();
    Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    Path circle = new Path();

    SweepGradient gradient;
    RadialGradient rGradient;

    private Bitmap bitmap;

    public ColorPickerView(Context context) {
        super(context);

        paint1.setStyle(Paint.Style.FILL);

        this.setDrawingCacheEnabled(true);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (size[0] == size[1] && size[1] == 0) {
            size[0] = getWidth();
            size[1] = getHeight();

            int s = size[0] / 2;
            if (size[1] / 2 < size[0] / 2) s = size[1] / 2;

            circleX = size[0] / 2;
            circleY = s + margin / 2;
            circleR = s - margin * 2;
            circle.addCircle(circleX, circleY, circleR, Path.Direction.CW);
            circle.close();

            float p = 1f / (colors.length - 1);
            float[] positions = new float[colors.length];

            for (int i=0; i<colors.length; i++)
                positions[i] = i * p;

            gradient = new SweepGradient(circleX, circleY, colors, positions);
            rGradient = new RadialGradient(circleX, circleY, circleR * 3 / 4, 0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP);
        };

        paint2.setShader(gradient);
        canvas.drawPath(circle, paint2);

        paint2.setShader(rGradient);
        canvas.drawPath(circle, paint2);

        if (drawCursor) {
            paint1.setColor(Color.BLACK);
            canvas.drawCircle(cursor[0], cursor[1], margin + 10, paint1);

            if (searchColor) {
                bitmap = this.getDrawingCache(true);

                // FIXME: En una zona en específico, a veces se selecciona el color negro, no sé
                // por qué, pero supongo que se debe a la divisón entre colores.
                // No he podido reproducir el bug en dos zonas distintas en la misma instancia, hasta
                // ahora ha aparecido en el amarillo y en el violeta.
                selectedColor = bitmap.getPixel(cursor[0], cursor[1]);
            } else {
                searchColor = true;  // Reset for the next time
            }

            paint1.setColor(selectedColor);
            canvas.drawCircle(cursor[0], cursor[1], margin, paint1);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE) {
            if (action == MotionEvent.ACTION_UP) {
                ignoreMoves = false;  // reset
            }

            return true;
        }

        // Circle inequality:
        //   P(x, y) belongs to C if:
        //   (x - Cx)^2 + (y - Cy)^2 <= r^2
        int x = (int)event.getX();
        int y = (int)event.getY();

        if (Math.sqrt(Math.pow(x - circleX, 2) + Math.pow(y - circleY, 2)) <= circleR) {
            // P(x, y) belongs to C
            cursor[0] = x;
            cursor[1] = y;
            drawCursor = true;

            invalidate();
        } else {
            if (action == MotionEvent.ACTION_DOWN) {
                // Ignore future MotionEvent.ACTION_MOVE
                ignoreMoves = true;
                return false;  // Important!
            }

            // find the line equation between P and (Cx, Cy) and after that find the intersections
            // between that line and the outline circumference
            //
            // r: y - y1 = (y2 - y1) / (x2 - x1)
            // C: (x - Cx)^2 + (y - Cy)^2 = r^2
            //
            // In a fastest way:
            // V = (P - C) => Answer = C + V / |V| * R;

            int Vx = x - circleX;
            int Vy = y - circleY;

            // -2 because I don't want the point on the circumference, I want the point on the circle
            this.cursor[0] = (int)(circleX + Vx * (circleR - 2) / Math.sqrt(Math.pow(Vx, 2) + Math.pow(Vy, 2)));
            this.cursor[1] = (int)(circleY + Vy * (circleR - 2) / Math.sqrt(Math.pow(Vy, 2) + Math.pow(Vx, 2)));

            invalidate();
        }

        return true;
    }

    public int getSelectedColor() {
        return this.selectedColor;
    }

    public void setSelectedColor(int color) {
        this.selectedColor = color;
        this.searchColor = false;
        this.drawCursor = false;
    }
}
