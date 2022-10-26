package com.skydrm.rmc.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.utils.commonUtils.IconHelper;

import java.util.ArrayList;
import java.util.List;

public class RepositorySelectAdapter extends RecyclerView.Adapter<RepositorySelectAdapter.ViewHolder> implements IDestroyable {
    private static final int TYPE_LIBRARY_ITEM = 0x01;
    private static final int TYPE_REPO_NORMAL_ITEM = 0x02;
    private static final int TYPE_REPO_GROUP_ITEM = 0x03;

    private Context mCtx;
    private List<BoundService> mData = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public RepositorySelectAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public void setData(List<BoundService> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_LIBRARY_ITEM;
        }
        if (position == 1) {
            return TYPE_REPO_GROUP_ITEM;
        }
        return TYPE_REPO_NORMAL_ITEM;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LIBRARY_ITEM) {
            return new LibraryItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_repo_select_library, parent, false));
        } else if (viewType == TYPE_REPO_NORMAL_ITEM) {
            return new NormalItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_repo_normal_item, parent, false));
        } else if (viewType == TYPE_REPO_GROUP_ITEM) {
            return new GroupItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_repo_group_item, parent, false));
        }
        throw new IllegalArgumentException("Unrecognized view type " + viewType + " performed.");
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            return;
        }
        holder.bindData(mData.get(position - 1));
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

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bindData(BoundService s);
    }

    class NormalItemViewHolder extends ViewHolder {
        private final ImageView ivIcon;
        private final TextView tvRepoName;
        private final TextView tvRepoAccount;

        NormalItemViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvRepoName = itemView.findViewById(R.id.tv_repo_name);
            tvRepoAccount = itemView.findViewById(R.id.tv_repo_account);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener == null) {
                        return;
                    }
                    int pos = getLayoutPosition();
                    if (pos == -1 || pos == 0) {
                        return;
                    }
                    mOnItemClickListener.onNormalItemClick(mData.get(pos - 1), pos);
                }
            });
        }

        @Override
        void bindData(BoundService s) {
            if (s.type == BoundService.ServiceType.MYDRIVE) {
                tvRepoName.setText(s.getDisplayName());
            } else {
                tvRepoName.setText(s.rmsNickName);
            }
            tvRepoAccount.setText(s.account);
            IconHelper.setRepoThumbnail(s, ivIcon);
        }
    }

    class GroupItemViewHolder extends NormalItemViewHolder {
        private final TextView tvRepoDesc;

        GroupItemViewHolder(View itemView) {
            super(itemView);
            tvRepoDesc = itemView.findViewById(R.id.tv_repo_desc);
        }

        @Override
        void bindData(BoundService s) {
            super.bindData(s);
            if (getItemCount() > 2) {
                tvRepoDesc.setText(mCtx.getString(R.string.connected_repositories));
            } else {
                tvRepoDesc.setText(mCtx.getString(R.string.connected_repository));
            }
        }
    }

    class LibraryItemViewHolder extends ViewHolder {

        LibraryItemViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onLibraryItemClick();
                    }
                }
            });
        }

        @Override
        void bindData(BoundService s) {

        }
    }

    public interface OnItemClickListener {
        void onLibraryItemClick();

        void onNormalItemClick(BoundService s, int pos);
    }
}
