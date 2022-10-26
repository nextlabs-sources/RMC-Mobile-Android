package com.skydrm.rmc.engine.Render;

import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.skydrm.pdf.PDFView;

import java.io.File;

public class PDFRender implements IFileRender {
    private RelativeLayout mRoot;
    private File mWorkingFile;

    private PDFView mPDFView;

    public PDFRender(RelativeLayout root, File workingFile) {
        this.mRoot = root;
        this.mWorkingFile = workingFile;

        initRender(root);
    }

    @Override
    public void fileRender() {
        if (mWorkingFile == null || !mWorkingFile.exists()) {
            return;
        }
        if (mWorkingFile.isDirectory()) {
            return;
        }
        mPDFView.startRender(mWorkingFile.getPath());
    }

    private void initRender(RelativeLayout root) {
        if (root == null) {
            return;
        }
        mPDFView = new PDFView(mRoot.getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mRoot.addView(mPDFView, layoutParams);
    }

    public void stopRender() {
        if (mPDFView != null) {
            mPDFView.stopRender();
            mPDFView = null;
        }
    }
}
