package com.example.craig.myapplication.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class LoadingDialog {

    private LoadingDialog() {}

    public static void showLoading(Context context, String msg)
    {
        stopLoading();
        loadingDialog = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
        loadingDialog.setMessage(msg);
        loadingDialog.setCancelable(false);
        loadingDialog.setInverseBackgroundForced(false);
        doLoading = true;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                synchronized (loadingSync)
                {
                    if(doLoading)
                        loadingDialog.show();
                }
            }
        }, SHOW_LOADING_DELAY);
        loadingDialog.show();
    }

    public static void stopLoading()
    {
        if(loadingDialog != null && loadingDialog.isShowing())
        {
            synchronized (loadingSync)
            {
                doLoading = false;
                loadingDialog.hide();
            }
        }
    }

    private static final Object loadingSync = false;
    private static boolean doLoading = true;
    private static final int SHOW_LOADING_DELAY = 750;
    private static ProgressDialog loadingDialog;
}
