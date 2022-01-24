package com.aliernfrog.LacMapTool.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliernfrog.LacMapTool.R;
import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.FileUtil;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;

public class MapPickerSheet extends BottomSheetDialogFragment {
    private MapPickerListener listener;

    Button pickMap;
    Button downloadMap;
    TextView noImportedMaps;
    LinearLayout root;

    Context context;
    String mapsPath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_map_picker, container, false);

        pickMap = view.findViewById(R.id.mapPicker_pick);
        downloadMap = view.findViewById(R.id.mapPicker_download);
        noImportedMaps = view.findViewById(R.id.mapPicker_noMapsWarning);
        root = view.findViewById(R.id.mapPicker_root);

        context = getActivity();
        if (getArguments() != null) mapsPath = getArguments().getString("mapsPath");

        getImportedMaps();
        setListeners();

        return view;
    }

    void getImportedMaps() {
        File[] files = new File(mapsPath).listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.getName().endsWith(".txt")) {
                ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.inflate_map, root, false);
                setMapView(file, view);
                noImportedMaps.setVisibility(View.GONE);
            }
        }
    }

    void setMapView(File map, ViewGroup view) {
        TextView name = view.findViewById(R.id.map_name);
        TextView size = view.findViewById(R.id.map_size);
        ImageView thumbnail = view.findViewById(R.id.map_thumbnail);
        String mapName = FileUtil.removeExtension(map.getName());
        String mapSize = (map.length()/1024)+" KB";
        String thumbnailPath = FileUtil.removeExtension(map.getPath())+".jpg";
        File thumbailFile = new File(thumbnailPath);
        name.setText(mapName);
        size.setText(mapSize);
        if (thumbailFile.exists()) {
            Bitmap thumbBitmap = BitmapFactory.decodeFile(thumbailFile.getAbsolutePath());
            thumbnail.setImageBitmap(thumbBitmap);
        }
        AppUtil.handleOnPressEvent(view, () -> {
            listener.onMapPicked(map.getPath());
            dismiss();
        });
        root.addView(view);
    }

    void pickMapFile() {
        listener.onFilePickRequested();
        dismiss();
    }

    void setListeners() {
        AppUtil.handleOnPressEvent(pickMap, this::pickMapFile);
        AppUtil.handleOnPressEvent(downloadMap, () -> {
            dismiss();
            MapDownloadSheet mapDownloadSheet = new MapDownloadSheet();
            mapDownloadSheet.show(getParentFragmentManager(), "map_download");
        });
        AppUtil.handleOnPressEvent(noImportedMaps);
    }

    public interface MapPickerListener {
        void onMapPicked(String path);
        void onFilePickRequested();
    }

    @Override
    public void onAttach(@NonNull Context cnx) {
        super.onAttach(cnx);

        listener = (MapPickerListener) cnx;
    }
}
