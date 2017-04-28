package com.reactnativenavigation.screens.animators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.reactnativenavigation.screens.CustomAnimator;
import com.reactnativenavigation.screens.Screen;
import com.reactnativenavigation.utils.ViewUtils;

public class SlideOutFromLeftAnimator implements CustomAnimator {

    public Animator createAnimator(Bundle animation, final Screen screen, final Runnable onAnimationEnd) {
        ObjectAnimator slideOutFromLeft = ObjectAnimator.ofFloat(screen, View.TRANSLATION_X, 0, ViewUtils.getScreenWidth());
        slideOutFromLeft.setInterpolator(new DecelerateInterpolator());
        slideOutFromLeft.setDuration(animation.getInt("durationMs", DEFAULT_ANIMATION_DURATION_MS));
        slideOutFromLeft.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });

        return slideOutFromLeft;
    }
}
