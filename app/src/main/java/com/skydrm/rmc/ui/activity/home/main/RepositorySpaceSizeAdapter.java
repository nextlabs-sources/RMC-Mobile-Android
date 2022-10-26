package com.skydrm.rmc.ui.activity.home.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.utils.FileUtils;

import java.util.List;

@Deprecated
public class RepositorySpaceSizeAdapter extends RecyclerView.Adapter<RepositorySpaceSizeAdapter.RepositorySpaceSizeViewHolder> {
    private static final int TOTAL_REPOSITORY_ITEM_VIEW = 0;
    private static final int SUB_REPOSITORY_ITEM_VIEW = 1;
    private static final int TYPE_WORKSPACE_REPO_VIEW = 2;
    private final String[] repositoryNameList;
    private List<Long> repositorySizeList;
    private Context mContent;
    private long mySpaceStorageSize = 0;
    private HomeRecycleViewItemClickListener ItemClickListener;

    private long mUsage;
    private long mQuota;
    private int mTotalFiles;

    public RepositorySpaceSizeAdapter(Context context) {
        this.mContent = context;
        repositoryNameList = mContent.getResources().getStringArray(R.array.repositoryName);
    }

    public void setData(List<Long> list, long mySpaceStorageSize) {
        this.repositorySizeList = list;
        this.mySpaceStorageSize = mySpaceStorageSize;
    }

