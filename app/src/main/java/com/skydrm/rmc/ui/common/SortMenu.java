package com.skydrm.rmc.ui.common;

import android.content.Context;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.base.BaseSortMenu;

import java.util.Arrays;
import java.util.List;

public class SortMenu extends BaseSortMenu {

    public SortMenu(Context ctx) {
        super(ctx);
    }

    @Override
    protected List<String> getData() {
        if (mCtx == null) {
            return null;
        }
        String[] values = mCtx.getResources().getStringArray(R.array.sort_menu_common);
        if (values.length == 0) {
            return null;
        }
        return Arrays.asList(values);
    }

}
