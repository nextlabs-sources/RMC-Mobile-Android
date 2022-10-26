package com.skydrm.rmc.ui.project.feature.service.share;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProjectSelectAdapter extends RecyclerView.Adapter<ProjectSelectAdapter.ViewHolder> implements IDestroyable {
    private static final int TYPE_HEADER = 0x01;
    private static final int TYPE_ITEM = 0x02;
    private List<IProject> mData = new ArrayList<>();
    private List<IProject> mOwnerByMe = new ArrayList<>();
    private List<IProject> mOwnerByOther = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;
    private int mIgnoreId;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    void setIgnoreDisplayProjectId(int id) {
        mIgnoreId = id;
    }

    void setOwnerByMeData(List<IProject> data) {
        mOwnerByMe.clear();
        if (data != null) {
            mOwnerByMe.addAll(data);
        }
        mData.clear();
        mData.addAll(mOwnerByMe);
        mData.addAll(mOwnerByOther);

        checkAndFilter(mData);
        notifyDataSetChanged();
    }

    void setOwnerByOtherData(List<IProject> data) {
        mOwnerByOther.clear();
        if (data != null) {
            mOwnerByOther.addAll(data);
        }
        mData.clear();
        mData.addAll(mOwnerByMe);
        mData.addAll(mOwnerByOther);

        checkAndFilter(mData);
        notifyDataSetChanged();
    }

    private void checkAndFilter(List<IProject> projects) {
        if (projects == null || projects.size() == 0) {
            return;
        }
        if (mIgnoreId == -1) {
            return;
        }
        Iterator<IProject> it = projects.iterator();
        while (it.hasNext()) {
            IProject next = it.next();
            if (next.getId() == mIgnoreId) {
                it.remove();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_header_project_select, null, false));
            case TYPE_ITEM:
                return new ItemViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_project_select, null, false));
        }
        throw new IllegalArgumentException("Unrecognized view type " + viewType + " performed,TYPE_HEADER,TYPE_ITEM are limited.");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 0) {
            return;
        }
        holder.bandData(mData.get(position - 1));
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    @Override
    public void onReleaseResource() {
        if (mOnItemClickListener != null) {
            mOnItemClickListener = null;
        }
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bandData(IProject p);
    }

    class HeaderViewHolder extends ViewHolder {

        HeaderViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bandData(IProject p) {

        }
    }

    class ItemViewHolder extends ViewHolder {
        private final ImageView mIvIcon;
        private final TextView mTvProjectName;
        private final TextView mTvDate;
        private final TextView mTvFileNum;

        ItemViewHolder(View itemView) {
            super(itemView);
            mIvIcon = itemView.findViewById(R.id.iv_icon);
            mTvProjectName = itemView.findViewById(R.id.tv_project_name);
            mTvDate = itemView.findViewById(R.id.tv_date);
            mTvFileNum = itemView.findViewById(R.id.tv_file_num);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener == null) {
                        return;
                    }
                    int pos = getLayoutPosition() - 1;
                    if (pos == -1) {
                        return;
                    }
                    mOnItemClickListener.onItemClick(mData.get(pos), pos);
                }
            });
        }

        @Override
        void bandData(IProject p) {
            if (p == null) {
                return;
            }
            mTvProjectName.setText(p.getDisplayName());
            mTvDate.setText(TimeUtil.formatLibraryFileDate(p.getCreationTime()));
            mTvFileNum.setText(String.valueOf(p.getTotalFiles()));
            if (p.isOwnedByMe()) {
                mIvIcon.setImageResource(R.drawable.icon_project_created_by_me);
            } else {
                mIvIcon.setImageResource(R.drawable.icon_project_invited_by_other);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(IProject p, int pos);
    }
}
