package com.aliernfrog.LacMapTool.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.MotionEvent;
import android.view.View;

public class AppUtil {

    public static String getVersName(Context context) throws Exception {
        PackageManager pm = context.getPackageManager();
        PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
        return pInfo.versionName;
    }

    public static Integer getVersCode(Context context) throws Exception {
        PackageManager pm = context.getPackageManager();
        PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
        return pInfo.versionCode;
    }

    public static void handleOnPressEvent(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f);
                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f);
                scaleDownX.setDuration(100);
                scaleDownY.setDuration(100);
                AnimatorSet scaleDown = new AnimatorSet();
                scaleDown.play(scaleDownX).with(scaleDownY);
                scaleDown.start();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f);
                ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f);
                scaleUpX.setDuration(100);
                scaleUpY.setDuration(100);
                AnimatorSet scaleUp = new AnimatorSet();
                scaleUp.play(scaleUpX).with(scaleUpY);
                scaleUp.start();
                break;
        }
    }
}
