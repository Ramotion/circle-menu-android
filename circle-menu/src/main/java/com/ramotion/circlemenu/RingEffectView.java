package com.ramotion.circlemenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.util.Log;
import android.view.View;


public class RingEffectView extends View {

    private final Paint mPaint;

    private RectF mRingRect;

    private float mAngle;
    private float mStartAngle;

    public RingEffectView(Context context) {
        super(context);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRingRect = new RectF(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(mRingRect, mStartAngle, mAngle, false, mPaint);
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

}
