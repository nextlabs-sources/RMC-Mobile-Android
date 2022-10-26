package com.skydrm.pdf.core.task;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.skydrm.pdf.core.IPagePool;
import com.skydrm.pdf.core.page.Page;
import com.skydrm.pdf.core.render.PDFRenderWrapper;
import com.skydrm.pdf.core.render.RenderTask;

public class DecodingTask extends AsyncTask<Void, Void, Page> implements Runnable {
    private PDFRenderWrapper mRenderCore;
    private RenderTask mTask;
    private IPagePool mPagePool;
    private boolean running;

    public DecodingTask(PDFRenderWrapper core, RenderTask task, IPagePool pagePool) {
        this.mRenderCore = core;
        this.mTask = task;
        this.mPagePool = pagePool;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    protected Page doInBackground(Void... voids) {
        return proceedTask(mTask);
    }

    @Override
    protected void onPostExecute(Page page) {
        super.onPostExecute(page);
        if (page != null) {
            if (running) {
                mTask.holder.bandData(page);
                cacheIntoPool(mTask.page, page);
            } else {
                Bitmap content = page.getContent();
                if (content != null && !content.isRecycled()) {
                    content.recycle();
                }
            }
        }
    }

    @Override
    public void run() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

        if (mRenderCore == null) {
            return null;
        }
        mRenderCore.renderPage(content, task.page);

        return new Page(task.page, content, task.print);
    }

    private void cacheIntoPool(int idx, Page page) {
        if (mPagePool == null) {
            return;
        }
        mPagePool.put(idx, page);
    }
}