    public void showWorkSpaceInfo(long usage, long quota, int totalFiles) {
        this.mUsage = usage;
        this.mQuota = quota;
        this.mTotalFiles = totalFiles;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RepositorySpaceSizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RepositorySpaceSizeViewHolder repositorySpaceSizeViewHolder = null;
        View view = null;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(mContent).inflate(R.layout.item_myspace_storage, parent, false);
                repositorySpaceSizeViewHolder = new TotalRepositorySizeViewHolder(view);
                break;
            case 1:
                view = LayoutInflater.from(mContent).inflate(R.layout.item_repositoryspacesize, parent, false);
                repositorySpaceSizeViewHolder = new SubRepositorySizeViewHolder(view);
                break;
            case TYPE_WORKSPACE_REPO_VIEW:
                view = LayoutInflater.from(mContent).inflate(R.layout.item_myspace_storage, parent, false);
                repositorySpaceSizeViewHolder = new WorkSpaceReoViewHolder(view);
                break;
        }
        return repositorySpaceSizeViewHolder;
    }

    @Override
    public void onBindViewHolder(RepositorySpaceSizeViewHolder holder, int position) {
        holder.bindViewData(position);
    }

    @Override
    public int getItemCount() {
        return repositoryNameList.length;
    }

    @Override
    public int getItemViewType(int position) {
        int type = -1;
        switch (repositoryNameList[position]) {
            case "MyDrive":
                type = SUB_REPOSITORY_ITEM_VIEW;
                break;
            case "MyVault":
                type = SUB_REPOSITORY_ITEM_VIEW;
                break;
            case "MySpace":
                type = TOTAL_REPOSITORY_ITEM_VIEW;
                break;
            case "WorkSpace":
                type = TYPE_WORKSPACE_REPO_VIEW;
                break;
        }
        return type;
    }

    public void setItemClickListener(HomeRecycleViewItemClickListener itemClickListener) {
        ItemClickListener = itemClickListener;
    }

    class RepositorySpaceSizeViewHolder extends RecyclerView.ViewHolder {
        View mItemView;

        RepositorySpaceSizeViewHolder(View itemView) {
            super(itemView);
            this.mItemView = itemView;
            initListener();
        }

        private void initListener() {
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null == ItemClickListener) {
                        return;
                    }
                    int layoutPosition = getLayoutPosition();
                    if (layoutPosition == -1) {
                        return;
                    }
                    String s = repositoryNameList[layoutPosition];

                    switch (s) {
                        case "MyDrive":
                            ItemClickListener.onSelectMyDrive();
                            break;
                        case "MyVault":
                            ItemClickListener.onSelectMyVault();
                            break;
                        case "MySpace":
                            ItemClickListener.onSelectMySpace();
                            break;
                        case "WorkSpace":
                            ItemClickListener.onSelectWorkSpace();
                            break;
                    }
                }
            });
        }

        public void bindViewData(int position) {

        }
    }

    class SubRepositorySizeViewHolder extends RepositorySpaceSizeViewHolder {
        View mItemView;
        ImageView colorBar;
        TextView repositoryName;
        TextView repositorySize;
        TypedArray colors;

        SubRepositorySizeViewHolder(View itemView) {
            super(itemView);
            this.mItemView = itemView;
            findView(mItemView);
        }

        private void findView(View mItemView) {
            colorBar = mItemView.findViewById(R.id.repository_color_bar);
            repositoryName = mItemView.findViewById(R.id.repository_name);
            repositorySize = mItemView.findViewById(R.id.repository_size);
        }

        public void bindViewData(int position) {
            colors = mContent.getResources().obtainTypedArray(R.array.repository_bar_colors);
            int color = colors.getColor(position, 0);
            colors.recycle();
            colorBar.setBackground(new ColorDrawable(color));
            repositoryName.setText(repositoryNameList[position]);
//            repositorySize.setText( FileUtils.transparentFileSize(repositorySizeList.get(position)));
            if (null != repositorySizeList && !repositorySizeList.isEmpty()) {
                repositorySize.setText(Formatter.formatFileSize(mContent, repositorySizeList.get(position)));
            }
        }
    }

    class TotalRepositorySizeViewHolder extends RepositorySpaceSizeViewHolder {
        View mItemView;
        TextView repositoryName;
        ProgressBar repositoryProgressBar;
        TextView totalFree;

        TotalRepositorySizeViewHolder(View itemView) {
            super(itemView);
            this.mItemView = itemView;
            findView(mItemView);
        }

        private void findView(View mItemView) {
            repositoryName = mItemView.findViewById(R.id.repository_name);
            repositoryProgressBar = mItemView.findViewById(R.id.repository_progressBar);
            totalFree = mItemView.findViewById(R.id.total_free);
        }

        public void bindViewData(int position) {
            repositoryName.setText(repositoryNameList[position]);
            repositoryProgressBar.setMax(100);
            if (null != repositorySizeList && !repositorySizeList.isEmpty()) {
                long totalUsageSize = repositorySizeList.get(position);
                int progress = (int) Math.ceil(repositorySizeList.get(0) * 100.0 / mySpaceStorageSize);
                repositoryProgressBar.setProgress(progress);
                int secondaryProgress = (int) Math.ceil(repositorySizeList.get(1) * 100.0 / mySpaceStorageSize);
                repositoryProgressBar.setSecondaryProgress(secondaryProgress + progress);
                totalFree.setText(FileUtils.transparentFileSize(mySpaceStorageSize - totalUsageSize < 0 ? 0 : mySpaceStorageSize - totalUsageSize));
            }
        }
    }

    class WorkSpaceReoViewHolder extends RepositorySpaceSizeViewHolder {
        TextView mTvRepoName;
        ProgressBar mPbRepoRatio;
        TextView mTvTotalFree;

        WorkSpaceReoViewHolder(View itemView) {
            super(itemView);
            mTvRepoName = mItemView.findViewById(R.id.repository_name);
            mPbRepoRatio = mItemView.findViewById(R.id.repository_progressBar);
            mTvTotalFree = mItemView.findViewById(R.id.total_free);
        }

        @Override
        public void bindViewData(int position) {
            mTvRepoName.setText(repositoryNameList[position]);
            mPbRepoRatio.setMax(100);

            int progress = (int) Math.ceil(mUsage * 100.0 / mQuota);
            mPbRepoRatio.setProgress(progress);

            mTvTotalFree.setText(FileUtils.transparentFileSize(mQuota - mUsage < 0 ? 0 : mQuota - mUsage));
        }
    }
}
