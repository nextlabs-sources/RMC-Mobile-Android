package com.skydrm.rmc.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.engine.DownloadManager;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NxFileBase;
import com.skydrm.rmc.ui.widget.swipelayout.SwipeMenuLayout;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.commonUtils.IconHelper;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * adapter for file list(recycler view)
 */
public class NXFileRecyclerViewAdapter extends RecyclerView.Adapter<NXFileRecyclerViewAdapter.ViewHolder> {
    private static final boolean DEBUG = SkyDRMApp.getInstance().isDebug();
    private static final String TAG = "NXFileRecycler";
    //    private static final int TYPE_SEARCH_HEADER = 0x1000001;
    private static final int NORMAL_ITEM = 0x1000002;
    private static final int GROUP_ITEM = 0x1000003;
    private static final String SPERATOR = ", ";

    private Context mCtx;
    private List<NXFileItem> mData = new ArrayList<>();

    // For file downloading
    private TextView mInitializing;
    private INxFile mNxSrcFile;
    private File mLocalFile;
    private boolean mOfflineButtonStatus = false;
    private boolean isScrollState = false;
    private OnContentClickListener mOnContentClickListener;
    private OnRightMenuClickListener mOnRightMenuClickListener;
    private OnLeftMenuClickListener mOnLeftMenuClickListener;
    private SwipeMenuLayout mSwipeMenu;
    private OnFileDownloadedListener mOnFileDownloadedListener = new OnFileDownloadedListener() {
        @Override
        public void onFileDownloaded(int position) {

        }
    };

    public NXFileRecyclerViewAdapter(Context ctx) {
        mCtx = ctx;
    }

