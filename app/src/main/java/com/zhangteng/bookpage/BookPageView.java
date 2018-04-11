package com.zhangteng.bookpage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by swing on 2018/4/11.
 */
public class BookPageView extends View {
    public static final String STYLE_TOP_RIGHT = "STYLE_TOP_RIGHT";//f点在右上角
    public static final String STYLE_LOWER_RIGHT = "STYLE_LOWER_RIGHT";//f点在右下角
    public String currentState = STYLE_LOWER_RIGHT;

    private int mWidth;
    private int mHeight;

    private Paint pathAPaint;
    private Paint pathBPaint;
    private Paint pathCPaint;

    private BookPagePoint a;
    private BookPagePoint f, g, e, h, c, j, b, k, d, i;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Path pathA, pathB, pathC;
    private Runnable runnable;
    private int multiple = 0;

    public BookPageView(Context context) {
        super(context);
        initPoint();
        initPaint();
    }

    public BookPageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPoint();
        initPaint();
    }

    public BookPageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPoint();
        initPaint();
    }

    private void initPoint() {
        a = new BookPagePoint();
        f = new BookPagePoint();
        g = new BookPagePoint();
        e = new BookPagePoint();
        h = new BookPagePoint();
        c = new BookPagePoint();
        j = new BookPagePoint();
        b = new BookPagePoint();
        k = new BookPagePoint();
        d = new BookPagePoint();
        i = new BookPagePoint();
    }

    private void initPaint() {
        pathAPaint = new Paint();
        pathAPaint.setColor(getResources().getColor(R.color.colorPrimary));
        pathAPaint.setAntiAlias(true);
        pathCPaint = new Paint();
        pathCPaint.setColor(getResources().getColor(R.color.colorAccent));
        pathCPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        pathCPaint.setAntiAlias(true);
        pathBPaint = new Paint();
        pathBPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        pathBPaint.setAntiAlias(true);
        pathBPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        pathA = new Path();
        pathC = new Path();
        pathB = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
        a.x = mWidth + 1;
        a.y = mHeight + 1;
        f.x = mWidth;
        f.y = mHeight;
        calcPointsXY(a, f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        bitmap = Bitmap.createBitmap((int) mWidth, (int) mHeight, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        if (a.x == mWidth + 1 && a.y == mHeight + 1) {
            bitmapCanvas.drawPath(getPathDefault(), pathAPaint);
        } else {
            if (f.x == mWidth && f.y == 0) {
                bitmapCanvas.drawPath(getPathAFromTopRight(), pathAPaint);
            } else if (f.x == mWidth && f.y == mHeight) {
                bitmapCanvas.drawPath(getPathAFromLowerRight(), pathAPaint);
            }

            bitmapCanvas.drawPath(getPathC(), pathCPaint);
            bitmapCanvas.drawPath(getPathB(), pathBPaint);
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getY() < getmHeight() / 2) {//从上半部分翻页
                    currentState = STYLE_TOP_RIGHT;
                    setTouchPoint(event.getX(), event.getY(), STYLE_TOP_RIGHT);
                } else if (event.getY() >= getmHeight() / 2) {//从下半部分翻页
                    currentState = STYLE_LOWER_RIGHT;
                    setTouchPoint(event.getX(), event.getY(), STYLE_LOWER_RIGHT);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                setTouchPoint(event.getX(), event.getY(), "");
                break;
            case MotionEvent.ACTION_UP:
                if (runnable != null) {
                    removeCallbacks(runnable);
                }
                setDefaultPath(currentState);//回到默认状态
                break;
        }
        return true;
    }

    /**
     * 设置触摸点
     *
     * @param x
     * @param y
     * @param style
     */
    public void setTouchPoint(float x, float y, String style) {
        switch (style) {
            case STYLE_TOP_RIGHT:
                f.x = mWidth;
                f.y = 0;
                break;
            case STYLE_LOWER_RIGHT:
                f.x = mWidth;
                f.y = mHeight;
                break;
            default:
                break;
        }
        a.x = x;
        a.y = y;
        calcPointsXY(a, f);
        postInvalidate();
    }

    public int getmWidth() {
        return mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }

    /**
     * 计算各点坐标
     *
     * @param a
     * @param f
     */
    private void calcPointsXY(BookPagePoint a, BookPagePoint f) {
        g.x = (a.x + f.x) / 2;
        g.y = (a.y + f.y) / 2;

        e.x = g.x - (f.y - g.y) * (f.y - g.y) / (f.x - g.x);
        e.y = f.y;

        h.x = f.x;
        h.y = g.y - (f.x - g.x) * (f.x - g.x) / (f.y - g.y);

        c.x = e.x - (f.x - e.x) / 2;
        c.y = f.y;

        j.x = f.x;
        j.y = h.y - (f.y - h.y) / 2;

        b = getIntersectionPoint(a, e, c, j);
        k = getIntersectionPoint(a, h, c, j);

        d.x = (c.x + 2 * e.x + b.x) / 4;
        d.y = (2 * e.y + c.y + b.y) / 4;

        i.x = (j.x + 2 * h.x + k.x) / 4;
        i.y = (2 * h.y + j.y + k.y) / 4;
    }

    /**
     * 计算两线段相交点坐标
     *
     * @param lineOne_My_pointOne
     * @param lineOne_My_pointTwo
     * @param lineTwo_My_pointOne
     * @param lineTwo_My_pointTwo
     * @return 返回该点
     */
    private BookPagePoint getIntersectionPoint(BookPagePoint lineOne_My_pointOne, BookPagePoint lineOne_My_pointTwo, BookPagePoint lineTwo_My_pointOne, BookPagePoint lineTwo_My_pointTwo) {
        float x1, y1, x2, y2, x3, y3, x4, y4;
        x1 = lineOne_My_pointOne.x;
        y1 = lineOne_My_pointOne.y;
        x2 = lineOne_My_pointTwo.x;
        y2 = lineOne_My_pointTwo.y;
        x3 = lineTwo_My_pointOne.x;
        y3 = lineTwo_My_pointOne.y;
        x4 = lineTwo_My_pointTwo.x;
        y4 = lineTwo_My_pointTwo.y;

        float pointX = ((x1 - x2) * (x3 * y4 - x4 * y3) - (x3 - x4) * (x1 * y2 - x2 * y1))
                / ((x3 - x4) * (y1 - y2) - (x1 - x2) * (y3 - y4));
        float pointY = ((y1 - y2) * (x3 * y4 - x4 * y3) - (x1 * y2 - x2 * y1) * (y3 - y4))
                / ((y1 - y2) * (x3 - x4) - (x1 - x2) * (y3 - y4));

        return new BookPagePoint(pointX, pointY);
    }

    /**
     * 回到默认状态
     */
    public void setDefaultPath(final String style) {
        runnable = new Runnable() {
            @Override
            public void run() {
                a.x = a.x - 100;
                if (style == STYLE_TOP_RIGHT) {
                    a.y = a.y + 100;
                } else if (style == STYLE_LOWER_RIGHT) {
                    a.y = a.y - 100;
                }
                setTouchPoint(a.x, a.y, "");
                if (Math.abs(a.x) > mWidth * 2 || Math.abs(a.y) > mHeight * 2) {
                    a.x = mWidth + 1;
                    a.y = mHeight + 1;
                    postInvalidate();
                } else {
                    postDelayed(runnable, 1);
                }
            }
        };
        postDelayed(runnable, 1);
    }

    /**
     * 绘制默认的界面
     *
     * @return
     */
    private Path getPathDefault() {
        pathA.reset();
        pathA.lineTo(0, mHeight);
        pathA.lineTo(mWidth, mHeight);
        pathA.lineTo(mWidth, 0);
        pathA.close();
        return pathA;
    }

    /**
     * 获取f点在右下角的pathA
     *
     * @return
     */
    private Path getPathAFromLowerRight() {
        pathA.reset();
        pathA.lineTo(0, mHeight);//移动到左下角
        pathA.lineTo(c.x, c.y);//移动到c点
        pathA.quadTo(e.x, e.y, b.x, b.y);//从c到b画贝塞尔曲线，控制点为e
        pathA.lineTo(a.x, a.y);//移动到a点
        pathA.lineTo(k.x, k.y);//移动到k点
        pathA.quadTo(h.x, h.y, j.x, j.y);//从k到j画贝塞尔曲线，控制点为h
        pathA.lineTo(mWidth, 0);//移动到右上角
        pathA.close();//闭合区域
        return pathA;
    }

    /**
     * 获取f点在右上角的pathA
     *
     * @return
     */
    private Path getPathAFromTopRight() {
        pathA.reset();
        pathA.lineTo(c.x, c.y);//移动到c点
        pathA.quadTo(e.x, e.y, b.x, b.y);//从c到b画贝塞尔曲线，控制点为e
        pathA.lineTo(a.x, a.y);//移动到a点
        pathA.lineTo(k.x, k.y);//移动到k点
        pathA.quadTo(h.x, h.y, j.x, j.y);//从k到j画贝塞尔曲线，控制点为h
        pathA.lineTo(mWidth, mHeight);//移动到右下角
        pathA.lineTo(0, mHeight);//移动到左下角
        pathA.close();
        return pathA;
    }

    /**
     * 绘制区域C
     *
     * @return
     */
    private Path getPathC() {
        pathC.reset();
        pathC.moveTo(i.x, i.y);//移动到i点
        pathC.lineTo(d.x, d.y);//移动到d点
        pathC.lineTo(b.x, b.y);//移动到b点
        pathC.lineTo(a.x, a.y);//移动到a点
        pathC.lineTo(k.x, k.y);//移动到k点
        pathC.close();//闭合区域
        return pathC;
    }

    /**
     * 绘制区域B
     *
     * @return
     */
    private Path getPathB() {
        pathB.reset();
        pathB.lineTo(0, mHeight);//移动到左下角
        pathB.lineTo(mWidth, mHeight);//移动到右下角
        pathB.lineTo(mWidth, 0);//移动到右上角
        pathB.close();//闭合区域
        return pathB;
    }
}
