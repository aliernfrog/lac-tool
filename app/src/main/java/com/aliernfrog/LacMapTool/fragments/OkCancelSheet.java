package com.aliernfrog.LacMapTool.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliernfrog.LacMapTool.R;
import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class OkCancelSheet extends BottomSheetDialogFragment {
    private OkCancelListener listener;

    TextView textView;
    Button okButton;
    Button cancelButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_okcancel, container, false);

        textView = view.findViewById(R.id.okCancel_text);
        okButton = view.findViewById(R.id.okCancel_ok);
        cancelButton = view.findViewById(R.id.okCancel_cancel);

        if (getArguments() != null) textView.setText(getArguments().getString("text"));
        AppUtil.handleOnPressEvent(okButton, () -> listener.onOkClick());
        AppUtil.handleOnPressEvent(cancelButton, this::dismiss);

        return view;
    }

    public interface OkCancelListener {
        void onOkClick();
    }

    @Override
    public void onAttach(@NonNull Context cnx) {
        super.onAttach(cnx);

        listener = (OkCancelListener) cnx;
    }
}