package com.ramotion.circlemenu;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;


public class CircleMenuView extends FrameLayout implements View.OnClickListener {

    private FloatingActionButton mMenuButton;
    private RippleEffectView mRippleView;
    private RingEffectView mRingView;

    private boolean mClosedState = true;

    private final List<View> mButtons = new ArrayList<>();

    public CircleMenuView(@NonNull Context context) {
        super(context);
        init(context, null, null, null);
    }

    public CircleMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, null, null);
    }

    public CircleMenuView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, null, null);
    }

    public CircleMenuView(@NonNull Context context, @NonNull List<Integer> icons, @NonNull List<Integer> colors) {
        super(context);
        init(context, null, icons, colors);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs,
                      @Nullable List<Integer> icons, @Nullable List<Integer> colors)
    {
        LayoutInflater.from(context).inflate(R.layout.circle_menu, this, true);

        setClipChildren(false);
        setClipToPadding(false);

        mMenuButton = findViewById(R.id.circle_menu_main_button);
        mRippleView = findViewById(R.id.circle_menu_ripple);

        mMenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final int duration = 500;

                if (mClosedState) {
                    startOpenMenuAnimation(duration);
                } else {
                    startCloseMenuAnimation(duration);
                }
            }
        });

        if (icons == null && colors == null && attrs != null) {
            final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleMenuView, 0, 0);

            final int iconArrayId = a.getResourceId(R.styleable.CircleMenuView_button_icons, 0);
            final int colorArrayId = a.getResourceId(R.styleable.CircleMenuView_button_colors, 0);

            final TypedArray iconsIds = getResources().obtainTypedArray(iconArrayId);
            final int[] colorsIds = getResources().getIntArray(colorArrayId);

            final int buttonsCount = Math.min(iconsIds.length(), colorsIds.length);

            icons = new ArrayList<>(buttonsCount);
            colors = new ArrayList<>(buttonsCount);

            for (int i = 0; i < buttonsCount; i++) {
                icons.add(iconsIds.getResourceId(i, -1));
                colors.add(colorsIds[i]);
            }

            a.recycle();
            iconsIds.recycle();
        }

        if (icons == null || colors == null) {
            // TODO: write more correctly
            throw new IllegalArgumentException("No icons for buttons set");
        }

        final int buttonsCount = Math.min(icons.size(), colors.size());
        for (int i = 0; i < buttonsCount; i++) {
            final FloatingActionButton button = new FloatingActionButton(context);
            button.setImageResource(icons.get(i));
            button.setBackgroundTintList(ColorStateList.valueOf(colors.get(i)));
            button.setClickable(true);
            button.setOnClickListener(this);
            button.setScaleX(0);
            button.setScaleY(0);
            button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            addView(button);
            mButtons.add(button);
        }

        final float density = context.getResources().getDisplayMetrics().density;
        final int buttonSize = (int) (density * 56);
        final int size = (int) (buttonSize + buttonSize * 2f);

        mRingView = new RingEffectView(context);
        mRingView.setStrokeColor(Color.RED);
        mRingView.setStrokeWidth(buttonSize);
        mRingView.setLayoutParams(new LayoutParams(size, size));
        addView(mRingView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        final float x = mMenuButton.getX();
        final float y = mMenuButton.getY();

        for (View button: mButtons) {
            button.setX(x);
            button.setY(y);
        }

        mRingView.setX(getWidth() / 2 - mRingView.getWidth() / 2);
        mRingView.setY(getHeight() / 2 - mRingView.getHeight() / 2);
    }

    @Override
    public void onClick(View view) {
        final int buttonNumber = mButtons.indexOf(view) + 1;
        final int stepAngle = 360 / mButtons.size();
        final int startAngle = -90 - stepAngle + stepAngle * buttonNumber;

        getRippleAnimation(view, 500).start();
        getRingAnimation(startAngle, 500).start();
    }

    // TODO: add size parameter
    private ObjectAnimator getRippleAnimation(@NonNull View centerView, int duration) {
        final float centreX = centerView.getX() + centerView.getWidth()  / 2;
        final float centreY = centerView.getY() + centerView.getHeight() / 2;

        mRippleView.setX(centreX - mRippleView.getWidth() / 2);
        mRippleView.setY(centreY - mRippleView.getHeight() / 2);
        mRippleView.setRadius(0);

        final ObjectAnimator animation = ObjectAnimator.ofFloat(mRippleView, "radius", mRippleView.getWidth() * 0.4f);
        // TODO: add fade out animation
        animation.setDuration(duration);
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRippleView.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mRippleView.setVisibility(View.INVISIBLE);
            }
        });
        return animation;
    }

    private AnimatorSet getRingAnimation(float startAngle, int duration) {
        mRingView.setStartAngle(startAngle);
        mRingView.setAngle(0);
        mRingView.setAlpha(1f);
        mRingView.setScaleX(1f);
        mRingView.setScaleY(1f);

        final ObjectAnimator angle = ObjectAnimator.ofFloat(mRingView, "angle", 360);
        final ObjectAnimator scaleX = ObjectAnimator.ofFloat(mRingView, "scaleX", 1f, 1.3f);
        final ObjectAnimator scaleY = ObjectAnimator.ofFloat(mRingView, "scaleY", 1f, 1.3f);
        final ObjectAnimator visible = ObjectAnimator.ofFloat(mRingView, "alpha", 1f, 0f);

        final AnimatorSet set = new AnimatorSet();
        set.play(scaleX).with(scaleY).with(visible).after(angle);
        set.setDuration(duration);
        return set;
    }

    private void startOpenMenuAnimation(int duration) {
        mClosedState = false;

        final ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(mMenuButton, "alpha", 0.3f);
        alphaAnimation.setDuration(duration / 2);
        alphaAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMenuButton.setImageResource(R.drawable.ic_close_black_24dp);
            }
        });

        final Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
        final Keyframe kf1 = Keyframe.ofFloat(0.5f, 30f);
        final Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
        final PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
        final ObjectAnimator rotateAnimation = ObjectAnimator.ofPropertyValuesHolder(mMenuButton, pvhRotation);
        rotateAnimation.setDuration(duration / 2);

