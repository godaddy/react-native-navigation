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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

class ScreenAnimator {

    private final float translationY;
    private Screen screen;

    private Map<String, CustomAnimator> customAnimators = new HashMap<>();

    ScreenAnimator(Screen screen) {
        this.screen = screen;
        translationY = 0.08f * ViewUtils.getScreenHeight();
    }

    private CustomAnimator loadAnimator(String type) {
        Class clazz;
        try {
            clazz = Class.forName(type);
            return (CustomAnimator) clazz.newInstance();
        } catch (Exception e) {
            Log.e("loadAnimator", "error loading CustomAnimator class '" + type + "'. " + e.getCause().getMessage(), e);
            return null;
        }
    }

    private Animator resolveCustomAnimator(Bundle animation, Runnable onAnimationEnd) {
        if (animation != null && animation.containsKey("type")) {
            String type = animation.getString("type");
            // use cached copy
            if (customAnimators.containsKey(type)) {
                return customAnimators.get(type).createAnimator(animation, screen, onAnimationEnd);
            }
            CustomAnimator customAnimator = loadAnimator(type);
            if (customAnimator != null) {
                customAnimators.put(type, customAnimator);
                return customAnimator.createAnimator(animation, screen, onAnimationEnd);
            }
        }
        return null;
    }

    private Animator resolveShowAnimator(Bundle animation, Runnable onAnimationEnd) {
        Animator customAnimator = resolveCustomAnimator(animation, onAnimationEnd);
        if (customAnimator != null) {
            return customAnimator;
        }
        return createShowAnimator(onAnimationEnd);
    }

    public void show(boolean animate, Bundle nextScreenAnimation, final Runnable onAnimationEnd) {
        if (animate) {
            resolveShowAnimator(nextScreenAnimation, onAnimationEnd).start();
        } else {
            screen.setVisibility(View.VISIBLE);
            if (onAnimationEnd != null) {
                NavigationApplication.instance.runOnMainThread(onAnimationEnd, 200);
            }
        }
    }

    private Animator resolveHideAnimation(Bundle animation, Runnable onAnimationEnd) {
        Animator customAnimation = resolveCustomAnimator(animation, onAnimationEnd);
        if (customAnimation != null) {
            return customAnimation;
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

}
