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


/**
 * CircleMenuView
 */
public class CircleMenuView extends FrameLayout {

    private static final int DEFAULT_BUTTON_SIZE = 56;
    private static final float DEFAULT_DISTANCE = DEFAULT_BUTTON_SIZE * 1.5f;
    private static final float DEFAULT_RING_SCALE_RATIO = 1.3f;
    private static final float DEFAULT_CLOSE_ICON_ALPHA = 0.3f;

    private final List<View> mButtons = new ArrayList<>();
    private final Rect mButtonRect = new Rect();

    private FloatingActionButton mMenuButton;
    private RingEffectView mRingView;

    private boolean mClosedState = true;
    private boolean mIsAnimating = false;

    private int mIconMenu;
    private int mIconClose;
    private int mDurationRing;
    private int mLongClickDurationRing;
    private int mDurationOpen;
    private int mDurationClose;
    private int mDesiredSize;
    private int mRingRadius;

    private float mDistance;

    private EventListener mListener;

    /**
     * CircleMenu event listener.
     */
    public static class EventListener {
        /**
         * Invoked on menu button click, before animation start.
         * @param view current CircleMenuView instance.
         */
        public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {}

        /**
         * Invoked on menu button click, after animation end.
         * @param view - current CircleMenuView instance.
         */
        public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {}

        /**
         * Invoked on close menu button click, before animation start.
         * @param view - current CircleMenuView instance.
         */
        public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {}

        /**
         * Invoked on close menu button click, after animation end.
         * @param view - current CircleMenuView instance.
         */
        public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {}

        /**
         * Invoked on button click, before animation start.
         * @param view - current CircleMenuView instance.
         * @param buttonIndex - clicked button zero-based index.
         */
        public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int buttonIndex) {}

