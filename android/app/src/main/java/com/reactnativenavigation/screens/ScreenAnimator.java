package com.reactnativenavigation.screens;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.reactnativenavigation.NavigationApplication;
import com.reactnativenavigation.utils.ViewUtils;
import com.reactnativenavigation.views.sharedElementTransition.SharedElementsAnimator;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

class ScreenAnimator {
    private static final String FADE_IN_ANIMATION = "fadeIn";
    private static final String FADE_OUT_ANIMATION = "fadeOut";
    private static final String SLIDE_IN_FROM_RIGHT_ANIMATION = "slideInFromRight";
    private static final String SLIDE_OUT_FROM_LEFT_ANIMATION = "slideOutFromLeft";
    private static final int DEFAULT_ANIMATION_DURATION_MS = 300;

    private final float translationY;
    private Screen screen;

    private static Map<String, CustomAnimator> customAnimators = new HashMap<>();

    ScreenAnimator(Screen screen) {
        this.screen = screen;
        translationY = 0.08f * ViewUtils.getScreenHeight();
    }

    private CustomAnimator loadCustomAnimator(String customAnimator) {
        Class clazz;
        try {
            clazz = Class.forName(customAnimator);
            return (CustomAnimator) clazz.newInstance();
        } catch (Exception e) {
            Log.e("loadCustomAnimator", "error loading CustomAnimator class '" + customAnimator + "'. " + e.getCause().getMessage(), e);
            return null;
        }
    }

    private Animator resolveCustomAnimator(Bundle animation, Runnable onAnimationEnd) {
        if (animation != null && animation.containsKey("androidCustomAnimator")) {
            String androidCustomAnimator = animation.getString("androidCustomAnimator");
            if (customAnimators == null) {
                customAnimators = new HashMap<>();
            }
            // use cached copy
            if (customAnimators.containsKey(androidCustomAnimator)) {
                return customAnimators.get(androidCustomAnimator).createAnimator(animation, screen, onAnimationEnd);
            }
            CustomAnimator customAnimator = loadCustomAnimator(androidCustomAnimator);
            if (customAnimator != null) {
                customAnimators.put(androidCustomAnimator, customAnimator);
                return customAnimator.createAnimator(animation, screen, onAnimationEnd);
            }
        }
        return null;
    }

    private Animator resolveShowAnimator(Bundle showScreenAnimation, Runnable onAnimationEnd) {
        if (showScreenAnimation != null && showScreenAnimation.containsKey("type")) {
            switch (showScreenAnimation.getString("type")) {
                case FADE_IN_ANIMATION: return createFadeInAnimator(showScreenAnimation, onAnimationEnd);
                case SLIDE_IN_FROM_RIGHT_ANIMATION: return createSlideInFromRightAnimator(showScreenAnimation, onAnimationEnd);
            }
        }
        Animator customAnimator = resolveCustomAnimator(showScreenAnimation, onAnimationEnd);
        if (customAnimator != null) {
            return customAnimator;
        }
        return createShowAnimator(onAnimationEnd);
    }

    public void show(boolean animate, Bundle showScreenAnimation, final Runnable onAnimationEnd) {
        if (animate) {
            resolveShowAnimator(showScreenAnimation, onAnimationEnd).start();
        } else {
            screen.setVisibility(View.VISIBLE);
            if (onAnimationEnd != null) {
                NavigationApplication.instance.runOnMainThread(onAnimationEnd, 200);
            }
        }
    }

    private Animator resolveHideAnimation(Bundle hideScreenAnimation, Runnable onAnimationEnd) {
        if (hideScreenAnimation != null && hideScreenAnimation.containsKey("type")) {
            switch (hideScreenAnimation.getString("type")) {
                case FADE_OUT_ANIMATION: return createFadeOutAnimator(hideScreenAnimation, onAnimationEnd);
                case SLIDE_OUT_FROM_LEFT_ANIMATION: return createSlideOutFromLeftAnimator(hideScreenAnimation, onAnimationEnd);
            }
        }
        Animator customAnimation = resolveCustomAnimator(hideScreenAnimation, onAnimationEnd);
        if (customAnimation != null) {
            return customAnimation;
        }
        return createHideAnimator(onAnimationEnd);
    }

