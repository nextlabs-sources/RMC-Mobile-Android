package com.skydrm.pdf.core;

import com.skydrm.pdf.core.page.Page;

public interface IPagePool {
    long getMaxSize();

    void put(int idx, Page page);

    Page get(int idx);

    void clearMemory();
}
