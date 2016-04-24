package com.superutilities.BubbleLevel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Board extends View {
    int height, width;
    int cX, cY1, cY2, r;

    Bubble bubble;


    //Paint de dibujar y Paint de Canvas
    static Paint drawPaint; //Tiene los valores de colores y formas con el que se va a pintar
    private Paint canvasPaint;
    //Color Inicial
    private static int paintColor = 0xFFFF0000;
    //canvas para pintar
    private Canvas drawCanvas; //Este es el que dibuja
    //Bitmap para guardar canvas
    private Bitmap canvasBitmap;


    public Board(Context context) {
        super(context);
        initDrawing();

    }

    public Board(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDrawing();

    }

    public Board(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDrawing();

    }

    private void initDrawing() {
        //Configuración del area sobre la que pintar
        drawPaint = new Paint();
        drawPaint.setColor(Color.WHITE);
        drawPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        bubble = new Bubble(10, 50000, 10);
        //drawPath = new Path();
        //setTamanyoPunto(20);
    }


    public Bubble getBubble() {
        return bubble;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        height = View.MeasureSpec.getSize(heightMeasureSpec);
        width = View.MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, height);

        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

        cX = width / 2;
        cY1 = height / 4;
        cY2 = (height * 3) / 4;
        r = width / 10;
        bubble.setR(r);
        bubble.setX(cX);

        invalidate();
    }

    //Pinta la vista
    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        drawBoard();
    }

    /**
     * Draws the level to the screen. Then draws the bubble on top.
     */
    private void drawBoard() {
        //Dibujo el Fondo Negro en Forma de Cuadrado
        drawPaint.setColor(Color.GRAY);
        drawCanvas.drawRect(0, 0, canvasBitmap.getWidth(), canvasBitmap.getHeight(), drawPaint);

        //Dibujo El interior lo que seria el Nivel Azul
        drawPaint.setColor(Color.BLUE);
        drawCanvas.drawRect(cX - r, cY1, cX + r, cY2, drawPaint);//left_side_header_layout, top, right, bottom, paint

        //Dibujo los circulos que estan en el extremo del nivel
        drawPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        drawPaint.setColor(Color.BLUE);
        drawPaint.setAntiAlias(true);
        //drawPaint.setStrokeWidth(20);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawCanvas.drawCircle(cX, cY1, r, drawPaint); //centerX, centerY, radius, paint
        drawCanvas.drawCircle(cX, cY2, r, drawPaint);

        //Dibujo las lineas metricas del nivel
        //drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setColor(Color.BLACK);
        drawPaint.setStrokeWidth(5);
        //drawPaint.setStrokeJoin(Paint.Join.ROUND);
        //drawPaint.setStrokeCap(Paint.Cap.ROUND);


        int xF = getWidth();
        int yF = getHeight();

        drawCanvas.drawLine(cX, cY1, cX, cY2, drawPaint);
        //drawCanvas.drawLine(cX, 0, cX, 0, drawPaint);

        //Dibujar la burbuja del nivel
        bubble.draw(drawCanvas, drawPaint);

        //repintar
        invalidate();
    }

}
