package com.aliernfrog.LacMapTool.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliernfrog.LacMapTool.R;
import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MapDownloadSheet extends BottomSheetDialogFragment {
    private MapDownloadListener listener;

    TextView title;
    ProgressBar progressBar;
    LinearLayout optionsLinear;
    EditText linkInput;
    Button downloadConfirm;

    DownloadManager downloadManager;

    Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_map_download, container, false);

        title = view.findViewById(R.id.mapDownload_title);
        progressBar = view.findViewById(R.id.mapDownload_progress);
        optionsLinear = view.findViewById(R.id.mapDownload_optionsLinear);
        linkInput = view.findViewById(R.id.mapDownload_linkInput);
        downloadConfirm = view.findViewById(R.id.mapDownload_download);

        context = getActivity();
        if (context != null) downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        setListeners();

        return view;
    }

    void download(String url) {
        setCancelable(false);
        title.setText(R.string.mapDownload_downloading);
        progressBar.setVisibility(View.VISIBLE);
        optionsLinear.setVisibility(View.GONE);
        downloadConfirm.setVisibility(View.GONE);
        startDownloading(url);
    }

    void startDownloading(String url) {
        String fileName = URLUtil.guessFileName(url, null, null);
        boolean isMap = fileName.endsWith(".txt");
        if (isMap) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setTitle(fileName)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            long downloadReference = downloadManager.enqueue(request);
            watchProgress(downloadReference);
        } else {
            Toast.makeText(context, R.string.mapDownload_notAMap, Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    @SuppressLint("Range")
    void watchProgress(long reference) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(reference);
                Cursor cursor = downloadManager.query(query);
                if (cursor.moveToFirst()) {
                    int downloadedBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int totalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    float downloadProgress = (downloadedBytes * 100f) / totalBytes;
                    progressBar.setProgress((int) downloadProgress);
                    if (downloadProgress > 99.9) {
                        timer.cancel();
                        String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        String name = new File(uri).getName();
                        ((Activity)context).runOnUiThread(() -> finishDownloading(name));
                    }
                    cursor.close();
                }
            }
        }, 0, 100);
    }

    void finishDownloading(String name) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/"+name;
        listener.onMapDownloaded(path);
        Toast.makeText(context.getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    void setListeners() {
        AppUtil.handleOnPressEvent(downloadConfirm, () -> {
            if (linkInput.getText().length() > 3) download(linkInput.getText().toString());
        });
    }

    public interface MapDownloadListener {
        void onMapDownloaded(String path);
    }

    @Override
    public void onAttach(@NonNull Context cnx) {
        super.onAttach(cnx);

        listener = (MapDownloadListener) cnx;
    }
}
