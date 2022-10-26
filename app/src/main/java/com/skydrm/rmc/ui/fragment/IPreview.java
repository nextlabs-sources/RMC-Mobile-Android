package com.skydrm.rmc.ui.fragment;

import android.content.Context;
import android.widget.RelativeLayout;

import java.io.File;

public interface IPreview {
    void buildRender(Context ctx, RelativeLayout mainLayout, File file);

    void start();

    void stop();
}