    public void hide(boolean animate, Bundle hideScreenAnimation, Runnable onAnimationEnd) {
        if (animate) {
            resolveHideAnimation(hideScreenAnimation, onAnimationEnd).start();
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

    private Animator createFadeInAnimator(Bundle animation, final Runnable onAnimationEnd) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(screen, View.ALPHA, 0, 1);
        fadeIn.setInterpolator(new LinearInterpolator());
        fadeIn.setDuration(animation.getInt("durationMs", DEFAULT_ANIMATION_DURATION_MS));
        fadeIn.addListener(new AnimatorListenerAdapter() {
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

        return fadeIn;
    }

    private Animator createFadeOutAnimator(Bundle animation, final Runnable onAnimationEnd) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(screen, View.ALPHA, 1, 0);
        fadeOut.setInterpolator(new LinearInterpolator());
        fadeOut.setDuration(animation.getInt("durationMs", DEFAULT_ANIMATION_DURATION_MS));
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                screen.setAlpha(1);
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });

        return fadeOut;
    }

    private Animator createSlideInFromRightAnimator(Bundle animation, final Runnable onAnimationEnd) {
        ObjectAnimator slideInFromRight = ObjectAnimator.ofFloat(screen, View.TRANSLATION_X, ViewUtils.getScreenWidth(), 0);
        slideInFromRight.setInterpolator(new DecelerateInterpolator());
        slideInFromRight.setDuration(animation.getInt("durationMs", DEFAULT_ANIMATION_DURATION_MS));
        slideInFromRight.setStartDelay(1);
        slideInFromRight.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                screen.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });

        return slideInFromRight;
    }

    private Animator createSlideOutFromLeftAnimator(Bundle animation, final Runnable onAnimationEnd) {
        ObjectAnimator slideInFromRight = ObjectAnimator.ofFloat(screen, View.TRANSLATION_X, 0, ViewUtils.getScreenWidth());
        slideInFromRight.setInterpolator(new DecelerateInterpolator());
        slideInFromRight.setDuration(animation.getInt("durationMs", DEFAULT_ANIMATION_DURATION_MS));
        slideInFromRight.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });

        return slideInFromRight;
    }

    void showWithSharedElementsTransitions(Runnable onAnimationEnd) {
        hideContentViewAndTopBar();
        screen.setVisibility(View.VISIBLE);
        new SharedElementsAnimator(this.screen.sharedElements).show(new Runnable() {
            @Override
            public void run() {
                animateContentViewAndTopBar(1, 280);
            }
        }, onAnimationEnd);
    }

    private void hideContentViewAndTopBar() {
        if (screen.screenParams.animateScreenTransitions) {
            screen.getContentView().setAlpha(0);
        }
        screen.getTopBar().setAlpha(0);
    }

    void hideWithSharedElementsTransition(Runnable onAnimationEnd) {
        new SharedElementsAnimator(screen.sharedElements).hide(new Runnable() {
            @Override
            public void run() {
                animateContentViewAndTopBar(0, 200);
            }
        }, onAnimationEnd);
    }

    private void animateContentViewAndTopBar(int alpha, int duration) {
        List<Animator> animators = new ArrayList<>();
        if (screen.screenParams.animateScreenTransitions) {
            animators.add(ObjectAnimator.ofFloat(screen.getContentView(), View.ALPHA, alpha));
        }
        animators.add(ObjectAnimator.ofFloat(screen.getTopBar(), View.ALPHA, alpha));
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        set.setDuration(duration);
        set.start();
    }
}
