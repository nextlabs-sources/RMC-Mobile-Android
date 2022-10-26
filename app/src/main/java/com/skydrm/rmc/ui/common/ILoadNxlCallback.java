package com.skydrm.rmc.ui.common;

import java.util.List;


public interface ILoadNxlCallback<T> {
    void onPreLoad();

    void onLoadResult(List<T> result);

    void onLoadFailed(Exception e);
}
