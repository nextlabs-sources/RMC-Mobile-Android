package com.skydrm.rmc.ui.service.favorite.model;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NxFileBase;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.ui.service.favorite.model.bean.FavoriteItem;
import com.skydrm.rmc.ui.myspace.sharewithme.OnItemClickListener;
import com.skydrm.rmc.ui.widget.swipelayout.SwipeMenuLayout;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.ViewUtils;
import com.skydrm.rmc.utils.commonUtils.IconHelper;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by hhu on 8/23/2017.
 */

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> implements IDestroyable {
    private static final int TYPE_NORMAL_ITEM = 0x001;
    private static final int TYPE_GROUP_ITEM = 0x002;

    private List<FavoriteItem> mData = new ArrayList<>();
    private OnItemClickListener<IFavoriteFile> mOnItemClickListener;

    public void setData(List<FavoriteItem> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<IFavoriteFile> listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_GROUP_ITEM;
        } else {
            String currentTitle = mData.get(position).title;
            boolean isDifferent = !mData.get(position - 1).title.equals(currentTitle);
            return isDifferent ? TYPE_GROUP_ITEM : TYPE_NORMAL_ITEM;
        }
    }

    @NonNull
    @Override
    public FavoriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_NORMAL_ITEM:
                return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.swipe_item_favorite, parent, false));
            case TYPE_GROUP_ITEM:
                return new GroupItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.swipe_item_group_favorite, parent, false));
        }
        throw new IllegalArgumentException(String.format(Locale.getDefault(),
                "unrecognized viewType -%d does not correspond to TYPE_NORMAL_ITEM,TYPE_GROUP_ITEM", viewType));
    }

    @Override
    public void onBindViewHolder(FavoriteAdapter.ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.bandData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setOperationStatus(int pos, int status) {
        if (pos == -1) {
            return;
        }
        for (int i = 0; i < mData.size(); i++) {
            if (i == pos) {
                FavoriteItem item = mData.get(i);
                MyVaultFile f = (MyVaultFile) item.file;
                f.setOperationStatus(status);
                notifyItemChanged(pos);
                break;
            }
        }
    }

    public void setOfflineStatus(int pos, boolean offline) {
        if (pos == -1) {
            return;
        }
        for (int i = 0; i < mData.size(); i++) {
            if (i == pos) {
                FavoriteItem item = mData.get(i);
                MyVaultFile f = (MyVaultFile) item.file;
                f.setOffline(offline);
                notifyItemChanged(pos);
                break;
            }
        }
    }

    public void removeItem(INxFile f) {
        if (mData == null || mData.isEmpty()) {
            return;
        }
        if (f == null) {
            return;
        }
        FavoriteItem removed = null;
        for (FavoriteItem item : mData) {
            if (item == null) {
                continue;
            }
            IFavoriteFile file = item.file;
            if (TextUtils.equals(file.getDisplayPath(), f.getDisplayPath())) {
                removed = item;
            }
        }
        if (removed != null) {
            mData.remove(removed);
        }
        notifyDataSetChanged();
    }

    public void removeItem(int pos) {
        mData.remove(pos);
        notifyItemRemoved(pos);
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

        abstract void bandData(FavoriteItem item);
    }

    private class ItemViewHolder extends ViewHolder {
        private SwipeMenuLayout mSwipeMenuLayout;
        private ImageView mIvFileIcon;
        private ImageView mIvOfflineIcon;
        private TextView mTvFileName;
        private TextView mTvFileSize;
        private TextView mTvFileDate;
        private TextView mTvFilePath;
        private ImageButton mIbMenuSite;
        private Button bt_01;
        private Button bt_02;
        //offline control use.
        private LinearLayout mLlOfflineStatusContainer;
        private ImageView mIvOfflineStatusIcon;
        private TextView mTvOfflineStatusText;

        ItemViewHolder(View itemView) {
            super(itemView);
            mSwipeMenuLayout = itemView.findViewById(R.id.swipe_menu_layout);
            mIvFileIcon = itemView.findViewById(R.id.iv_file_icon);
            mIvOfflineIcon = itemView.findViewById(R.id.iv_offline);

            mTvFileName = itemView.findViewById(R.id.tv_file_name);
            mTvFileSize = itemView.findViewById(R.id.file_size);
            mTvFileDate = itemView.findViewById(R.id.file_date);
            mTvFilePath = itemView.findViewById(R.id.file_path);
            mIbMenuSite = itemView.findViewById(R.id.item_menu_site);
            bt_01 = itemView.findViewById(R.id.bt_01);
            bt_02 = itemView.findViewById(R.id.bt_02);

            mLlOfflineStatusContainer = itemView.findViewById(R.id.ll_offline_status_container);
            mIvOfflineStatusIcon = itemView.findViewById(R.id.iv_offline_status);
            mTvOfflineStatusText = itemView.findViewById(R.id.tv_offline_status);

            initListener();
        }

        private void initListener() {
            mSwipeMenuLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getLayoutPosition();
                    if (pos == -1) {
                        return;
                    }
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mData.get(pos).file, pos);
                    }
                }
            });
            mIbMenuSite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getLayoutPosition();
                    if (pos == -1) {
                        return;
                    }
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onToggleItemMenu(mData.get(pos).file, pos);
                    }
                }
            });
            bt_01.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeMenuIfNecessary();
                    final int pos = getLayoutPosition();
                    if (pos == -1) {
                        return;
                    }
                    if (mOnItemClickListener != null) {
                        v.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mOnItemClickListener.onSwipeButton_01Click(mData.get(pos).file, pos, bt_01);
                            }
                        }, 200);
                    }
                }
            });
            bt_02.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeMenuIfNecessary();
                    final int pos = getLayoutPosition();
                    if (pos == -1) {
                        return;
                    }
                    if (mOnItemClickListener != null) {
                        v.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mOnItemClickListener.onSwipeButton_02Click(mData.get(pos).file, pos, bt_02);
                            }
                        }, 200);
                    }
                }
            });
        }

        private void closeMenuIfNecessary() {
            if (mSwipeMenuLayout == null) {
                return;
            }
            if (!mSwipeMenuLayout.isMenuOpen()) {
                return;
            }
            mSwipeMenuLayout.smoothCloseMenu();
        }

        @Override
        void bandData(FavoriteItem item) {
            IFavoriteFile f = item.file;

            String name = f.getName();
            mIvFileIcon.setImageResource(IconHelper.getNxlIconResourceIdByExtension(name.toLowerCase()));
            mTvFileName.setText(name);
            mTvFileSize.setText(FileUtils.transparentFileSize(f.getSize()));
            mTvFileDate.setText(TimeUtil.formatData(f.getLastModifiedTime()));

            mTvFilePath.setText(f.getDisplayPath());

            if (f instanceof NxFileBase) {
                bt_01.setText(R.string.share);
                if (name.endsWith(".nxl")) {
                    bt_02.setText(R.string.log);
                } else {
                    bt_02.setText(R.string.protect);
                }

                //reset all status.
                mSwipeMenuLayout.setEnabled(true);
                mIbMenuSite.setEnabled(true);
                resetOfflineStatusColor();
            } else if (f instanceof MyVaultFile) {
                MyVaultFile vf = (MyVaultFile) f;
                bt_01.setText(vf.isShared() ? R.string.manage : R.string.share);
                bt_02.setText(R.string.log);
                if (f.isOffline()) {
                    mIvOfflineIcon.setVisibility(View.VISIBLE);
                    mIvOfflineIcon.setImageResource(R.drawable.available_offline_icon3);
                    mLlOfflineStatusContainer.setVisibility(View.GONE);
                    //reset all status.
                    mSwipeMenuLayout.setEnabled(true);
                    mIbMenuSite.setEnabled(true);
                    resetOfflineStatusColor();
                } else {
                    int status = f.getOperationStatus();
                    if (status == INxlFile.PROCESS) {
                        mSwipeMenuLayout.setEnabled(false);
                        mIbMenuSite.setEnabled(false);
                        setupOfflineStatusColor();
                        mIvOfflineIcon.setVisibility(View.VISIBLE);
                        mIvOfflineIcon.setImageResource(R.drawable.icon_offline_rotate);
                        mLlOfflineStatusContainer.setVisibility(View.VISIBLE);
                        mIvOfflineStatusIcon.setImageResource(R.drawable.icon_offline_processing);
                        mTvOfflineStatusText.setText(R.string.updating);
                        mTvOfflineStatusText.setTextColor(Color.parseColor("#BDBDBD"));
                    } else {
                        resetOfflineStatusColor();
                        mSwipeMenuLayout.setEnabled(true);
                        mIbMenuSite.setEnabled(true);
                        mIvOfflineIcon.setVisibility(View.GONE);
                        mLlOfflineStatusContainer.setVisibility(View.GONE);
                    }
                }
            }
        }

        private void setupOfflineStatusColor() {
            mIvFileIcon.setColorFilter(Color.parseColor("#BDBDBD"));
            mTvFileName.setTextColor(Color.parseColor("#BDBDBD"));
            mTvFileSize.setTextColor(Color.parseColor("#BDBDBD"));
            mTvFileDate.setTextColor(Color.parseColor("#BDBDBD"));
            mTvFilePath.setTextColor(Color.parseColor("#BDBDBD"));
            mIbMenuSite.setColorFilter(Color.parseColor("#BDBDBD"));
        }

        private void resetOfflineStatusColor() {
            mIvFileIcon.setColorFilter(android.R.color.transparent);
            mTvFileName.setTextColor(Color.parseColor("#4C4E64"));
            mTvFileSize.setTextColor(Color.parseColor("#4C4E64"));
            mTvFileDate.setTextColor(Color.parseColor("#4C4E64"));
            mTvFilePath.setTextColor(Color.parseColor("#4C4E64"));
            mIbMenuSite.setColorFilter(Color.parseColor("#4C4E64"));
        }
    }

    private class GroupItemViewHolder extends ItemViewHolder {
        private TextView tvTitle;

        GroupItemViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_group_title);
        }

        @Override
        void bandData(FavoriteItem item) {
            super.bandData(item);
            String title = item.title;
            if (title == null || title.isEmpty()) {
                if (ViewUtils.isVisible(tvTitle)) {
                    tvTitle.setVisibility(View.GONE);
                }
            } else {
                if (ViewUtils.isGone(tvTitle)) {
                    tvTitle.setVisibility(View.VISIBLE);
                }
                tvTitle.setText(title);
            }
        }
    }
}
