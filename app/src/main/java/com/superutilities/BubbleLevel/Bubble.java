package com.superutilities.BubbleLevel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Bubble {
    private int x, y, radius;

    public Bubble(int xP, int yP, int r) {
        x = xP;
        y = yP;
        radius = r;
    }

    public void draw(Canvas drawCanvas, Paint drawPaint) {
        drawPaint.setColor(Color.BLUE);
        drawCanvas.drawCircle(x, y, radius, drawPaint);
    }

    public void setY(int newY) {
        y = newY;
    }

    public void setX(int newX) {
        x = newX;
    }

    public void setR(int newR) {
        radius = newR;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }
}
