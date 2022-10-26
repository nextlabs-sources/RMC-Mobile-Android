package com.skydrm.rmc.ui.widget.animation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by hhu on 5/17/2017.
 */

public class ShakeAnimator {
    private AnimatorSet mAnimatorSet;
    private static final int DURATION = 1000;
    private long duration = DURATION;

    {
        mAnimatorSet = new AnimatorSet();
    }

    AnimatorSet getAnimatorAgent() {
        return mAnimatorSet;
    }

    public void setDurtion(long duration) {
        this.duration = duration;
    }

    public void setTarget(View target) {
        reset(target);
        prapareTarget(target);
    }

    public void reset(View target) {
        ViewCompat.setAlpha(target, 1);
        ViewCompat.setScaleX(target, 1);
        ViewCompat.setScaleY(target, 1);
        ViewCompat.setTranslationX(target, 0);
        ViewCompat.setTranslationY(target, 0);
        ViewCompat.setRotation(target, 0);
        ViewCompat.setRotationY(target, 0);
        ViewCompat.setRotationX(target, 0);
    }

    private void prapareTarget(View target) {
        getAnimatorAgent().playTogether(
//                ObjectAnimator.ofFloat(target, "scaleX", 1, 0.9f, 0.9f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1),
//                ObjectAnimator.ofFloat(target, "scaleY", 1, 0.9f, 0.9f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1),
//                ObjectAnimator.ofFloat(target, "rotation", 0, -3, -3, 3, -3, 3, -3, 3, -3, 0)
                ObjectAnimator.ofFloat(target, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
        );
    }

    public void startAnimation() {
        mAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimatorSet.setDuration(duration);
        mAnimatorSet.start();
    }
}