        /**
         * Invoked on button click, after animation end.
         * @param view - current CircleMenuView instance.
         * @param buttonIndex - clicked button zero-based index.
         */
        public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int buttonIndex) {}

        /**
         * Invoked on button long click. Invokes {@see onButtonLongClickAnimationStart} and {@see onButtonLongClickAnimationEnd}
         * if returns true.
         * @param view current CircleMenuView instance.
         * @param buttonIndex clicked button zero-based index.
         * @return  true if the callback consumed the long click, false otherwise.
         */
        public boolean onButtonLongClick(@NonNull CircleMenuView view, int buttonIndex) { return false; }

        /**
         * Invoked on button long click, before animation start.
         * @param view - current CircleMenuView instance.
         * @param buttonIndex - clicked button zero-based index.
         */
        public void onButtonLongClickAnimationStart(@NonNull CircleMenuView view, int buttonIndex) {}

        /**
         * Invoked on button long click, after animation end.
         * @param view - current CircleMenuView instance.
         * @param buttonIndex - clicked button zero-based index.
         */
        public void onButtonLongClickAnimationEnd(@NonNull CircleMenuView view, int buttonIndex) {}
    }

    private class OnButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View view) {
            if (mIsAnimating) {
                return;
            }

            final Animator click = getButtonClickAnimation((FloatingActionButton)view);
            click.setDuration(mDurationRing);
            click.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mListener != null) {
                        mListener.onButtonClickAnimationStart(CircleMenuView.this, mButtons.indexOf(view));
                    }
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    mClosedState = true;
                    if (mListener != null) {
                        mListener.onButtonClickAnimationEnd(CircleMenuView.this, mButtons.indexOf(view));
                    }
                }
            });
            click.start();
        }
    }

    private class OnButtonLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(final View view) {
            if (mListener == null) {
                return false;
            }

            final boolean result =  mListener.onButtonLongClick(CircleMenuView.this, mButtons.indexOf(view));
            if (result && !mIsAnimating) {
                final Animator click = getButtonClickAnimation((FloatingActionButton)view);
                click.setDuration(mLongClickDurationRing);
                click.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mListener.onButtonLongClickAnimationStart(CircleMenuView.this, mButtons.indexOf(view));
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mClosedState = true;
                        mListener.onButtonLongClickAnimationEnd(CircleMenuView.this, mButtons.indexOf(view));
                    }
                });
                click.start();
            }

            return result;
        }
    }

    public CircleMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleMenuView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs == null) {
            throw new IllegalArgumentException("No buttons icons or colors set");
        }

        final int menuButtonColor;
        final List<Integer> icons;
        final List<Integer> colors;

        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleMenuView, 0, 0);
        try {
            final int iconArrayId = a.getResourceId(R.styleable.CircleMenuView_button_icons, 0);
            final int colorArrayId = a.getResourceId(R.styleable.CircleMenuView_button_colors, 0);

            final TypedArray iconsIds = getResources().obtainTypedArray(iconArrayId);
            try {
                final int[] colorsIds = getResources().getIntArray(colorArrayId);
                final int buttonsCount = Math.min(iconsIds.length(), colorsIds.length);

                icons = new ArrayList<>(buttonsCount);
                colors = new ArrayList<>(buttonsCount);

                for (int i = 0; i < buttonsCount; i++) {
                    icons.add(iconsIds.getResourceId(i, -1));
                    colors.add(colorsIds[i]);
                }
            } finally {
                iconsIds.recycle();
            }

            mIconMenu = a.getResourceId(R.styleable.CircleMenuView_icon_menu, R.drawable.ic_menu_black_24dp);
            mIconClose = a.getResourceId(R.styleable.CircleMenuView_icon_close, R.drawable.ic_close_black_24dp);

            mDurationRing = a.getInteger(R.styleable.CircleMenuView_duration_ring, getResources().getInteger(android.R.integer.config_mediumAnimTime));
            mLongClickDurationRing = a.getInteger(R.styleable.CircleMenuView_long_click_duration_ring, getResources().getInteger(android.R.integer.config_longAnimTime));
            mDurationOpen = a.getInteger(R.styleable.CircleMenuView_duration_open, getResources().getInteger(android.R.integer.config_mediumAnimTime));
            mDurationClose = a.getInteger(R.styleable.CircleMenuView_duration_close, getResources().getInteger(android.R.integer.config_mediumAnimTime));

            final float density = context.getResources().getDisplayMetrics().density;
            final float defaultDistance = DEFAULT_DISTANCE * density;
            mDistance = a.getDimension(R.styleable.CircleMenuView_distance, defaultDistance);

            menuButtonColor = a.getColor(R.styleable.CircleMenuView_icon_color, Color.WHITE);
        } finally {
            a.recycle();
        }

        initLayout(context);
        initMenu(menuButtonColor);
        initButtons(context, icons, colors);
    }

    /**
     * Constructor for creation CircleMenuView in code, not in xml-layout.
     * @param context current context, will be used to access resources.
     * @param icons buttons icons resource ids array. Items must be @DrawableRes.
     * @param colors buttons colors resource ids array. Items must be @DrawableRes.
     */
    public CircleMenuView(@NonNull Context context, @NonNull List<Integer> icons, @NonNull List<Integer> colors) {
        super(context);

        final float density = context.getResources().getDisplayMetrics().density;
        final float defaultDistance = DEFAULT_DISTANCE * density;

        mIconMenu = R.drawable.ic_menu_black_24dp;
        mIconClose = R.drawable.ic_close_black_24dp;

        mDurationRing = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        mLongClickDurationRing = getResources().getInteger(android.R.integer.config_longAnimTime);
        mDurationOpen = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        mDurationClose = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mDistance = defaultDistance;

        initLayout(context);
        initMenu(Color.WHITE);
        initButtons(context, icons, colors);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int w = resolveSizeAndState(mDesiredSize, widthMeasureSpec, 0);
        final int h = resolveSizeAndState(mDesiredSize, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!changed && mIsAnimating) {
            return;
        }

        mMenuButton.getContentRect(mButtonRect);

        mRingView.setStrokeWidth(mButtonRect.width());
        mRingView.setRadius(mRingRadius);

        final LayoutParams lp = (LayoutParams) mRingView.getLayoutParams();
        lp.width = right - left;
        lp.height = bottom - top;
    }

    private void initLayout(@NonNull Context context) {
        LayoutInflater.from(context).inflate(R.layout.circle_menu, this, true);

        setWillNotDraw(true);
        setClipChildren(false);
        setClipToPadding(false);

        final float density = context.getResources().getDisplayMetrics().density;
        final float buttonSize = DEFAULT_BUTTON_SIZE * density;

        mRingRadius = (int) (buttonSize + (mDistance - buttonSize / 2));
        mDesiredSize = (int) (mRingRadius * 2 * DEFAULT_RING_SCALE_RATIO);

        mRingView = findViewById(R.id.ring_view);
    }

    private void initMenu(int menuButtonColor) {
        final AnimatorListenerAdapter animListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mListener != null) {
                    if (mClosedState) {
                        mListener.onMenuOpenAnimationStart(CircleMenuView.this);
                    } else {
                        mListener.onMenuCloseAnimationStart(CircleMenuView.this);
                    }
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mListener != null) {
                    if (mClosedState) {
                        mListener.onMenuOpenAnimationEnd(CircleMenuView.this);
                    } else {
                        mListener.onMenuCloseAnimationEnd(CircleMenuView.this);
                    }
                }

                mClosedState = !mClosedState;
            }
        };

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
                animation.addListener(animListener);
                animation.start();
            }
        });
    }

    private void initButtons(@NonNull Context context, @NonNull List<Integer> icons, @NonNull List<Integer> colors) {
        final int buttonsCount = Math.min(icons.size(), colors.size());
        for (int i = 0; i < buttonsCount; i++) {
            final FloatingActionButton button = new FloatingActionButton(context);
            button.setImageResource(icons.get(i));
            button.setBackgroundTintList(ColorStateList.valueOf(colors.get(i)));
            button.setClickable(true);
            button.setOnClickListener(new OnButtonClickListener());
            button.setOnLongClickListener(new OnButtonLongClickListener());
            button.setScaleX(0);
            button.setScaleY(0);
            button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            addView(button);
            mButtons.add(button);
        }
    }

    private void offsetAndScaleButtons(float centerX, float centerY, float angleStep, float offset, float scale) {
        for (int i = 0, cnt = mButtons.size(); i < cnt; i++) {
            final float angle = angleStep * i - 90;
            final float x = (float) Math.cos(Math.toRadians(angle)) * offset;
            final float y = (float) Math.sin(Math.toRadians(angle)) * offset;

            final View button = mButtons.get(i);
            button.setX(centerX + x);
            button.setY(centerY + y);
            button.setScaleX(1.0f * scale);
            button.setScaleY(1.0f * scale);
        }
    }

    private Animator getButtonClickAnimation(final @NonNull FloatingActionButton button) {
        final int buttonNumber = mButtons.indexOf(button) + 1;
        final float stepAngle = 360f / mButtons.size();
        final float rOStartAngle = (270 - stepAngle + stepAngle * buttonNumber);
        final float rStartAngle = rOStartAngle > 360 ? rOStartAngle % 360 : rOStartAngle;

        final float x = (float) Math.cos(Math.toRadians(rStartAngle)) * mDistance;
        final float y = (float) Math.sin(Math.toRadians(rStartAngle)) * mDistance;

        final float pivotX = button.getPivotX();
        final float pivotY = button.getPivotY();
        button.setPivotX(pivotX - x);
        button.setPivotY(pivotY - y);

        final ObjectAnimator rotateButton = ObjectAnimator.ofFloat(button, "rotation", 0f, 360f);
        rotateButton.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                button.setPivotX(pivotX);
                button.setPivotY(pivotY);
            }
        });

        final float elevation = mMenuButton.getCompatElevation();

        mRingView.setVisibility(View.INVISIBLE);
        mRingView.setStartAngle(rStartAngle);

        final ColorStateList csl = button.getBackgroundTintList();
        if (csl != null) {
            mRingView.setStrokeColor(csl.getDefaultColor());
        }

        final ObjectAnimator ring = ObjectAnimator.ofFloat(mRingView, "angle", 360);
        final ObjectAnimator scaleX = ObjectAnimator.ofFloat(mRingView, "scaleX", 1f, DEFAULT_RING_SCALE_RATIO);
        final ObjectAnimator scaleY = ObjectAnimator.ofFloat(mRingView, "scaleY", 1f, DEFAULT_RING_SCALE_RATIO);
        final ObjectAnimator visible = ObjectAnimator.ofFloat(mRingView, "alpha", 1f, 0f);

        final AnimatorSet lastSet = new AnimatorSet();
        lastSet.playTogether(scaleX, scaleY, visible, getCloseMenuAnimation());

        final AnimatorSet firstSet = new AnimatorSet();
        firstSet.playTogether(rotateButton, ring);

        final AnimatorSet result = new AnimatorSet();
        result.play(firstSet).before(lastSet);
        result.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    bringChildToFront(mRingView);
                    bringChildToFront(button);
                } else {
                    button.setCompatElevation(elevation + 1);
                    ViewCompat.setZ(mRingView, elevation + 1);

                    for (View b : mButtons) {
                        if (b != button) {
                            ((FloatingActionButton) b).setCompatElevation(0);
                        }
                    }
                }

                mRingView.setScaleX(1f);
                mRingView.setScaleY(1f);
                mRingView.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    for (View b : mButtons) {
                        ((FloatingActionButton) b).setCompatElevation(elevation);
                    }

                    ViewCompat.setZ(mRingView, elevation);
                }
            }
        });

        return result;
    }

    private Animator getOpenMenuAnimation() {
        final ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(mMenuButton, "alpha", DEFAULT_CLOSE_ICON_ALPHA);

        final Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
        final Keyframe kf1 = Keyframe.ofFloat(0.5f, 60f);
        final Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
        final PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
        final ObjectAnimator rotateAnimation = ObjectAnimator.ofPropertyValuesHolder(mMenuButton, pvhRotation);
        rotateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private boolean iconChanged = false;
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float fraction = valueAnimator.getAnimatedFraction();
                if (fraction >= 0.5f && !iconChanged) {
                    iconChanged = true;
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
                offsetAndScaleButtons(centerX, centerY, angleStep, value, fraction);
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

    /**
     * See {@link R.styleable#CircleMenuView_duration_close}
     * @param duration close animation duration in milliseconds.
     */
    public void setDurationClose(int duration) {
        mDurationClose = duration;
    }

    /**
     * See {@link R.styleable#CircleMenuView_duration_close}
     * @return current close animation duration.
     */
    public int getDurationClose() {
        return mDurationClose;
    }

    /**
     * See {@link R.styleable#CircleMenuView_duration_open}
     * @param duration open animation duration in milliseconds.
     */
    public void setDurationOpen(int duration) {
        mDurationOpen = duration;
    }

    /**
     * See {@link R.styleable#CircleMenuView_duration_open}
     * @return current open animation duration.
     */
    public int getDurationOpen() {
        return mDurationOpen;
    }

    /**
     * See {@link R.styleable#CircleMenuView_duration_ring}
     * @param duration ring animation duration in milliseconds.
     */
    public void setDurationRing(int duration) {
        mDurationRing = duration;
    }

    /**
     * See {@link R.styleable#CircleMenuView_duration_ring}
     * @return current ring animation duration.
     */
    public int getDurationRing() {
        return mDurationRing;
    }

    /**
     * See {@link R.styleable#CircleMenuView_long_click_duration_ring}
     * @return current long click ring animation duration.
     */
    public int getLongClickDurationRing() {
        return mLongClickDurationRing;
    }

    /**
     * See {@link R.styleable#CircleMenuView_long_click_duration_ring}
     * @param duration long click ring animation duration in milliseconds.
     */
    public void setLongClickDurationRing(int duration) {
        mLongClickDurationRing = duration;
    }

    /**
     * See {@link R.styleable#CircleMenuView_distance}
     * @param distance in pixels.
     */
    public void setDistance(float distance) {
        mDistance = distance;
        invalidate();
    }

    /**
     * See {@link R.styleable#CircleMenuView_distance}
     * @return current distance in pixels.
     */
    public float getDistance() {
        return mDistance;
    }

    /**
     * See {@link CircleMenuView.EventListener }
     * @param listener new event listener or null.
     */
    public void setEventListener(@Nullable EventListener listener) {
        mListener = listener;
    }

    /**
     * See {@link CircleMenuView.EventListener }
     * @return current event listener or null.
     */
    public EventListener getEventListener() {
        return mListener;
    }

    private void openOrClose(boolean open, boolean animate) {
        if (mIsAnimating) {
            return;
        }

        if (open && !mClosedState) {
            return;
        }

        if (!open && mClosedState) {
            return;
        }

        if (animate) {
            mMenuButton.performClick();
        } else {
            mClosedState = !open;

            final float centerX = mMenuButton.getX();
            final float centerY = mMenuButton.getY();

            final int buttonsCount = mButtons.size();
            final float angleStep = 360f / buttonsCount;

            final float offset = open ? mDistance : 0f;
            final float scale = open ? 1f : 0f;

            mMenuButton.setImageResource(open ? mIconClose : mIconMenu);
            mMenuButton.setAlpha(open ? DEFAULT_CLOSE_ICON_ALPHA : 1f);

            final int visibility = open ? View.VISIBLE : View.INVISIBLE;
            for (View view: mButtons) {
                view.setVisibility(visibility);
            }

            offsetAndScaleButtons(centerX, centerY, angleStep, offset, scale);
        }
    }

    /**
     * Open menu programmatically
     * @param animate open with animation or not
     */
    public void open(boolean animate) {
        openOrClose(true, animate);
    }

    /**
     * Close menu programmatically
     * @param animate close with animation or not
     */
    public void close(boolean animate) {
        openOrClose(false, animate);
    }

}
