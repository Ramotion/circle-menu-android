package com.ramotion.circlemenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


public class RingEffectView extends View {

    private final Paint mPaint;

    private RectF mRingRect;

    private float mAngle;
    private float mStartAngle;
    private int mRadius;

    public RingEffectView(Context context) {
        this(context, null);
    }

    public RingEffectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mRingRect != null) {
            canvas.drawArc(mRingRect, mStartAngle, mAngle, false, mPaint);
        }
    }

    @Override
    public void setAlpha(@FloatRange(from = 0.0, to = 1.0) float alpha) {
        mPaint.setAlpha((int)(255 * alpha));
        invalidate();
    }

    @Override
    public float getAlpha() {
        return mPaint.getAlpha() / 255;
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(@FloatRange(from = 0.0, to = 360.0) float angle) {
        mAngle = angle;
        invalidate();
    }

    public float getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(@FloatRange(from = 0.0, to = 360.0) float startAngle) {
        mStartAngle = startAngle;
    }

    public void setStrokeColor(int color) {
        mPaint.setColor(color);
    }

    public void setStrokeWidth(int width) {
        mPaint.setStrokeWidth(width);
    }

    public void setRadius(int radius) {
        mRadius = radius;
        final int w = getMeasuredWidth();
        final int h = getMeasuredHeight();

        final int wo = (w - radius * 2) / 2;
        final int ho = (h - radius * 2) / 2;

        final float sw = mPaint.getStrokeWidth() * 0.5f;

        mRingRect = new RectF(wo + sw, ho + sw, w - wo - sw, h - ho - sw);
    }

    public int getRadius() {
        return mRadius;
    }

}
