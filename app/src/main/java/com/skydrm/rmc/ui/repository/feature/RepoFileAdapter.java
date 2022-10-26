package com.skydrm.rmc.ui.repository.feature;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.commonUtils.IconHelper;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_CREATE_NEW_FOLDER;
import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_SELECT_PATH;

/**
 * Created by hhu on 5/10/2017.
 */

public class RepoFileAdapter extends RecyclerView.Adapter<RepoFileAdapter.ViewHolder> {
    private static final int NORMAL_ITEM = 0x1000002;
    private static final int GROUP_ITEM = 0x1000003;
    private Context mCtx;
    private List<NXFileItem> mData = new ArrayList<>();
    private Set<NXItemViewHolder> mViewHolders = new HashSet<>();
    private boolean onSelectState = false;
    private int mActionIntent;

    private INxFile mCurrentFolder;
    private String mParentPathId = "/";
    private OnItemClickListener mOnItemClickListener;

    public RepoFileAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public String getParentPathId() {
        return mParentPathId;
    }

    public String getFolderName() {
        if (mParentPathId.equals("/")) {
            return "";
        }
        if (mCurrentFolder == null) {
            return "";
        }
        return mCurrentFolder.getName();
    }

    public void setParentPathId(String pathId) {
        this.mParentPathId = pathId;
    }

