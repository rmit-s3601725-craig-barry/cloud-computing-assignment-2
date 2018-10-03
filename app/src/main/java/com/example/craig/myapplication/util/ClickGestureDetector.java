package com.example.craig.myapplication.util;

import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class ClickGestureDetector
        implements GestureDetector.OnGestureListener
{
    private Callback<Void> callback;

    public ClickGestureDetector() {}

    public ClickGestureDetector(Callback<Void> callback)
    {
        this.callback = callback;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        callback.callback(null);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
