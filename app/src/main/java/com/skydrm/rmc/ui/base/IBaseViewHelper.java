package com.skydrm.rmc.ui.base;

import android.content.Context;
import android.view.View;

/**
 * Created by hhu on 5/3/2017.
 */

public interface IBaseViewHelper {
    View getCurrentLayout();

    void restoreView();

    void showLayout(View view);

    View inflate(int layoutId);

    Context getContext();

    View getView();
}
