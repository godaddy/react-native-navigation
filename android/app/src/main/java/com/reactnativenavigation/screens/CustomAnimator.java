package com.reactnativenavigation.screens;

import android.animation.Animator;
import android.os.Bundle;

public interface CustomAnimator {
    Animator createAnimator(Bundle animation, Screen screen, Runnable onAnimationEnd);
}