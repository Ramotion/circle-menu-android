package com.ramotion.circlemenu;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;


public class CircleMenuView extends FrameLayout {

    private FloatingActionButton mMenuButton;
    private RippleEffectView mRippleView;

    private boolean mClosedState = true;

    public CircleMenuView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CircleMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleMenuView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        LayoutInflater.from(context).inflate(R.layout.circle_menu, this, true);

        mMenuButton = findViewById(R.id.circle_menu_main_button);
        mRippleView = findViewById(R.id.circle_menu_ripple);

        mMenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final int duration = 1000;

                if (mClosedState) {
                    startRippleAnimation(view, duration);
                    startOpenMenuAnimation(duration);
                } else {
                    startCloseMenuAnimation(duration);
                }
            }
        });
    }

    private void startRippleAnimation(@NonNull View centerView, int duration) {
        final float centreX = centerView.getX() + centerView.getWidth()  / 2;
        final float centreY = centerView.getY() + centerView.getHeight() / 2;

        mRippleView.setX(centreX - mRippleView.getWidth() / 2);
        mRippleView.setY(centreY - mRippleView.getHeight() / 2);
        mRippleView.setRadius(0);

        final ObjectAnimator animation = ObjectAnimator.ofFloat(mRippleView, "radius", mRippleView.getWidth());
        animation.setDuration(duration);
        animation.start();
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

        final AnimatorSet set = new AnimatorSet();
        set.play(rotateAnimation).after(alphaAnimation);
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