    public void removeItem(INxFile f) {
        if (mData == null || mData.size() == 0) {
            return;
        }
        int pos = -1;
        for (int i = 0; i < mData.size(); i++) {
            INxFile nxFile = mData.get(i).getNXFile();
            if (nxFile.getCloudPath().equals(f.getCloudPath())) {
                pos = i;
                break;
            }
        }
        if (pos != -1) {
            mData.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    public void setData(List<NXFileItem> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void setFavoriteStatus(int pos, boolean favorite) {
        if (mData == null || mData.size() == 0) {
            return;
        }
        INxFile f = mData.get(pos).getNXFile();
        NxFileBase base = (NxFileBase) f;
        base.setMarkedAsFavorite(favorite);
        notifyItemChanged(pos);
    }

    public void setFavoriteStatus(INxFile f, boolean favorite) {
        if (mData == null || mData.size() == 0) {
            return;
        }
        if (f == null) {
            return;
        }
        for (NXFileItem item : mData) {
            INxFile base = item.getNXFile();
            if (TextUtils.equals(base.getLocalPath(), f.getLocalPath())) {
                if (base instanceof NxFileBase) {
                    ((NxFileBase) base).setMarkedAsFavorite(favorite);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NORMAL_ITEM) {
            return new NormalItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_recyclerview_item2, parent, false));
        } else if (viewType == GROUP_ITEM) {
            return new GroupItemItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.nxfile_recyclerview_group_item, parent, false));
        }
        throw new IllegalArgumentException("Unrecognized view type " + viewType + " performed.");
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bandData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
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

    public void closeSwipeMenu() {
        if (mSwipeMenu != null) {
            if (mSwipeMenu.isMenuOpen()) {
                mSwipeMenu.smoothCloseMenu();
            }
        }
    }

    //    public interface On
    public void setOnContentClickListener(OnContentClickListener onContentClickListener) {
        this.mOnContentClickListener = onContentClickListener;
    }

    public void setOnRightMenuClickListener(OnRightMenuClickListener onRightMenuClickListener) {
        this.mOnRightMenuClickListener = onRightMenuClickListener;
    }

    public void setOnLeftMenuClickListener(OnLeftMenuClickListener onLeftMenuClickListener) {
        this.mOnLeftMenuClickListener = onLeftMenuClickListener;
    }

    public void setOnFileDownloadedListener(OnFileDownloadedListener onFileDownloadedListener) {
        mOnFileDownloadedListener = onFileDownloadedListener;
    }

    public interface OnContentClickListener {
        void onItemClick(INxFile f, View view, int position);

        void onDetailClick(INxFile f, View view, int position);
    }

    public interface OnRightMenuClickListener {
        void onShare(INxFile f, int adapterPosition);

        /**
         * This method is on Right Menu Protect button click
         *
         * @param view     the view you click in recyclerview
         * @param position the item position of recyclerview you clicked
         */
        void onProtect(INxFile f, View view, int position);

        void onViewActivityLog(INxFile f, int position);
    }

    public interface OnLeftMenuClickListener {
        void onDelete(INxFile nxFile, int adapterPosition);
    }

    public interface OnFileDownloadedListener {
        void onFileDownloaded(int position);
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bandData(NXFileItem item);
    }

    private class NormalItemViewHolder extends ViewHolder implements View.OnClickListener {
        //Container of all the Menus
        private SwipeMenuLayout mMenuContainer;
        //Item Content
        private TextView mFileName;
        private TextView subDetail;
        private ImageButton fileInfo;
        private ImageView fileThumbnail;
        private ImageView favoriteFlag;
        private ImageView offlineLocalFlag;
        //Item Right Menu
        private TextView mFileNameOfRightMenu;
        private TextView mTvSubTextRightMenu;
        private ImageView mFavoriteButton;
        private ImageView mOfflineButton;
        private Button mShareButton;
        private Button mProtectButton;
        private Button mLogButton;
        //Item Left Menu
        private TextView mFileNameOfLeftMenu;
        private TextView mTvSubTextLeftMenu;
        private boolean mFavoriteButtonSelected = false;
        private boolean mOfflineButtonSelected = false;
        private ProgressBar mProgressBar;
        private ImageButton mDeleteButton;

        private NormalItemViewHolder(View itemView) {
            super(itemView);
            initView(itemView);
            initEvent();
        }

        private void initView(View itemView) {
            //Item Content
            fileThumbnail = itemView.findViewById(R.id.nxfile_thumbnail);
            mFileName = itemView.findViewById(R.id.nxfile_name);
            subDetail = itemView.findViewById(R.id.nxfile_sub_detail);
            fileInfo = itemView.findViewById(R.id.nxfile_detail);
            favoriteFlag = itemView.findViewById(R.id.nxfile_favorite_icon);
            offlineLocalFlag = itemView.findViewById(R.id.nxfile_offline_local_icon);
            //Item Right Menu
            mFileNameOfRightMenu = itemView.findViewById(R.id.file_name_right_menu);
            mTvSubTextRightMenu = itemView.findViewById(R.id.tv_sub_text_right_menu);
            mFavoriteButton = itemView.findViewById(R.id.swipe_right_menu_btn_favorite);
            mOfflineButton = itemView.findViewById(R.id.swipe_right_menu_btn_offline);
            mShareButton = itemView.findViewById(R.id.swipe_right_menu_btn_share);
            mProtectButton = itemView.findViewById(R.id.swipe_right_menu_btn_protect);
            mLogButton = itemView.findViewById(R.id.swipe_right_menu_btn_log);
            mProgressBar = itemView.findViewById(R.id.download_progress_right_menu);
            mInitializing = itemView.findViewById(R.id.download_initial_right_menu);
            //Item Left Menu
            mFileNameOfLeftMenu = itemView.findViewById(R.id.file_name_left_menu);
            mTvSubTextLeftMenu = itemView.findViewById(R.id.tv_sub_text_left_menu);
            mMenuContainer = itemView.findViewById(R.id.swipe_menu_layout);
            mDeleteButton = itemView.findViewById(R.id.swipe_left_menu_btn_delete);
        }

        private void initEvent() {
            //Item Content
            itemView.findViewById(R.id.nxfile_detail).setOnClickListener(this);
            itemView.findViewById(R.id.swipe_content_view).setOnClickListener(this);
            //Item Right Menu
            mShareButton.setOnClickListener(this);
            mProtectButton.setOnClickListener(this);
            mLogButton.setOnClickListener(this);
            mFavoriteButton.setOnClickListener(this);
            mOfflineButton.setOnClickListener(this);
            //Item left Menu
            mDeleteButton.setOnClickListener(this);
            mMenuContainer.setOnMenuStatusChangeListener(new SwipeMenuLayout.OnMenuStatusChangeListener() {
                @Override
                public void onChange(SwipeMenuLayout swipeMenuLayout) {
                    mSwipeMenu = swipeMenuLayout;
                }
            });
        }

        private void updateUI_FavoriteButton() {
            if (mFavoriteButtonSelected) {
//                mFavoriteButton.setBackgroundResource(R.drawable.bg_favorite_button_selected_2);
                mFavoriteButton.setImageResource(R.drawable.favorited_icon3);
            } else {
//                mFavoriteButton.setBackgroundResource(R.drawable.bg_favorite_button_2);
                mFavoriteButton.setImageResource(R.drawable.make_as_favorite_icon3);
            }
            mFavoriteButtonSelected = !mFavoriteButtonSelected;
        }

        private void updateUI_OfflineButton() {
            if (mOfflineButtonSelected) {
//                mOfflineButton.setBackgroundResource(R.drawable.bg_offline_button_selected_2);
                mOfflineButton.setImageResource(R.drawable.available_offline_icon3);
            } else {
//                mOfflineButton.setBackgroundResource(R.drawable.bg_offline_button_2);
                mOfflineButton.setImageResource(R.drawable.download_offline_icon3);
            }
            mOfflineButtonSelected = !mOfflineButtonSelected;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.swipe_content_view:
                    int pos = getLayoutPosition();
                    if (pos == -1) {
                        return;
                    }
                    INxFile f = mData.get(pos).getNXFile();
                    if (mOnContentClickListener != null) {
                        mOnContentClickListener.onItemClick(f, itemView, pos);
                    }
                    break;
                case R.id.nxfile_detail:
                    int pos1 = getLayoutPosition();
                    if (pos1 == -1) {
                        return;
                    }
                    INxFile f1 = mData.get(pos1).getNXFile();
                    if (mOnContentClickListener != null) {
                        mOnContentClickListener.onDetailClick(f1, itemView, pos1);
                    }
                    break;
                case R.id.swipe_right_menu_btn_favorite:
                    try {
                        if (mMenuContainer.isMenuOpen()) {
                            mMenuContainer.smoothCloseMenu();
                        }
                        final INxFile nxFile = mData.get(getAdapterPosition()).getNXFile();
                        if (mFavoriteButtonSelected) {
                            SkyDRMApp.getInstance().getRepoSystem().markAsFavorite(nxFile);
                        } else {
                            SkyDRMApp.getInstance().getRepoSystem().unmarkAsFavorite(nxFile);
                        }
                        updateUI_FavoriteButton();
                    } catch (Exception e) {
                        if (DEBUG) {
                            Log.e(TAG, "onClick: " + e);
                        }
                    }
                    break;
                case R.id.swipe_right_menu_btn_offline:
                    //getAdapterPosition() will lead to ArrayIndexOutOfBoundsException
                    // java.lang.ArrayIndexOutOfBoundsException: length=109; index=-1
                    try {
                        if (mMenuContainer.isMenuOpen()) {
                            mMenuContainer.smoothCloseMenu();
                        }
                        mNxSrcFile = mData.get(getAdapterPosition()).getNXFile();
                        if (mOfflineButtonSelected) {
                            SkyDRMApp.getInstance().getRepoSystem().markAsOffline(mNxSrcFile);
                        } else {
                            SkyDRMApp.getInstance().getRepoSystem().unmarkAsOffline(mNxSrcFile);
                        }
                        if (mOfflineButtonSelected) {
                            File file = DownloadManager.getInstance().tryGetFile(mCtx, mNxSrcFile, mProgressBar, null, false, new DownloadManager.IDownloadCallBack() {
                                @Override
                                public void onDownloadFinished(boolean taskStatus, String localPath, @Nullable FileDownloadException e) {
                                    if (DEBUG) {
                                        Log.e(TAG, "onDownloadFinished: " + getAdapterPosition());
                                    }
                                    mOnFileDownloadedListener.onFileDownloaded(getLayoutPosition());
                                    if (taskStatus) {
                                        mLocalFile = new File(localPath);
                                    }
                                    mProgressBar.setVisibility(View.GONE);
                                    DownloadManager.getInstance().removeDownloader(mNxSrcFile);
//                                    offlineRefreshingFlag.setVisibility(View.GONE);
//                                    offlineLocalFlag.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onDownloadProgress(long value) {

                                }
                            });
                            if (file != null) {
                                mLocalFile = file;
                            } else {
                                // show download progress bar
                                mProgressBar.setVisibility(View.VISIBLE);
                            }
                        } else {
                            DownloadManager.Downloader downloader = DownloadManager.getInstance().tryGetDownloader(mNxSrcFile);
                            if (downloader != null) {
                                ICancelable mDownloadFileCancelHandler = downloader.getDownLoadCancelHandler();
                                if (mDownloadFileCancelHandler != null) {
                                    if (DEBUG) {
                                        Log.e(TAG, "onClick:mDownloadFileCancel");
                                    }
                                    mDownloadFileCancelHandler.cancel();
                                    DownloadManager.getInstance().removeDownloader(mNxSrcFile);
                                    mProgressBar.setVisibility(View.GONE);
//                                    offlineRefreshingFlag.setVisibility(View.GONE);
//                                    offlineLocalFlag.setVisibility(View.GONE);
                                }
                            }
                        }
                        updateUI_OfflineButton();
                    } catch (Exception e) {
                        if (DEBUG) {
                            Log.e(TAG, "onClick: " + e);
                        }
                    }
                    break;
                case R.id.swipe_right_menu_btn_protect:
                    closeMenuIfNecessary();
                    if (mOnRightMenuClickListener == null) {
                        return;
                    }
                    final int pos2 = getLayoutPosition();
                    if (pos2 == -1) {
                        return;
                    }
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mOnRightMenuClickListener.onProtect(mData.get(pos2).getNXFile(), itemView, pos2);
                        }
                    }, 200);
                    break;
                case R.id.swipe_right_menu_btn_share:
                    closeMenuIfNecessary();
                    if (mOnRightMenuClickListener == null) {
                        return;
                    }
                    final int pos3 = getLayoutPosition();
                    if (pos3 == -1) {
                        return;
                    }
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mOnRightMenuClickListener.onShare(mData.get(pos3).getNXFile(), pos3);
                        }
                    }, 200);
                    break;
                case R.id.swipe_right_menu_btn_log:
                    if (mOnRightMenuClickListener != null) {
                        if (mData.size() != 0) {
                            mOnRightMenuClickListener.onViewActivityLog(mData.get(getAdapterPosition()).getNXFile(), getAdapterPosition());
                        }
                    }
                    break;
                case R.id.swipe_left_menu_btn_delete:
                    if (mMenuContainer.isMenuOpen()) {
                        mMenuContainer.smoothCloseMenu();
                    }
                    if (mOnLeftMenuClickListener != null) {
                        if (mData.size() != 0) {
                            mOnLeftMenuClickListener.onDelete(mData.get(getAdapterPosition()).getNXFile(), getAdapterPosition());
                        }
                    }
                    break;
            }
        }

        private void closeMenuIfNecessary() {
            if (mMenuContainer == null) {
                return;
            }
            if (!mMenuContainer.isMenuOpen()) {
                return;
            }
            mMenuContainer.smoothCloseMenu();
        }

        @Override
        void bandData(NXFileItem item) {
            INxFile fileItem = item.getNXFile();

//        holder.offlineLocalFlag.setVisibility(View.GONE);
            mMenuContainer.setSwipeRightEnable(false);
            mShareButton.setVisibility(View.VISIBLE);
            //hide the protect and delete site in sliding menu(Maybe reopen in future just keep the code)
//        holder.mDeleteButton.setVisibility(View.VISIBLE);
            if (!fileItem.getName().endsWith(".nxl")) {
                mProtectButton.setVisibility(View.VISIBLE);
                mLogButton.setVisibility(View.GONE);
            } else {
                mProtectButton.setVisibility(View.GONE);
                mLogButton.setVisibility(fileItem.getService().type.equals(BoundService.ServiceType.MYDRIVE) ?
                        View.VISIBLE : View.GONE);
            }
            // folder and sharepoint site, disable right-swipe feature
            if (mMenuContainer.isMenuOpen()) {
                mMenuContainer.smoothCloseMenu();
            }
            if (fileItem.isSite()) {
                fileThumbnail.setColorFilter(mCtx.getResources().getColor(R.color.main_drak_light));
                fileThumbnail.setImageResource(R.drawable.home_site_icon);
                mMenuContainer.setSwipeRightEnable(false);
                mMenuContainer.setSwipeLeftEnable(false);
                favoriteFlag.setVisibility(View.GONE);
                if (!fileItem.getService().type.equals(BoundService.ServiceType.MYDRIVE)) {
                    fileInfo.setVisibility(View.GONE);
                } else {
                    fileInfo.setVisibility(View.VISIBLE);
                }
            } else if (fileItem.isFolder()) {
                favoriteFlag.setVisibility(View.GONE);
                mMenuContainer.setSwipeRightEnable(false);
                mMenuContainer.setSwipeLeftEnable(false);
                mShareButton.setVisibility(View.GONE);
                fileThumbnail.setColorFilter(mCtx.getResources().getColor(R.color.main_drak_light));
                fileThumbnail.setImageResource(R.drawable.icon_folder_black);
                if (!fileItem.getService().type.equals(BoundService.ServiceType.MYDRIVE)) {
                    fileInfo.setVisibility(View.GONE);
                } else {
                    fileInfo.setVisibility(View.VISIBLE);
                }
            } else {
                fileInfo.setVisibility(View.VISIBLE);
                mMenuContainer.setSwipeRightEnable(true);
                mMenuContainer.setSwipeLeftEnable(false);
                fileThumbnail.setColorFilter(mCtx.getResources().getColor(android.R.color.transparent));
                String nxlFileType = fileItem.getName().toLowerCase();
                fileThumbnail.setImageResource(IconHelper.getNxlIconResourceIdByExtension(nxlFileType));
                configItemMarks(fileItem);
            }
            configItemDisplayingString(fileItem);
        }

        private void configItemMarks(INxFile fileItem) {
            if (fileItem.isMarkedAsFavorite()) {
                favoriteFlag.setVisibility(View.VISIBLE);
            } else {
                favoriteFlag.setVisibility(View.GONE);
            }
            if (fileItem.isMarkedAsOffline()) {
                offlineLocalFlag.setVisibility(View.GONE);
            } else {
                offlineLocalFlag.setVisibility(View.GONE);
            }
            //
            mFavoriteButtonSelected = fileItem.isMarkedAsFavorite();
            mOfflineButtonSelected = fileItem.isMarkedAsOffline();
            mOfflineButtonStatus = fileItem.isMarkedAsOffline();
            //
            updateUI_FavoriteButton();
            updateUI_OfflineButton();
        }

        private void configItemDisplayingString(INxFile fileItem) {
        /*
            item name:
                for sharepoint site,
         */
            String tempFileName = fileItem.getName();
            if (fileItem.isSite()) {
                tempFileName = fileItem.getName().substring(1);
            }

            mFileName.setText(tempFileName);
            mFileNameOfLeftMenu.setText(tempFileName);
            mFileNameOfRightMenu.setText(tempFileName);

        /*
            basic format:
             doc:           [service-name],[size],[lastmodifytime]
             folder/side:   [service-name]
         */
            BoundService service = fileItem.getService();
            boolean isFolder = fileItem.isFolder();
            String strTime = "";
            String strService = "unknown service";
            String strFileSize = "";
            try {
                long fileSize = fileItem.getSize();
                String time = TimeUtil.formatData(fileItem.getLastModifiedTimeLong());
                if (!isFolder) {
                    // folder will not display fsize,and time
                    strTime = time.isEmpty() ? "" : time;
                    if (fileSize != -1) {
                        // N/A means Not Available
                        strFileSize = RenderHelper.isGoogleFile(fileItem) ? "N/A" : FileUtils.transparentFileSize(fileSize);
                    }
                }
                if (service != null) {
                    strService = service.alias;
                }
            } catch (Exception e) {
                if (DEBUG) e.printStackTrace();

            }

            // config each item's face (left,normal,right)
            if (service == null) {
                // bug-triggered
                if (DEBUG) {
                    Log.e(TAG, "Bug-triggered, service is null when render itme:" + fileItem.getDisplayPath());
                }
                subDetail.setText(strService);
                mTvSubTextLeftMenu.setText(strService);
                mTvSubTextRightMenu.setText(strService);
            } else {
                if (isFolder) { // folder only displays service-name
                    subDetail.setText(strService);
                    mTvSubTextLeftMenu.setText(strService);
                    mTvSubTextRightMenu.setText(strService);
                } else {// doc displays [service-name],[size],[lastmodifytime]
                    String formatString = strService + SPERATOR + strFileSize + SPERATOR + strTime;
                    subDetail.setText(formatString);
                    mTvSubTextLeftMenu.setText(formatString);
                    mTvSubTextRightMenu.setText(formatString);
                }
            }
        }
    }

    private class GroupItemItemViewHolder extends NormalItemViewHolder {
        private TextView mTvTitle;

        private GroupItemItemViewHolder(View itemView) {
            super(itemView);
            mTvTitle = itemView.findViewById(R.id.nxfile_title);
        }

        @Override
        void bandData(NXFileItem item) {
            super.bandData(item);
            String title = item.getTitle();
            if (!TextUtils.isEmpty(title)) {
                mTvTitle.setVisibility(View.VISIBLE);
                mTvTitle.setText(title);
            } else {
                mTvTitle.setVisibility(View.GONE);
            }
        }
    }
}
