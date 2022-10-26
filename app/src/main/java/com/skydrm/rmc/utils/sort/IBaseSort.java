package com.skydrm.rmc.utils.sort;

import java.util.List;

public interface IBaseSort<T> {
    void onSortFile(List<T> target);

    List<T> doSort();
}
