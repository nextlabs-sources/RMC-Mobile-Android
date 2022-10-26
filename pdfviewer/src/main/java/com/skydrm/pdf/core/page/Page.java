package com.skydrm.pdf.core.page;

import android.graphics.Bitmap;

import java.util.Objects;

public class Page {
    private int index;
    private Bitmap content;
    private boolean thumbNail;

    public Page(int index, Bitmap content, boolean thumbNail) {
        this.index = index;
        this.content = content;
        this.thumbNail = thumbNail;
    }

    public int getIndex() {
        return index;
    }

    public Bitmap getContent() {
        return content;
    }

    public boolean isThumbNail() {
        return thumbNail;
    }

    public void recycle() {
        if (content != null && !content.isRecycled()) {
            content.recycle();
        }
        index = -1;
    }

    public boolean isRecycled() {
        return content != null && content.isRecycled();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return index == page.index &&
                thumbNail == page.thumbNail &&
                Objects.equals(content, page.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, content, thumbNail);
    }

    @Override
    public String toString() {
        return "Page{" +
                "index=" + index +
                '}';
    }
}
