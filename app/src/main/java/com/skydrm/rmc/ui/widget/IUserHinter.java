package com.skydrm.rmc.ui.widget;

/**
 * Created by hhu on 3/22/2017.
 */

public interface IUserHinter<T> {
    void hintUser(T show);

    void hintUser(T show, String msg);

    void removeHint(T remove);
}
