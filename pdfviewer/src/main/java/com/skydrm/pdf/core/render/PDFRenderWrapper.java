package com.skydrm.pdf.core.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import com.skydrm.pdf.core.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;

public class PDFRenderWrapper {
    private PdfRenderer mRenderCore;
    private int mPageCount;
    private int mRenderMode = PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY;

    public PDFRenderWrapper(Context ctx, String path, final InitCallback callback) {
        InitTask initTask = new InitTask(ctx, path, new InitTask.Callback() {
            @Override
            public void onInitProgress(int progress) {
                if (callback != null) {
                    callback.onInitProgress(progress);
                }
            }

            @Override
            public void onSuccess(ParcelFileDescriptor descriptor) {
                try {
                    mRenderCore = new PdfRenderer(descriptor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mRenderCore == null) {
                    if (callback != null) {
                        callback.onFailed(new Exception("Invalid file performed."));
                    }
                    return;
                }
                mPageCount = mRenderCore.getPageCount();
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) {
                    callback.onFailed(e);
                }
            }
        });
        initTask.run();
    }

    public void setDisplayRenderMode() {
        this.mRenderMode = PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY;
    }

    public void setPrintRenderMode() {
        this.mRenderMode = PdfRenderer.Page.RENDER_MODE_FOR_PRINT;
    }

    public synchronized PdfRenderer.Page getPage(int idx) {
        if (idx < 0 || idx > mPageCount - 1) {
            return null;
        }
        return mRenderCore.openPage(idx);
    }

    public synchronized int getPageCount() {
        return mPageCount;
    }

    public synchronized void renderPage(Bitmap dest, int idx) {
        try (PdfRenderer.Page page = getPage(idx)) {
            if (page == null) {
                return;
            }
            page.render(dest, null, null, mRenderMode);
        }
    }

    public synchronized void renderPage(Bitmap dest, Rect destClip, Matrix transform, int idx) {
        try (PdfRenderer.Page page = getPage(idx)) {
            if (page == null) {
                return;
            }
            page.render(dest, destClip, transform, mRenderMode);
        }
    }

    private static ParcelFileDescriptor getParcelFileDescriptor(Context ctx, String path) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor;

        File pdfCopy = new File(path);

        if (pdfCopy.exists()) {
            parcelFileDescriptor = ParcelFileDescriptor.open(pdfCopy, ParcelFileDescriptor.MODE_READ_ONLY);
            return parcelFileDescriptor;
        }

        if (Utils.isAnAsset(path)) {
            //pdfCopy = new File(context.getCacheDir(), path);
            pdfCopy = Utils.fileFromAsset(ctx, path);
            parcelFileDescriptor = ParcelFileDescriptor.open(pdfCopy, ParcelFileDescriptor.MODE_READ_ONLY);
        } else {
            URI uri = URI.create(String.format("file://%s", path));
            parcelFileDescriptor = ctx.getContentResolver().openFileDescriptor(Uri.parse(uri.toString()), "rw");
        }
        return parcelFileDescriptor;
    }

    static class InitTask extends AsyncTask<Void, Integer, ParcelFileDescriptor> implements Runnable {
        private WeakReference<Context> mCtx;
        private String mPath;
        private Callback mCallback;
        private Exception mExp;

        public InitTask(Context ctx, String path,
                        Callback callback) {
            this.mCtx = new WeakReference<>(ctx);
            this.mPath = path;
            this.mCallback = callback;
        }

        @Override
        protected ParcelFileDescriptor doInBackground(Void... voids) {
            Context ctx = mCtx.get();
            if (ctx == null) {
                return null;
            }
            if (mPath == null || mPath.isEmpty()) {
                return null;
            }
            try {
                publishProgress(10);
                return PDFRenderWrapper.getParcelFileDescriptor(ctx, mPath);
            } catch (IOException e) {
                mExp = e;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mCallback != null) {
                mCallback.onInitProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(ParcelFileDescriptor descriptor) {
            super.onPostExecute(descriptor);
            if (descriptor != null) {
                if (mCallback != null) {
                    mCallback.onSuccess(descriptor);
                }
            } else {
                if (mCallback != null) {
                    mCallback.onFailed(mExp == null ?
                            new Exception("Unknown error") : mExp);
                }
            }
        }

        @Override
        public void run() {
            this.execute();
        }

        public interface Callback {
            void onInitProgress(int progress);

            void onSuccess(ParcelFileDescriptor descriptor);

            void onFailed(Exception e);
        }
    }

    public interface InitCallback {
        void onInitProgress(int progress);

        void onSuccess();

        void onFailed(Exception e);
    }
}
