package com.skydrm.pdf.core;

import com.skydrm.pdf.core.page.Page;
import com.skydrm.pdf.core.view.PDFBaseAdapter;

public interface IPagesLoader {
    int getPageSize();

    Page loadPage(PDFBaseAdapter.ViewHolder holder, int position);

    void start();

    void stop();
}
