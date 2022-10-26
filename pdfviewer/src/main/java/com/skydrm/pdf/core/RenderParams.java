package com.skydrm.pdf.core;

import android.graphics.Bitmap;

public class RenderParams {
    private static final int MAX_CACHE_SIZE = 8 * 1024;
    private int mCacheSize;
    private Bitmap.Config mConfig;
    private int mPageWidth;
    private int mPageHeight;

    private RenderParams(Builder builder) {
        this.mCacheSize = builder.maxSize;
        this.mConfig = builder.config;
        this.mPageWidth = builder.pageWidth;
        this.mPageHeight = builder.pageHeight;
    }

    public int getMaxCacheSize() {
        return mCacheSize <= 0 ? MAX_CACHE_SIZE : mCacheSize;
    }

    public Bitmap.Config getBitmapConfig() {
        return mConfig;
    }

    public int getPageWidth() {
        return mPageWidth;
    }

    public int getPageHeight() {
        return mPageHeight;
    }

    public static class Builder {
        private int maxSize;
        private Bitmap.Config config = Bitmap.Config.ARGB_8888;
        private int pageWidth;
        private int pageHeight;

        public Builder setMaxCacheSize(int size) {
            this.maxSize = size;
            return this;
        }

        public Builder setConfig(Bitmap.Config config) {
            this.config = config;
            return this;
        }

        public Builder setPageWidth(int width) {
            this.pageWidth = width;
            return this;
        }

        public Builder setPageHeight(int height) {
            this.pageHeight = height;
            return this;
        }

        public RenderParams build() {
            return new RenderParams(this);
        }
    }
}
