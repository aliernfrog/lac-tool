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

import com.aliernfrog.LacMapTool.MapsActivity;
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

    MapsActivity context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_map_picker, container, false);

        pickMap = view.findViewById(R.id.mapPicker_pick);
        downloadMap = view.findViewById(R.id.mapPicker_download);
        noImportedMaps = view.findViewById(R.id.mapPicker_noMapsWarning);
        root = view.findViewById(R.id.mapPicker_root);

        context = (MapsActivity) getActivity();

        getImportedMaps();
        setListeners();

        return view;
    }

    void getImportedMaps() {
        File[] files = context.getImportedMaps();
        for (File map : files) {
            if (map.getName().endsWith(".txt")) {
                ViewGroup view = (ViewGroup) context.getLayoutInflater().inflate(R.layout.map, root, false);
                addMapView(map, view);
                noImportedMaps.setVisibility(View.GONE);
            }
        }
    }

    void addMapView(File map, ViewGroup view) {
        TextView name = view.findViewById(R.id.map_name);
        ImageView thumbnail = view.findViewById(R.id.map_thumbnail);
        String mapName = FileUtil.removeExtension(map.getName());
        String thumbnailPath = FileUtil.removeExtension(map.getPath())+".jpg";
        File thumbailFile = new File(thumbnailPath);
        name.setText(mapName);
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
        context.pickMapFile();
        dismiss();
    }

    void setListeners() {
        AppUtil.handleOnPressEvent(pickMap, this::pickMapFile);
        AppUtil.handleOnPressEvent(downloadMap, () -> {
            dismiss();
            MapDownloadSheet mapDownloadSheet = new MapDownloadSheet();
            mapDownloadSheet.show(context.getSupportFragmentManager(), "map_download");
        });
        AppUtil.handleOnPressEvent(noImportedMaps);
    }

    public interface MapPickerListener {
        void onMapPicked(String path);
    }

    @Override
    public void onAttach(@NonNull Context cnx) {
        super.onAttach(cnx);

        listener = (MapPickerListener) cnx;
    }
}
