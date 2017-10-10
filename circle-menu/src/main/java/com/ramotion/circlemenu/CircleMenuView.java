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
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;


public class CircleMenuView extends FrameLayout implements View.OnClickListener {

    private FloatingActionButton mMenuButton;
    private RingEffectView mRingView;

    private boolean mClosedState = true;
    private boolean mIsAnimating = false;

    private int mIconMenu;
    private int mIconClose;
    private int mDurationRing;
    private int mDurationOpen;
    private int mDurationClose;
    private float mDistance;

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
        // TODO: add setter
        final int menuButtonColor;

        if (attrs != null) {
            final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleMenuView, 0, 0);
            try {
                if (icons == null && colors == null) {
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
                }

                mIconMenu = a.getResourceId(R.styleable.CircleMenuView_icon_menu, R.drawable.ic_menu_black_24dp);
                mIconClose = a.getResourceId(R.styleable.CircleMenuView_icon_close, R.drawable.ic_close_black_24dp);

                mDurationRing = a.getInteger(R.styleable.CircleMenuView_duration_ring, getResources().getInteger(android.R.integer.config_mediumAnimTime));
                mDurationOpen = a.getInteger(R.styleable.CircleMenuView_duration_open, getResources().getInteger(android.R.integer.config_mediumAnimTime));
                mDurationClose = a.getInteger(R.styleable.CircleMenuView_duration_close, getResources().getInteger(android.R.integer.config_mediumAnimTime));

                mDistance = a.getDimension(R.styleable.CircleMenuView_distance, -1);

                menuButtonColor = a.getColor(R.styleable.CircleMenuView_icon_color, Color.WHITE);
            } finally {
                a.recycle();
            }
        } else {
            mIconMenu = R.drawable.ic_menu_black_24dp;
            mIconClose = R.drawable.ic_close_black_24dp;

            mDurationRing = getResources().getInteger(android.R.integer.config_mediumAnimTime);
            mDurationOpen = getResources().getInteger(android.R.integer.config_mediumAnimTime);
            mDurationClose = getResources().getInteger(android.R.integer.config_mediumAnimTime);

            mDistance = -1;

            menuButtonColor = Color.WHITE;
        }

        if (icons == null || colors == null) {
            throw new IllegalArgumentException("No buttons icons or colors set");
        }

        LayoutInflater.from(context).inflate(R.layout.circle_menu, this, true);

        setClipChildren(false);
        setClipToPadding(false);

        mMenuButton = findViewById(R.id.circle_menu_main_button);
        mMenuButton.setImageResource(mIconMenu);
        mMenuButton.setBackgroundTintList(ColorStateList.valueOf(menuButtonColor));
        mMenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsAnimating) {
                    return;
                }

                final Animator animation = mClosedState ? getOpenMenuAnimation() : getCloseMenuAnimation();
                animation.setDuration(mClosedState ? mDurationClose : mDurationOpen);
                animation.start();
            }
        });

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

        mRingView = new RingEffectView(context);
        mRingView.setStrokeColor(Color.RED);
        addView(mRingView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!changed && mIsAnimating) {
            return;
        }

        final float x = mMenuButton.getX();
        final float y = mMenuButton.getY();

        for (View button: mButtons) {
            button.setX(x);
            button.setY(y);
        }

        final Rect buttonRect = new Rect();
        mMenuButton.getContentRect(buttonRect);

        mRingView.setX(getWidth() / 2 - mRingView.getWidth() / 2);
        mRingView.setY(getHeight() / 2 - mRingView.getHeight() / 2);
        mRingView.setStrokeWidth(buttonRect.width());

        if (mDistance == -1) {
            mDistance = buttonRect.width() * 1.5f;
        }

        final LayoutParams lp = (LayoutParams) mRingView.getLayoutParams();
        lp.width = (int) mDistance * 2;
        lp.height = (int) mDistance * 2;
        mRingView.setLayoutParams(lp);
    }

    @Override
    public void onClick(View view) {
        if (mIsAnimating) {
            return;
        }

        final Animator click = getButtonClickAnimation((FloatingActionButton)view);
        click.setDuration(mDurationRing);
        click.start();
    }

    private Animator getButtonClickAnimation(final @NonNull FloatingActionButton button) {
        final int buttonNumber = mButtons.indexOf(button) + 1;
        final int stepAngle = 360 / mButtons.size();
        final int startAngle = -90 - stepAngle + stepAngle * buttonNumber;

        final Rect buttonRect = new Rect();
        mMenuButton.getContentRect(buttonRect);

        final float x = (float) Math.cos(Math.toRadians(startAngle)) * mDistance;
        final float y = (float) Math.sin(Math.toRadians(startAngle)) * mDistance;

        button.setPivotX(button.getPivotX() - x);
        button.setPivotY(button.getPivotY() - y);

        final ObjectAnimator rotateButton = ObjectAnimator.ofFloat(button, "rotation", 0f, 360f);
        rotateButton.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                button.setPivotX(buttonRect.width() / 2);
                button.setPivotY(buttonRect.height() / 2);
            }
        });

        final float elevation = mMenuButton.getCompatElevation();

        mRingView.setStartAngle(startAngle);
        mRingView.setAngle(0);
        mRingView.setAlpha(1f);
        mRingView.setScaleX(1f);
        mRingView.setScaleY(1f);
        mRingView.setStrokeColor(button.getBackgroundTintList().getDefaultColor());

        final ObjectAnimator angle = ObjectAnimator.ofFloat(mRingView, "angle", 360);
        final ObjectAnimator scaleX = ObjectAnimator.ofFloat(mRingView, "scaleX", 1f, 1.3f);
        final ObjectAnimator scaleY = ObjectAnimator.ofFloat(mRingView, "scaleY", 1f, 1.3f);
        final ObjectAnimator visible = ObjectAnimator.ofFloat(mRingView, "alpha", 1f, 0f);

        final AnimatorSet lastSet = new AnimatorSet();
        lastSet.playTogether(scaleX, scaleY, visible, getCloseMenuAnimation());

        final AnimatorSet firstSet = new AnimatorSet();
        firstSet.playTogether(rotateButton, angle);
        firstSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    bringChildToFront(mRingView);
                    bringChildToFront(button);
                } else {
                    button.setCompatElevation(elevation + 2);
                    ViewCompat.setZ(mRingView, elevation + 1);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                button.setCompatElevation(elevation);
                ViewCompat.setZ(mRingView, elevation);
            }
        });

        final AnimatorSet result = new AnimatorSet();
        result.play(firstSet).before(lastSet);
        result.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
            }
        });

        return result;
    }

    private Animator getOpenMenuAnimation() {
        mClosedState = false;

        final ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(mMenuButton, "alpha", 0.3f);

        final Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
        final Keyframe kf1 = Keyframe.ofFloat(0.5f, 60f);
        final Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
        final PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
        final ObjectAnimator rotateAnimation = ObjectAnimator.ofPropertyValuesHolder(mMenuButton, pvhRotation);
        rotateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (valueAnimator.getAnimatedFraction() == 0.5f) {
                    mMenuButton.setImageResource(mIconClose);
                }
            }
        });

        final float centerX = mMenuButton.getX();
        final float centerY = mMenuButton.getY();

        final int buttonsCount = mButtons.size();
        final float angleStep = 360f / buttonsCount;

        final ValueAnimator buttonsAppear = ValueAnimator.ofFloat(0f, mDistance);
        buttonsAppear.setInterpolator(new OvershootInterpolator());
        buttonsAppear.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (View view: mButtons) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        });
        buttonsAppear.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
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
                    button.setScaleX(1.0f * fraction);
                    button.setScaleY(1.0f * fraction);
                }
            }
        });

        final AnimatorSet result = new AnimatorSet();
        result.playTogether(alphaAnimation, rotateAnimation, buttonsAppear);
        result.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
            }
        });

        return result;
    }

    private Animator getCloseMenuAnimation() {
        mClosedState = true;

        final ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(mMenuButton, "scaleX", 0f);
        final ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(mMenuButton, "scaleY", 0f);
        final ObjectAnimator alpha1 = ObjectAnimator.ofFloat(mMenuButton, "alpha", 0f);
        final AnimatorSet set1 = new AnimatorSet();
        set1.playTogether(scaleX1, scaleY1, alpha1);
        set1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (View view: mButtons) {
                    view.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mMenuButton.setRotation(60f);
                mMenuButton.setImageResource(mIconMenu);
            }
        });

        final ObjectAnimator angle = ObjectAnimator.ofFloat(mMenuButton, "rotation", 0);
        final ObjectAnimator alpha2 = ObjectAnimator.ofFloat(mMenuButton, "alpha", 1f);
        final ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mMenuButton, "scaleX", 1f);
        final ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mMenuButton, "scaleY", 1f);
        final AnimatorSet set2 = new AnimatorSet();
        set2.setInterpolator(new OvershootInterpolator());
        set2.playTogether(angle, alpha2, scaleX2, scaleY2);

        final AnimatorSet result = new AnimatorSet();
        result.play(set1).before(set2);
        result.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
            }
        });
        return result;
    }

    public void setIconMenu(@DrawableRes int iconId) {
        mIconMenu = iconId;
    }

    @DrawableRes
    public int getIconMenu() {
        return mIconMenu;
    }

    public void setIconClose(@DrawableRes int iconId) {
        mIconClose = iconId;
    }

    @DrawableRes
    public int getIconClose() {
        return mIconClose;
    }

    public void setDurationClose(int duration) {
        mDurationClose = duration;
    }

    public int getDurationClose() {
        return mDurationClose;
    }

    public void setDurationOpen(int duration) {
        mDurationOpen = duration;
    }

    public int getDurationOpen() {
        return mDurationOpen;
    }

    public void setDurationRing(int duration) {
        mDurationRing = duration;
    }

    public int getDurationRing() {
        return mDurationRing;
    }

    public void setDistance(float distance) {
        mDistance = distance;
        invalidate();
    }

    public float getDistance() {
        return mDistance;
    }

}
