package com.example.craig.myapplication.util;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

public class Transitions
{
    public static final void setupTransition(ViewGroup root)
    {
        Scene s = new Scene(root);
        Transition t = new Slide(Gravity.LEFT);
        t.setDuration(TRANSITION_SPEED);
        t.setStartDelay(0);
        t.setInterpolator(new LinearOutSlowInInterpolator());
        TransitionManager.go(s, t);
    }

    public static final void addElement(ViewGroup root, View item)
    {
        int delay = nElements++ * TRANSITION_DELAY;

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Transitions.setupTransition(root);
                root.addView(item);
                nElements--;
            }
        }, delay);
    }

    private static int nElements = 0;

    private final static int TRANSITION_DELAY = 120;
    private final static int TRANSITION_SPEED = 400;
}
