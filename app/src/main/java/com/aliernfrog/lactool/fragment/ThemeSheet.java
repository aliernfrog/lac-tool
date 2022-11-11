package com.aliernfrog.lactool.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.aliernfrog.LacMapTool.R;
import com.aliernfrog.lactool.utils.AppUtil;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ThemeSheet extends BottomSheetDialogFragment {
    RadioButton themeSystem;
    RadioButton themeLight;
    RadioButton themeDark;
    Button confirmButton;

    Context context;

    SharedPreferences config;
    SharedPreferences.Editor configEdit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_theme, container, false);

        themeSystem = view.findViewById(R.id.theme_system);
        themeLight = view.findViewById(R.id.theme_light);
        themeDark = view.findViewById(R.id.theme_dark);
        confirmButton = view.findViewById(R.id.theme_confirm);

        context = getActivity();

        if (context != null) {
            config = context.getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
            configEdit = config.edit();
        }

        getCurrent();
        setListeners();

        return view;
    }

    void getCurrent() {
        int currentTheme = AppCompatDelegate.getDefaultNightMode();
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) themeSystem.setChecked(true);
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) themeLight.setChecked(true);
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) themeDark.setChecked(true);
    }

    void setTheme(int theme) {
        AppCompatDelegate.setDefaultNightMode(theme);
        configEdit.putInt("appTheme", theme);
        configEdit.commit();
    }

    void setListeners() {
        themeSystem.setOnClickListener(v -> setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
        themeLight.setOnClickListener(v -> setTheme(AppCompatDelegate.MODE_NIGHT_NO));
        themeDark.setOnClickListener(v -> setTheme(AppCompatDelegate.MODE_NIGHT_YES));
        AppUtil.handleOnPressEvent(confirmButton, this::dismiss);
    }
}