    public void setData(List<NXFileItem> data) {
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
            return GROUP_ITEM;
        } else {
            NXFileItem entity = mData.get(position);
            String currentTitle = entity.getTitle();
            int prevIndex = position - 1;
            boolean isDifferent = !mData.get(prevIndex).getTitle().equals(currentTitle);
            return isDifferent ? GROUP_ITEM : NORMAL_ITEM;
        }
    }


    @Override
    public RepoFileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case NORMAL_ITEM:
                NXItemViewHolder nxItemViewHolder = new NXItemViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_normal_library_files, parent, false));
                mViewHolders.add(nxItemViewHolder);
                return nxItemViewHolder;
            case GROUP_ITEM:
                NXTitleViewHolder nxTitleViewHolder = new NXTitleViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_group_library_files, parent, false));
                mViewHolders.add(nxTitleViewHolder);
                return nxTitleViewHolder;
        }
        throw new IndexOutOfBoundsException("unrecognized viewType: " + viewType + " does not " +
                "correspond to NORMAL_ITEM, GROUP_ITEM");
    }

    @Override
    public void onBindViewHolder(RepoFileAdapter.ViewHolder holder, int position) {
        holder.bindData(mData.get(position));
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bindData(NXFileItem item);
    }

    private class NXItemViewHolder extends ViewHolder {
        private ImageView mIvThumbnail;
        private ImageView mIvFavoriteFlag;
        private ImageView mIvOfflineFlag;
        private TextView mTvFileName;
        private TextView mTvDriveType;
        private TextView mTvFileDate;
        private TextView mTvFileSize;
        private RelativeLayout mRlContainer;
        private int mClickView;

        private NXItemViewHolder(View itemView) {
            super(itemView);
            mIvThumbnail = itemView.findViewById(R.id.file_thumbnail);
            mIvFavoriteFlag = itemView.findViewById(R.id.file_favorite_icon);
            mIvOfflineFlag = itemView.findViewById(R.id.file_offline_icon);
            mTvFileName = itemView.findViewById(R.id.file_name);
            mTvDriveType = itemView.findViewById(R.id.drive_type);
            mTvFileDate = itemView.findViewById(R.id.file_date);
            mTvFileSize = itemView.findViewById(R.id.file_size);
            mRlContainer = itemView.findViewById(R.id.rl_container);

            initEvent(itemView);
        }

        private void initEvent(View itemView) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getLayoutPosition();
                    if (pos == -1) {
                        return;
                    }
                    NXFileItem item = mData.get(pos);
                    INxFile f = item.getNXFile();
                    if (f.isFolder()) {
                        mParentPathId = f.getParent();
                        mCurrentFolder = f;
                    }
                    bindItemSelectState(item);
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(item, pos);
                    }
                }
            });
        }

        private void bindItemSelectState(NXFileItem item) {
            if (!item.getNXFile().isFolder()) {
                if (!onSelectState) {
                    mClickView = item.getNXFile().getLocalPath().hashCode();
                    onSelectState = true;
                    item.setSelected(true);
                    mRlContainer.setBackgroundColor(Color.parseColor("#FFFCD6"));
                } else if (mClickView != item.getNXFile().getLocalPath().hashCode()) {
                    //select not the same item.
                    resetItemSelectedStatus(item);
                    item.setSelected(true);
                    mRlContainer.setBackgroundColor(Color.parseColor("#FFFCD6"));
                    mClickView = item.getNXFile().getLocalPath().hashCode();
                    onSelectState = true;
                } else {
                    //select the same item.
                    mClickView = -1;
                    onSelectState = false;
                    mRlContainer.setBackgroundColor(mCtx.getResources().getColor(android.R.color.white));
                    item.setSelected(false);
                }
            }
        }

        private void resetItemSelectedStatus(NXFileItem item) {
            for (NXItemViewHolder viewHolder : mViewHolders) {
                item.setSelected(false);
                viewHolder.resetStatus();
            }
        }

        private void resetStatus() {
            mRlContainer.setBackgroundColor(mCtx.getResources().getColor(android.R.color.white));
            onSelectState = false;
            mClickView = -1;
        }

        @Override
        void bindData(NXFileItem item) {
            INxFile fileItem = item.getNXFile();
            mIvFavoriteFlag.setVisibility(View.GONE);
            mIvOfflineFlag.setVisibility(View.GONE);
            if (fileItem.isSite()) {
                itemView.setEnabled(true);
                mIvThumbnail.setColorFilter(mCtx.getResources().getColor(R.color.main_drak_light));
                mIvThumbnail.setImageResource(R.drawable.home_site_icon);
                mRlContainer.setBackgroundColor(mCtx.getResources().getColor(android.R.color.white));
                mTvFileSize.setVisibility(View.GONE);
                mTvFileDate.setVisibility(View.GONE);
            } else if (fileItem.isFolder()) {
                itemView.setEnabled(true);
                mIvThumbnail.setColorFilter(mCtx.getResources().getColor(R.color.main_drak_light));
                mIvThumbnail.setImageResource(R.drawable.icon_folder_black);
                mRlContainer.setBackgroundColor(mCtx.getResources().getColor(android.R.color.white));
                mTvFileSize.setVisibility(View.GONE);
                mTvFileDate.setVisibility(View.GONE);
            } else {
                if (item.isSelected()) {
                    mRlContainer.setBackgroundColor(Color.parseColor("#FFFCD6"));
                } else {
                    if (mActionIntent == ACTION_SELECT_PATH) {
                        itemView.setEnabled(false);
                        mRlContainer.setBackgroundColor(Color.parseColor("#F2F3F5"));
                    } else if (mActionIntent == ACTION_CREATE_NEW_FOLDER) {
                        itemView.setEnabled(false);
                        mRlContainer.setBackgroundColor(Color.parseColor("#F2F3F5"));
                    } else {
                        itemView.setEnabled(true);
                        mRlContainer.setBackgroundColor(mCtx.getResources().getColor(android.R.color.white));
                    }
                }
                mIvThumbnail.setColorFilter(mCtx.getResources().getColor(android.R.color.transparent));
                mTvFileSize.setVisibility(View.VISIBLE);
                mTvFileDate.setVisibility(View.VISIBLE);
                String nxlFileType = fileItem.getName().toLowerCase();
                mIvThumbnail.setImageResource(IconHelper.getNxlIconResourceIdByExtension(nxlFileType));
            }
            configItemMarks(fileItem);
            configItemDisplayingString(fileItem);
        }

        private void configItemMarks(INxFile fileItem) {
            if (fileItem.isMarkedAsFavorite() && fileItem.isMarkedAsOffline()) {
                mIvFavoriteFlag.setVisibility(View.VISIBLE);
                mIvOfflineFlag.setVisibility(View.VISIBLE);
            } else if (fileItem.isMarkedAsOffline()) {
                mIvOfflineFlag.setVisibility(View.VISIBLE);
            } else if (fileItem.isMarkedAsFavorite()) {
                mIvFavoriteFlag.setVisibility(View.VISIBLE);
            }
        }

        private void configItemDisplayingString(INxFile fileItem) {
            String tempFileName = fileItem.getName();
            if (fileItem.isSite()) {
                tempFileName = fileItem.getName().substring(1);
            }
            mTvFileName.setText(tempFileName);
            BoundService service = fileItem.getService();
            mTvDriveType.setText(service.getDisplayName());
            String strTime = "";
            String strFileSize = "";
            long fileSize = fileItem.getSize();
            String time = TimeUtil.formatData(fileItem.getLastModifiedTimeLong());
            if (!fileItem.isFolder()) {
                // folder will not display fsize,and time
                strTime = time.isEmpty() ? "" : time;
                if (fileSize != -1) {
                    // N/A means Not Available
                    strFileSize = RenderHelper.isGoogleFile(fileItem) ? "N/A" : FileUtils.transparentFileSize(fileSize);
                }
            }
            mTvFileDate.setText(strTime);
            mTvFileSize.setText(strFileSize);
        }
    }

    private class NXTitleViewHolder extends NXItemViewHolder {
        private final TextView mTvTitle;

        private NXTitleViewHolder(View itemView) {
            super(itemView);
            mTvTitle = itemView.findViewById(R.id.file_title);
        }

        @Override
        void bindData(NXFileItem item) {
            super.bindData(item);
            mTvTitle.setText(item.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public interface OnItemClickListener {
        void onItemClick(NXFileItem entity, int position);
    }

    public void setActionIntent(int actionIntent) {
        this.mActionIntent = actionIntent;
    }
}
