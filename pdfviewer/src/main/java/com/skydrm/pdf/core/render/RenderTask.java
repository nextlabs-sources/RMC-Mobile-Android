package com.skydrm.pdf.core.render;


import com.skydrm.pdf.core.view.PDFBaseAdapter;

public class RenderTask {
    public PDFBaseAdapter.ViewHolder holder;
    public int page;
    public boolean print;
    public float width, height;

    public RenderTask(PDFBaseAdapter.ViewHolder holder, int page,
                      boolean print, float width, float height) {
        this.holder = holder;
        this.page = page;
        this.print = print;
        this.width = width;
        this.height = height;
    }
}
