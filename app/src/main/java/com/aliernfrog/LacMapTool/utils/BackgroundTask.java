package com.aliernfrog.LacMapTool.utils;

import android.app.Activity;

public abstract class BackgroundTask {

    private Activity activity;
    public BackgroundTask(Activity activity) {
        this.activity = activity;
    }

    private void startBackground() {
        new Thread(new Runnable() {
            public void run() {

                doInBackground();
                activity.runOnUiThread(new Runnable() {
                    public void run() {

                        onPostExecute();
                    }
                });
            }
        }).start();
    }
    public void execute(){
        startBackground();
    }

    public abstract void doInBackground();
    public abstract void onPostExecute();

}