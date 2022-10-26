package com.skydrm.rmc.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.commonUtils.IconHelper;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_CREATE_NEW_FOLDER;
import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_SELECT_PATH;

/**
 * Created by hhu on 5/10/2017.
 */

@Deprecated
public class LibraryFilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static DevLog log = new DevLog(LibraryFilesAdapter.class.getSimpleName());
    private static final int NORMAL_ITEM = 0x1000002;
    private static final int GROUP_ITEM = 0x1000003;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<NXFileItem> mFileLists;
    private Set<NXItemViewHolder> viewHolders = new HashSet<>();
    private boolean onSelectState = false;
    private int mActionIntent;

    public LibraryFilesAdapter(Context context, List<NXFileItem> mNXFileItems) {
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.mFileLists = mNXFileItems;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return GROUP_ITEM;
        } else {
            NXFileItem entity = mFileLists.get(position);
            String currentTitle = entity.getTitle();
            int prevIndex = position - 1;
            boolean isDifferent = !mFileLists.get(prevIndex).getTitle().equals(currentTitle);
            return isDifferent ? GROUP_ITEM : NORMAL_ITEM;
        }
    }

    private class NXItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView fileThumbnail;
        private ImageView fileFavoriteFlag;
        private ImageView fileOfflineFlag;
        private TextView fileName;
        private TextView driveType;
        private TextView fileDate;
        private TextView fileSize;
        private RelativeLayout rlContainer;
        private int mClickView;

        private NXItemViewHolder(View itemView) {
            super(itemView);
            fileThumbnail = (ImageView) itemView.findViewById(R.id.file_thumbnail);
            fileFavoriteFlag = (ImageView) itemView.findViewById(R.id.file_favorite_icon);
            fileOfflineFlag = (ImageView) itemView.findViewById(R.id.file_offline_icon);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            driveType = (TextView) itemView.findViewById(R.id.drive_type);
            fileDate = (TextView) itemView.findViewById(R.id.file_date);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);
            rlContainer = (RelativeLayout) itemView.findViewById(R.id.rl_container);
        }

        public void resetStatus() {
            rlContainer.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
            onSelectState = false;
            mClickView = -1;
        }
    }

    private class NXTitleViewHolder extends NXItemViewHolder {

        private final TextView fileTitle;

        private NXTitleViewHolder(View itemView) {
            super(itemView);
            fileTitle = (TextView) itemView.findViewById(R.id.file_title);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case NORMAL_ITEM:
                NXItemViewHolder nxItemViewHolder = new NXItemViewHolder(mLayoutInflater.inflate
                        (R.layout.item_normal_library_files, parent, false));
                viewHolders.add(nxItemViewHolder);
                return nxItemViewHolder;
            case GROUP_ITEM:
                NXTitleViewHolder nxTitleViewHolder = new NXTitleViewHolder(mLayoutInflater.
                        inflate(R.layout.item_group_library_files, parent, false));
                viewHolders.add(nxTitleViewHolder);
                return nxTitleViewHolder;
        }
        throw new IndexOutOfBoundsException("unrecognized viewType: " + viewType + " does not " +
                "correspond to NORMAL_ITEM, GROUP_ITEM");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case NORMAL_ITEM:
                if (mFileLists != null && mFileLists.size() != 0) {
                    NXFileItem normalFileItem = mFileLists.get(position);
                    bindNormalItem(mContext, normalFileItem, (NXItemViewHolder) holder, position);
                }
                break;
            case GROUP_ITEM:
                if (mFileLists != null && mFileLists.size() != 0) {
                    NXFileItem groupFileItem = mFileLists.get(position);
                    bindGroupItem(mContext, groupFileItem, (NXTitleViewHolder) holder, position);
                }
                break;
        }
    }

    private void bindNormalItem(Context context, NXFileItem entity, NXItemViewHolder holder, int position) {
        INxFile fileItem = entity.getNXFile();
        holder.fileFavoriteFlag.setVisibility(View.GONE);
        holder.fileOfflineFlag.setVisibility(View.GONE);
        if (fileItem.isSite()) {
            holder.itemView.setEnabled(true);
            holder.fileThumbnail.setColorFilter(context.getResources().getColor(R.color.main_drak_light));
            holder.fileThumbnail.setImageResource(R.drawable.home_site_icon);
            holder.rlContainer.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
            holder.fileSize.setVisibility(View.GONE);
            holder.fileDate.setVisibility(View.GONE);
        } else if (fileItem.isFolder()) {
            holder.itemView.setEnabled(true);
            holder.fileThumbnail.setColorFilter(context.getResources().getColor(R.color.main_drak_light));
            holder.fileThumbnail.setImageResource(R.drawable.icon_folder_black);
            holder.rlContainer.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
            holder.fileSize.setVisibility(View.GONE);
            holder.fileDate.setVisibility(View.GONE);
        } else {
            if (entity.isSelected()) {
                holder.rlContainer.setBackgroundColor(Color.parseColor("#FFFCD6"));
            } else {
                if (mActionIntent == ACTION_SELECT_PATH) {
                    holder.itemView.setEnabled(false);
                    holder.rlContainer.setBackgroundColor(Color.parseColor("#F2F3F5"));
                } else if (mActionIntent == ACTION_CREATE_NEW_FOLDER) {
                    holder.itemView.setEnabled(false);
                    holder.rlContainer.setBackgroundColor(Color.parseColor("#F2F3F5"));
                } else {
                    holder.itemView.setEnabled(true);
                    holder.rlContainer.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
                }
            }
            holder.fileThumbnail.setColorFilter(context.getResources().getColor(android.R.color.transparent));
            holder.fileSize.setVisibility(View.VISIBLE);
            holder.fileDate.setVisibility(View.VISIBLE);
            String nxlFileType = fileItem.getName().toLowerCase();
            holder.fileThumbnail.setImageResource(IconHelper.getNxlIconResourceIdByExtension(nxlFileType));
        }
        configItemMarks(holder, fileItem);
        configItemDisplayingString(holder, fileItem);
        bindItemClickListener(holder, entity, position);
    }

    private void bindItemClickListener(final NXItemViewHolder holder, final NXFileItem entity, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindItemSelectState(entity, holder);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(entity, holder.getLayoutPosition());
                }
            }
        });
    }

    private void bindItemSelectState(NXFileItem item, NXItemViewHolder holder) {
        if (!item.getNXFile().isFolder()) {
            if (!onSelectState) {
                log.e("select a  item.");
                holder.mClickView = item.getNXFile().getLocalPath().hashCode();
                onSelectState = true;
                item.setSelected(true);
                holder.rlContainer.setBackgroundColor(Color.parseColor("#FFFCD6"));
            } else if (holder.mClickView != item.getNXFile().getLocalPath().hashCode()) {
                log.e("select a new item.");
                //select not the same item.
                resetItemSelectedStatus(item);
                item.setSelected(true);
                holder.rlContainer.setBackgroundColor(Color.parseColor("#FFFCD6"));
                holder.mClickView = item.getNXFile().getLocalPath().hashCode();
                onSelectState = true;
            } else {
                log.e("select the same item.");
                //select the same item.
                holder.mClickView = -1;
                onSelectState = false;
                holder.rlContainer.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
                item.setSelected(false);
            }
        }
    }

    public void resetItemSelectedStatus(NXFileItem item) {
        for (NXItemViewHolder viewHolder : viewHolders) {
            item.setSelected(false);
            viewHolder.resetStatus();
//            notifyDataSetChanged();
        }
    }

    private void configItemMarks(NXItemViewHolder holder, INxFile fileItem) {
        if (fileItem.isMarkedAsFavorite() && fileItem.isMarkedAsOffline()) {
            holder.fileFavoriteFlag.setVisibility(View.VISIBLE);
            holder.fileOfflineFlag.setVisibility(View.VISIBLE);
        } else if (fileItem.isMarkedAsOffline()) {
            holder.fileOfflineFlag.setVisibility(View.VISIBLE);
        } else if (fileItem.isMarkedAsFavorite()) {
            holder.fileFavoriteFlag.setVisibility(View.VISIBLE);
        }
    }

    private void configItemDisplayingString(NXItemViewHolder holder, INxFile fileItem) {
        String tempFileName = fileItem.getName();
        if (fileItem.isSite()) {
            tempFileName = fileItem.getName().substring(1);
        }
        holder.fileName.setText(tempFileName);
        BoundService service = fileItem.getService();
        holder.driveType.setText(service.getDisplayName());
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
        holder.fileDate.setText(strTime);
        holder.fileSize.setText(strFileSize);
    }

    private void bindGroupItem(Context context, NXFileItem entity, NXTitleViewHolder holder, int position) {
        bindNormalItem(context, entity, holder, position);
        holder.fileTitle.setText(entity.getTitle());
    }

    @Override
    public int getItemCount() {
        return mFileLists.size();
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(NXFileItem entity, int position);
    }

    public void setActionIntent(int actionIntent) {
        this.mActionIntent = actionIntent;
    }
}
