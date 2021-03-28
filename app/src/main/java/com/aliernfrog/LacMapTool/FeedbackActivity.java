package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.BackgroundTask;
import com.aliernfrog.LacMapTool.utils.WebUtil;

import org.json.JSONObject;

@SuppressLint("ClickableViewAccessibility")
public class FeedbackActivity extends AppCompatActivity {
    ImageView goBack;
    LinearLayout feedbackLinear;
    EditText feedbackInput;
    Button sendFeedback;
    TextView log;

    String postUrl = "https://ensibot-discord.aliernfrog.repl.co";
    String logs = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        goBack = findViewById(R.id.feedback_goback);
        feedbackLinear = findViewById(R.id.feedback_bg);
        feedbackInput = findViewById(R.id.feedback_input);
        sendFeedback = findViewById(R.id.feedback_done);
        log = findViewById(R.id.feedback_log);

        setListener();
    }

    public void sendFeedback() {
        doPostRequest(postUrl, feedbackInput.getText().toString());
    }

    public void doPostRequest(String Url, String body) {
        devLog("attempting to do POST request to: "+Url+" with body: "+body, false);try {
            JSONObject obj = new JSONObject();
            obj.put("type", "feedback");
            obj.put("body", body);
            final String[] res = {null};
            new BackgroundTask(this) {

                @Override
                public void doInBackground() {
                    try {
                        res[0] = WebUtil.doPostRequest(Url, obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                devLog(e.toString(), true);
                            }
                        });
                    }
                }

                @Override
                public void onPostExecute() {
                    handleResponse(res[0]);
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString(), true);
        }
    }

    public void handleResponse(String res) {
        devLog("received response: "+res, false);
        if (res == null) {
            devLog("can't access server", false);
            Toast.makeText(getApplicationContext(), "can't access server", Toast.LENGTH_SHORT).show();
        } else {
            if (res.contains("received")) {
                Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
                devLog("thanks for sharing your feedback!", false);
            } else {
                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void devLog(String toLog, Boolean error) {
        String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
        if (error) toLog = "<font color=red>"+toLog+"</font>";
        logs = logs+"<br /><font color=#00FFFF>["+tag+"]</font> "+toLog;
        log.setText(Html.fromHtml(logs));
    }

    void setListener() {
        goBack.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                finish();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        feedbackLinear.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        sendFeedback.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                sendFeedback();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        log.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
    }
}