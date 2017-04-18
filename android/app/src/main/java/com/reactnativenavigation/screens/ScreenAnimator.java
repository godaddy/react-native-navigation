package com.reactnativenavigation.screens;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.reactnativenavigation.NavigationApplication;
import com.reactnativenavigation.utils.ViewUtils;
import com.reactnativenavigation.views.sharedElementTransition.SharedElementsAnimator;

import javax.annotation.Nullable;

class ScreenAnimator {
    private static final String FADE_IN_ANIMATION = "fadeIn";
    private static final String FADE_OUT_ANIMATION = "fadeOut";
    private static final String SLIDE_IN_FROM_RIGHT_ANIMATION = "slideInFromRight";
    private static final String SLIDE_OUT_FROM_LEFT_ANIMATION = "slideOutFromLeft";

    private final float translationY;
    private final int screenWidth;
    private Screen screen;

    ScreenAnimator(Screen screen) {
        this.screen = screen;
        translationY = 0.08f * ViewUtils.getScreenHeight();
        screenWidth = ViewUtils.getScreenWidth();
    }

    private Animator resolveShowAnimation(Bundle animation, Runnable onAnimationEnd) {
        if (animation != null && animation.containsKey("type")) {
            switch (animation.getString("type")) {
                case FADE_IN_ANIMATION: return createFadeInAnimator(animation, onAnimationEnd);
                case SLIDE_IN_FROM_RIGHT_ANIMATION: return createSlideInFromRightAnimator(animation, onAnimationEnd);
            }
        }
        return createShowAnimator(onAnimationEnd);
    }

    public void show(boolean animate, Bundle nextScreenAnimation, final Runnable onAnimationEnd) {
        if (animate) {
            resolveShowAnimation(nextScreenAnimation, onAnimationEnd).start();
        } else {
            screen.setVisibility(View.VISIBLE);
            if (onAnimationEnd != null) {
                NavigationApplication.instance.runOnMainThread(onAnimationEnd, 200);
            }
        }
    }

    private Animator resolveHideAnimation(Bundle animation, Runnable onAnimationEnd) {
        if (animation != null && animation.containsKey("type")) {
            switch (animation.getString("type")) {
                case FADE_OUT_ANIMATION: return createFadeOutAnimator(animation, onAnimationEnd);
                case SLIDE_OUT_FROM_LEFT_ANIMATION: return createSlideOutFromLeftAnimator(animation, onAnimationEnd);
            }
        }
        return createHideAnimator(onAnimationEnd);
    }

    public void hide(boolean animate, Bundle previousScreenAnimation, Runnable onAnimationEnd) {
        if (animate) {
            resolveHideAnimation(previousScreenAnimation, onAnimationEnd).start();
        } else {
            screen.setVisibility(View.INVISIBLE);
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            }
        }
    }

    private Animator createShowAnimator(final @Nullable Runnable onAnimationEnd) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(screen, View.ALPHA, 0, 1);
        alpha.setInterpolator(new DecelerateInterpolator());
        alpha.setDuration(200);

        ObjectAnimator translationY = ObjectAnimator.ofFloat(screen, View.TRANSLATION_Y, this.translationY, 0);
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration(280);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(translationY, alpha);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                screen.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });
        return set;
    }

    private Animator createHideAnimator(final Runnable onAnimationEnd) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(screen, View.ALPHA, 0);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setStartDelay(100);
        alpha.setDuration(150);

        ObjectAnimator translationY = ObjectAnimator.ofFloat(screen, View.TRANSLATION_Y, this.translationY);
        translationY.setInterpolator(new AccelerateInterpolator());
        translationY.setDuration(250);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(translationY, alpha);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });
        return set;
    }

    void showWithSharedElementsTransitions(Runnable onAnimationEnd) {
        screen.setVisibility(View.VISIBLE);
        new SharedElementsAnimator(this.screen.sharedElements).show(onAnimationEnd);
    }

    void hideWithSharedElementsTransition(Runnable onAnimationEnd) {
        new SharedElementsAnimator(screen.sharedElements).hide(onAnimationEnd);
    }

    private Animator createFadeInAnimator(Bundle animation, final Runnable onAnimationEnd) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(screen, View.ALPHA, 0, 1);
        fadeIn.setInterpolator(new LinearInterpolator());
        fadeIn.setDuration(animation.getInt("durationMs", 300));

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeIn);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                screen.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });
        return set;
    }

    private Animator createFadeOutAnimator(Bundle animation, final Runnable onAnimationEnd) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(screen, View.ALPHA, 1, 0);
        fadeOut.setInterpolator(new LinearInterpolator());
        fadeOut.setDuration(animation.getInt("durationMs", 300));

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeOut);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                screen.setAlpha(1);
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });
        return set;
    }

    private Animator createSlideInFromRightAnimator(Bundle animation, final Runnable onAnimationEnd) {
        ObjectAnimator slideInFromRight = ObjectAnimator.ofFloat(screen, View.TRANSLATION_X, this.screenWidth, 0);
        slideInFromRight.setInterpolator(new DecelerateInterpolator());
        slideInFromRight.setDuration(animation.getInt("durationMs", 300));

        AnimatorSet set = new AnimatorSet();
        set.playTogether(slideInFromRight);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                screen.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });
        return set;
    }

    private Animator createSlideOutFromLeftAnimator(Bundle animation, final Runnable onAnimationEnd) {
        ObjectAnimator slideInFromRight = ObjectAnimator.ofFloat(screen, View.TRANSLATION_X, 0, this.screenWidth);
        slideInFromRight.setInterpolator(new DecelerateInterpolator());
        slideInFromRight.setDuration(animation.getInt("durationMs", 300));

        AnimatorSet set = new AnimatorSet();
        set.playTogether(slideInFromRight);
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });
        return set;
    }
}
