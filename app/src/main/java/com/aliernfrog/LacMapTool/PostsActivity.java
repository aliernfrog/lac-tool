package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.BackgroundTask;
import com.aliernfrog.LacMapTool.utils.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressLint("ClickableViewAccessibility")
public class PostsActivity extends AppCompatActivity {
    ImageView goback;
    LinearLayout rootLinear;
    ProgressBar progressBar;
    TextView log;

    String newsURL = "https://blursedbots.glitch.me/lacmaptool/news.json";
    String logs = "";
    JSONArray newsArray;

    Boolean devMode;

    SharedPreferences config;
    SharedPreferences.Editor configEdit;
    SharedPreferences update;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        configEdit = config.edit();
        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        devMode = config.getBoolean("enableDebug", false);

        goback = findViewById(R.id.posts_goback);
        rootLinear = findViewById(R.id.posts_linear_posts);
        progressBar = findViewById(R.id.posts_progressBar);
        log = findViewById(R.id.posts_log);

        setListeners();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                getContentFromURL(newsURL);
            }
        }, 1500);
    }

    public void setPostUpdate() {
        Integer updated = update.getInt("postUpdate", 0);
        devLog("attempting to set postUpdate to "+updated, false);
        configEdit.putInt("postUpdate", updated);
        configEdit.commit();
    }

    public void getNews(String string) {
        try {
            newsArray = new JSONArray(string);
            devLog("Found "+newsArray.length()+" objects", false);
            for (int i = 0; i < newsArray.length(); i++) {
                JSONObject object = newsArray.getJSONObject(i);
                ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(R.layout.news, rootLinear, false);
                devLog(" inflated: "+i, false);
                setNewsView(layout, object);
            }
            progressBar.setVisibility(View.GONE);
            setPostUpdate();
            devLog(" done", false);
        } catch (JSONException e) {
            e.printStackTrace();
            devLog(e.toString(), true);
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
            devLog(e.toString(), true);
        }
    }

    public void setNewsView(ViewGroup layout, JSONObject object) throws JSONException {
        LinearLayout background = layout.findViewById(R.id.news_bg);
        TextView title = layout.findViewById(R.id.news_title);
        ImageView thumbnail = layout.findViewById(R.id.news_thumbnail);
        TextView description = layout.findViewById(R.id.news_description);
        TextView footer = layout.findViewById(R.id.news_footer);
        Button redirect = layout.findViewById(R.id.news_redirect);
        title.setText(Html.fromHtml(object.getString("title")));
        description.setText(Html.fromHtml(object.getString("description")));
        if (!object.getString("thumbnail").contains("://")) {
            thumbnail.setVisibility(View.GONE);
            devLog("thumbnail not found", false);
        } else {
            devLog("attempting to set thumbnail", false);
            try {
                URL imgUrl = new URL(object.getString("thumbnail"));
                Bitmap bitmap = BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream());
                thumbnail.setImageBitmap(bitmap);
                thumbnail.setOnLongClickListener(v -> {
                    redirectURL(imgUrl.toString());
                    return true;
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                devLog(e.toString(), true);
            } catch (IOException e) {
                e.printStackTrace();
                devLog(e.toString(), true);
            }
        }
        if (object.getString("footer").length() < 2) {
            footer.setVisibility(View.GONE);
            devLog("footer not found", false);
        } else {
            footer.setText(Html.fromHtml(object.getString("footer")));
        }
        if (object.getString("color").contains("button")) background.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.linear_button));
        if (object.getString("color").contains("blue")) background.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.linear_blue));
        if (object.getString("color").contains("red")) background.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.linear_red));
        if (!object.getString("redirect").contains("://")) {
            redirect.setVisibility(View.GONE);
            devLog("redirect not found", false);
        } else {
            String[] arr = object.getString("redirect").split(";;;;");
            try {
                if (arr[0] != null) {
                    redirect.setText(Html.fromHtml(arr[0]));
                    if (arr[1] != null) redirect.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                redirectURL(arr[1]);
                            }
                            AppUtil.handleOnPressEvent(v, event);
                            return true;
                        }
                    });
                }
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                redirect.setVisibility(View.GONE);
                devLog(e.toString(), true);
            }
        }
        background.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        rootLinear.addView(layout);
    }

    public void redirectURL(String string) {
        if (string.startsWith("activity://")) {
            try {
                String activity = string.replace("activity://", "");
                Intent intent = new Intent(this.getApplicationContext(), Class.forName(getApplicationContext().getPackageName()+"."+activity));
                devLog("attempting to redirect to "+activity, false);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                devLog(e.toString(), true);
            }
        } else {
            devLog("attempting to redirect to: "+string, false);
            Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(string));
            startActivity(viewIntent);
        }
    }

    void devLog(String toLog, Boolean error) {
        if (devMode) {
            String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
            if (error) toLog = "<font color=red>"+toLog+"</font>";
            logs = logs+"<br /><font color=#00FFFF>["+tag+"]</font> "+toLog;
            log.setText(Html.fromHtml(logs));
        }
    }

    public void getContentFromURL(String urlString) {
        devLog("attempting to read: "+urlString, false);
        String[] res = {null};
        try {
            new BackgroundTask(this) {
                @Override
                public void doInBackground() {
                    try {
                        String str = WebUtil.getContentFromURL(urlString);
                        res[0] = str;
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> devLog(e.toString(), true));
                    }
                }

                @Override
                public void onPostExecute() {
                    getNews(res[0]);
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString(), true);
        }
    }

    void setListeners() {
        goback.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                finish();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
    }
}