package com.skydrm.rmc.ui.service.log;

public interface ILoadCallback<T, E> {
    void onLoading();

    void onResult(T result, int total);

    void onError(E error);
}
