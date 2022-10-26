package com.skydrm.rmc.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.base.NavigationType;
import com.skydrm.rmc.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RepoSpaceSizeAdapter2 extends RecyclerView.Adapter<RepoSpaceSizeAdapter2.ViewHolder> {
    private List<RepoSpaceSizeItem> mData = new ArrayList<>();
    private List<RepoSpaceSizeItem> mDriveData = new ArrayList<>();
    private List<RepoSpaceSizeItem> mWorkSpaceData = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setData(List<RepoSpaceSizeItem> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void setMyDriveData(List<RepoSpaceSizeItem> data) {
        mData.clear();
        mDriveData.clear();
        if (data != null) {
            mDriveData.addAll(data);
        }

        mData.addAll(mDriveData);
        mData.addAll(mWorkSpaceData);

        notifyDataSetChanged();
    }

    public void setWorkSpaceData(List<RepoSpaceSizeItem> data) {
        mData.clear();
        mWorkSpaceData.clear();
        if (data != null) {
            mWorkSpaceData.addAll(data);
        }

        mData.addAll(mDriveData);
        mData.addAll(mWorkSpaceData);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
        return new NormalItemViewHolder(LayoutInflater.from(group.getContext()).inflate(R.layout.item_repo_space_size,
                group, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        holder.bindData(mData.get(i));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindData(RepoSpaceSizeItem item);
    }

    class NormalItemViewHolder extends ViewHolder {
        private final ImageView mIvRepoIcon;
        private final TextView mTvRepoName;
        private final TextView mTvFileNum;
        private final TextView mTvRepoUsage;

        NormalItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mIvRepoIcon = itemView.findViewById(R.id.iv_repo_icon);
            mTvRepoName = itemView.findViewById(R.id.tv_repo_name);
            mTvFileNum = itemView.findViewById(R.id.tv_file_num);
            mTvRepoUsage = itemView.findViewById(R.id.tv_repo_usage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getLayoutPosition();
                    if (pos == -1) {
                        return;
                    }
                    RepoSpaceSizeItem item = mData.get(pos);
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(item.toNavigationType(v.getContext()));
                    }
                }
            });
        }

        @Override
        void bindData(RepoSpaceSizeItem item) {
            if (item == null) {
                return;
            }
            mIvRepoIcon.setImageResource(item.getRepoDrawableId());
            mTvRepoName.setText(item.getRepoName());
            int repoTotalFiles = item.getRepoTotalFiles();
            if (repoTotalFiles == -1) {
                mTvFileNum.setVisibility(View.INVISIBLE);
            } else {
                mTvFileNum.setText(String.format(Locale.getDefault(), "%d files", repoTotalFiles));
            }
            mTvRepoUsage.setText(FileUtils.transparentFileSize(item.getRepoUsage()));
        }

    }

    public interface OnItemClickListener {
        void onItemClick(NavigationType type);
    }
}