//        final int radius = mRippleView.getWidth() / 2 + mMenuButton.getWidth() / 2;
        final float radius = mMenuButton.getWidth() * 1.5f;
        final float centerX = mMenuButton.getX();
        final float centerY = mMenuButton.getY();

        final int buttonsCount = mButtons.size();
        final float angleStep = 360f / buttonsCount;

        final ValueAnimator buttonsAnimation = ValueAnimator.ofFloat(0f, radius);
        buttonsAnimation.setDuration(duration);
        buttonsAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float fraction = valueAnimator.getAnimatedFraction();
                final float value = (float)valueAnimator.getAnimatedValue();

                for (int i = 0; i < buttonsCount; i++) {
                    final float angle = angleStep * i - 90;
                    final float x = (float) Math.cos(Math.toRadians(angle)) * value;
                    final float y = (float) Math.sin(Math.toRadians(angle)) * value;

                    final View button = mButtons.get(i);
                    button.setX(centerX + x);
                    button.setY(centerY + y);
                    button.setScaleX(fraction);
                    button.setScaleY(fraction);
                }
            }
        });

        final ObjectAnimator rippleAnimation = getRippleAnimation(mMenuButton, duration);

        final AnimatorSet set1 = new AnimatorSet();
        set1.play(alphaAnimation).before(rotateAnimation);

        final AnimatorSet set = new AnimatorSet();
        set.playTogether(set1, rippleAnimation, buttonsAnimation);
        set.setDuration(duration);
        set.start();
    }

    private void startCloseMenuAnimation(int duration) {
        mClosedState = true;

        mMenuButton.setImageResource(R.drawable.ic_menu_black_24dp);

        final ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(mMenuButton, "alpha", 1f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.start();
    }

}
