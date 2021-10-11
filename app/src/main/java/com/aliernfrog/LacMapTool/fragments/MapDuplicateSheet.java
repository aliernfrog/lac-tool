package com.aliernfrog.LacMapTool.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliernfrog.LacMapTool.MapsActivity;
import com.aliernfrog.LacMapTool.R;
import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.FileUtil;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;

public class MapDuplicateSheet extends BottomSheetDialogFragment {
    private MapDuplicateListener listener;

    EditText nameInput;
    Button duplicateConfirm;

    MapsActivity context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_map_duplicate, container, false);

        nameInput = view.findViewById(R.id.mapDuplicate_nameInput);
        duplicateConfirm = view.findViewById(R.id.mapDuplicate_duplicate);

        context = (MapsActivity) getActivity();

        getMapName();
        setListeners();

        return view;
    }

    void getMapName() {
        File file = new File(context.getMapPath());
        String name = FileUtil.removeExtension(file.getName());
        nameInput.setText(name);
    }

    void duplicate(String name) {
        listener.onMapDuplicateConfirm(name);
        dismiss();
    }

    void setListeners() {
        AppUtil.handleOnPressEvent(duplicateConfirm, () -> {
            if (nameInput.getText().length() > 0) duplicate(nameInput.getText().toString());
        });
    }

    public interface MapDuplicateListener {
        void onMapDuplicateConfirm(String name);
    }

    @Override
    public void onAttach(@NonNull Context cnx) {
        super.onAttach(cnx);

        listener = (MapDuplicateListener) cnx;
    }
}
