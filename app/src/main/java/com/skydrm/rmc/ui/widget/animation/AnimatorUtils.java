package com.skydrm.rmc.ui.widget.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by jrzhou on 12/30/2016.
 */

public class AnimatorUtils {

    //  duration: default 200
    // interpolator: decrease
    public static void open(@NonNull final View view, @NonNull int duration, TimeInterpolator interpolator, Animator.AnimatorListener animatorListener) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator rotationY = ObjectAnimator.ofFloat(view, "rotationY", 90f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleX", 0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f);
        animatorSet.play(scaleY).with(rotationY);
        animatorSet.setDuration(duration);
        if (null != animatorListener) {
            animatorSet.setInterpolator(interpolator);
        }
        if (null != animatorListener) {
            animatorSet.addListener(animatorListener);
        }
        animatorSet.start();
    }

    //  duration: default 200
    // interpolator: increase
    public static void close(@NonNull View view, @NonNull int duration, TimeInterpolator interpolator, Animator.AnimatorListener animatorListener) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator rotationY = ObjectAnimator.ofFloat(view, "rotationY", 0f, 90f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f, 0f);
        animatorSet.play(scaleY).with(rotationY);
        animatorSet.setDuration(duration);
        if (null != animatorListener) {
            animatorSet.setInterpolator(interpolator);
        }
        if (null != animatorListener) {
            animatorSet.addListener(animatorListener);
        }
        animatorSet.start();
    }

}
