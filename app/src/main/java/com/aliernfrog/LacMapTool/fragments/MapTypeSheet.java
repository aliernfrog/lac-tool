package com.aliernfrog.LacMapTool.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliernfrog.LacMapTool.R;
import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MapTypeSheet extends BottomSheetDialogFragment {
    private MapTypeListener listener;

    RadioGroup typeGroup;
    Button doneButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_map_type, container, false);

        typeGroup = view.findViewById(R.id.mapType_group);
        doneButton = view.findViewById(R.id.mapType_done);

        if (getArguments() != null) ((RadioButton)typeGroup.getChildAt(getArguments().getInt("mapTypeInt"))).setChecked(true);

        setListeners();

        return view;
    }

    void finishChoosing() {
        int checkedIndex = typeGroup.indexOfChild(typeGroup.findViewById(typeGroup.getCheckedRadioButtonId()));
        listener.onMapTypeChoose(checkedIndex);
        dismiss();
    }

    void setListeners() {
        AppUtil.handleOnPressEvent(doneButton, this::finishChoosing);
    }

    public interface MapTypeListener {
        void onMapTypeChoose(int type);
    }

    @Override
    public void onAttach(@NonNull Context cnx) {
        super.onAttach(cnx);

        listener = (MapTypeListener) cnx;
    }
}
