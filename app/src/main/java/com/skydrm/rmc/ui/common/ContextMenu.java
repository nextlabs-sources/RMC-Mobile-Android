package com.skydrm.rmc.ui.common;

import android.support.annotation.IntDef;

/**
 * Created by hhu on 5/10/2017.
 */

public class ContextMenu {
    public static final int ACTION_SHARE = 0x1001;
    public static final int ACTION_PROTECT = 0x1002;
    public static final int ACTION_ADD = 0x1003;
    public static final int ACTION_SELECT_PATH = 0x1004;
    public static final int ACTION_CREATE_NEW_FOLDER = 0x1005;
    public static final int ACTION_ADD_PROJECT = 0x1006;

    @IntDef({ACTION_SHARE, ACTION_PROTECT, ACTION_ADD, ACTION_SELECT_PATH,
            ACTION_CREATE_NEW_FOLDER, ACTION_ADD_PROJECT})
    public @interface ChooseAction {

    }

}
