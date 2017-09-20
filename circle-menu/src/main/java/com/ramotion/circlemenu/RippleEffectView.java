package com.ramotion.circlemenu;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class RippleEffectView extends View {

    private float mSize;
    private Paint mCirclePaint;
    private Paint mHolePaint;

    private float mRadius = 0f;
    private float mRadiusDiff = 200f;

    public RippleEffectView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public RippleEffectView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RippleEffectView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        setLayerType(LAYER_TYPE_HARDWARE, null);

        mHolePaint = new Paint();
        mHolePaint.setColor(Color.RED);
        mHolePaint.setStyle(Paint.Style.FILL);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(Color.WHITE);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mSize = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float center = mSize / 2;

        final float circleRadius = Math.min(center, mRadius);
        final float holeRadius = Math.min(center + mRadiusDiff, mRadius);

        canvas.drawCircle(center, center, holeRadius - mRadiusDiff, mHolePaint); // hole
        canvas.drawCircle(center, center, circleRadius, mCirclePaint); // circle
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        mRadius = radius;
        invalidate();
    }

    public float getRadiusDiff() {
        return mRadiusDiff;
    }

    public void setRadiusDiff(float diff) {
        mRadiusDiff = diff;
    }

}
