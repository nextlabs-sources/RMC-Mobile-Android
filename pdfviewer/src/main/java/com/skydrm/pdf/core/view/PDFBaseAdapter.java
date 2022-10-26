package com.skydrm.pdf.core.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.skydrm.pdf.core.IPagesLoader;
import com.skydrm.pdf.core.page.Page;

import java.util.ArrayList;
import java.util.List;

public abstract class PDFBaseAdapter extends RecyclerView.Adapter<PDFBaseAdapter.ViewHolder> {
    protected IPagesLoader mPagesLoader;
    protected List<Page> mData;

    public PDFBaseAdapter(IPagesLoader loader) {
        this.mPagesLoader = loader;
        this.mData = new ArrayList<>();
    }

    @NonNull
    @Override
    public PDFBaseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        return onCreateItemViewHolder(viewGroup, type);
    }

    @Override
    public void onBindViewHolder(@NonNull PDFBaseAdapter.ViewHolder holder, int position) {
        if (mPagesLoader == null) {
            return;
        }
        holder.itemView.setTag(position);

        if (mData.size() == 0) {
            mPagesLoader.loadPage(holder, position);
        } else {
            if (position < 0 || position > mData.size() - 1) {
                mPagesLoader.loadPage(holder, position);
            } else {
                Page page = mData.get(position);
                if (page != null) {
                    holder.bandData(page);
                } else {
                    mPagesLoader.loadPage(holder, position);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mPagesLoader == null) {
            return 0;
        }
        return mPagesLoader.getPageSize();
    }

    protected abstract PDFBaseAdapter.ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int type);

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bandData(Page page);
    }
}
