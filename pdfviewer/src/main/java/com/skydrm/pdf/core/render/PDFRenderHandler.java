package com.skydrm.pdf.core.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Message;

import com.skydrm.pdf.core.IPagePool;
import com.skydrm.pdf.core.RenderParams;
import com.skydrm.pdf.core.cache.LRUPageCachePool;
import com.skydrm.pdf.core.page.Page;
import com.skydrm.pdf.core.task.DecodingTask;
import com.skydrm.pdf.core.view.PDFBaseAdapter;

public class PDFRenderHandler extends Handler {
    private static final int MSG_RENDER_TASK = 1;
    private PDFRenderWrapper mRenderCore;
    private IPagePool mPagePool;

    private boolean running;

    public PDFRenderHandler(Context ctx, String path, int maxPagePoolSize,
                            PDFRenderWrapper.InitCallback callback) {
        this.mRenderCore = new PDFRenderWrapper(ctx, path, callback);
        this.mPagePool = new LRUPageCachePool(maxPagePoolSize);
    }

    public PDFRenderWrapper getRenderCore() {
        return mRenderCore;
    }

    public Page tryGetPage(PDFBaseAdapter.ViewHolder holder, int page, int width, int height) {
        Page fromPool = getFromPool(page);
        if (fromPool != null) {
            return fromPool;
        }

        addRenderingTask(holder, page, width, height);

        return new Page(page, null, false);
    }

    private void addRenderingTask(PDFBaseAdapter.ViewHolder holder, int page, int width, int height) {
        RenderTask task = new RenderTask(holder, page, false, width, height);
        DecodingTask dt = new DecodingTask(mRenderCore, task, mPagePool);
        dt.setRunning(running);
        dt.run();
//        Message msg = obtainMessage(MSG_RENDER_TASK, task);
//        sendMessage(msg);
    }

    public int getPageCount() {
        return mRenderCore.getPageCount();
    }

    public RenderParams tryGetRenderParams() {
        int height = 0;
        int width = 0;
        try (PdfRenderer.Page page = mRenderCore.getPage(0)) {
            if (page != null) {
                width = page.getWidth();
                height = page.getHeight();
            }
        }
        return new RenderParams.Builder()
                .setPageHeight(height)
                .setPageWidth(width)
                .build();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        RenderTask task = (RenderTask) msg.obj;
        Page page = proceedTask(task);
        if (page != null) {
            if (running) {
                task.holder.bandData(page);
                cacheIntoPool(task.page, page);
            } else {
                Bitmap content = page.getContent();
                if (content != null && !content.isRecycled()) {
                    content.recycle();
                }
            }
        }
    }

    private Page getFromPool(int idx) {
        Page content = mPagePool.get(idx);
        if (content != null) {
            if (content.isRecycled()) {
                return null;
            }
            return content;
        }
        return null;
    }

    private void cacheIntoPool(int idx, Page page) {
        mPagePool.put(idx, page);
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
        removeMessages(MSG_RENDER_TASK);
        if (mPagePool != null) {
            mPagePool.clearMemory();
            mPagePool = null;
        }
    }

    private Page proceedTask(RenderTask task) {
        if (task == null) {
            return null;
        }

        if (task.print) {
            mRenderCore.setPrintRenderMode();
        } else {
            mRenderCore.setDisplayRenderMode();
        }

        int w = Math.round(task.width);
        int h = Math.round(task.height);

        Bitmap content = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        content.eraseColor(Color.parseColor("#00000000"));
        mRenderCore.renderPage(content, task.page);

        return new Page(task.page, content, task.print);
    }
}
