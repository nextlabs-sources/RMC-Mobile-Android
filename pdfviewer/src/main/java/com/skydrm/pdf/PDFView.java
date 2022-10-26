package com.skydrm.pdf;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.skydrm.pdf.core.IPagesLoader;
import com.skydrm.pdf.core.page.PagesLoader;
import com.skydrm.pdf.core.render.PDFRenderWrapper;
import com.skydrm.pdf.core.view.PDFContentAdapter;
import com.skydrm.pdf.core.view.PDFRecyclerView;
import com.skydrm.pdf.core.view.PDFToolbar;


public class PDFView extends LinearLayout implements PDFToolbar.OnDrawToggleClickListener {
    private static final int MAX_CONTENT_PAGE_CACHE_SIZE = 6;
    private String mPath;
    private ProgressBar mPbProgress;
    private PDFRecyclerView mPDFContent;
    private PDFToolbar mPDFToolbar;

    private IPagesLoader mPageLoader;
    private LinearLayoutManager mPDFContentLayoutManager;

    public PDFView(@NonNull Context context) {
        this(context, null);
    }

    public PDFView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PDFView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(HORIZONTAL);

        initView();
    }

    private void initView() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.layout_pdf_view,
                this, true);
        mPbProgress = root.findViewById(R.id.pb_progress);

        mPDFToolbar = root.findViewById(R.id.pdf_toolbar);
        mPDFToolbar.setVisibility(GONE);


        mPDFContent = root.findViewById(R.id.rv_pdf_main);
        mPDFContentLayoutManager = new LinearLayoutManager(getContext());
        mPDFContent.setLayoutManager(mPDFContentLayoutManager);
    }

    public void startRender(String path) {
        this.mPath = path;
        mPbProgress.setVisibility(VISIBLE);
        mPageLoader = new PagesLoader(getContext(), path, MAX_CONTENT_PAGE_CACHE_SIZE, null,
                new PDFRenderWrapper.InitCallback() {
                    @Override
                    public void onInitProgress(int progress) {
                        mPbProgress.setProgress(progress);
                    }

                    @Override
                    public void onSuccess() {
                        mPbProgress.setVisibility(GONE);

                        mPDFContent.setAdapter(new PDFContentAdapter(mPageLoader));
                        mPageLoader.start();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        mPbProgress.setVisibility(GONE);

                        Toast.makeText(getContext().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void stopRender() {
        if (mPageLoader != null) {
            mPageLoader.stop();
            mPageLoader = null;
        }
    }

    @Override
    public void onToggleClick() {

    }

    private void selectContentItem(int pos) {
        mPDFContentLayoutManager.scrollToPosition(pos);
    }
}
