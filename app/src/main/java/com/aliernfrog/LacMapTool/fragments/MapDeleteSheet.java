package com.aliernfrog.LacMapTool.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliernfrog.LacMapTool.R;
import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MapDeleteSheet extends BottomSheetDialogFragment {
    private MapDeleteListener listener;

    Button cancel;
    Button delete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_map_delete, container, false);

        cancel = view.findViewById(R.id.mapDelete_cancel);
        delete = view.findViewById(R.id.mapDelete_confirm);

        setListeners();

        return view;
    }

    void setListeners() {
        AppUtil.handleOnPressEvent(cancel, this::dismiss);
        AppUtil.handleOnPressEvent(delete, () -> {
            listener.onDeleteConfirm();
            dismiss();
        });
    }

    public interface MapDeleteListener {
        void onDeleteConfirm();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (MapDeleteListener) context;
    }
}
