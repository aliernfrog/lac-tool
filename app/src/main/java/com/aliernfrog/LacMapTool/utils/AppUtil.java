package com.aliernfrog.LacMapTool.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONObject;

@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
public class AppUtil {
    static String updateUrl = "https://aliernfrog.glitch.me/lacmaptool/update.json";

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

    public static void copyToClipboard(String string, Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("LAC Tool", string);
        manager.setPrimaryClip(clip);
    }

    public static Boolean getUpdates(Context context) throws Exception {
        SharedPreferences update = context.getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        SharedPreferences.Editor updateEdit = update.edit();
        String rawUpdate = WebUtil.getContentFromURL(updateUrl);
        JSONObject object = new JSONObject(rawUpdate);
        updateEdit.putInt("updateLatest", object.getInt("latest"));
        updateEdit.putString("updateDownload", object.getString("download"));
        updateEdit.putString("updateChangelog", object.getString("changelog"));
        updateEdit.putString("updateChangelogVersion", object.getString("changelogVersion"));
        updateEdit.putString("notes", object.getString("notes"));
        updateEdit.commit();
        return true;
    }

    public static void toggleView(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
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
