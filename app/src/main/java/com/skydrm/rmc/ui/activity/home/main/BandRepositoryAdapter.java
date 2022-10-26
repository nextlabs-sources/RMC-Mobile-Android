package com.skydrm.rmc.ui.activity.home.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.reposystem.types.BoundService;

import java.util.ArrayList;
import java.util.List;

public class BandRepositoryAdapter extends RecyclerView.Adapter<BandRepositoryAdapter.BandRepositoryViewHolder> {

    private static final int DISPLAY_ITEM_VIEW = 0;
    private static final int ADD_ITEM_VIEW = 1;
    private Context mContext;
    private List<BoundService> boundServices;
    private HomeRecycleViewItemClickListener itemClickListener;

    public BandRepositoryAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<BoundService> boundServices) {
        this.boundServices = new ArrayList<>();
        for (BoundService boundService : boundServices) {
            if (boundService.type != BoundService.ServiceType.MYDRIVE) {
                this.boundServices.add(boundService);
            }
        }
    }

    @Override
    public BandRepositoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        BandRepositoryViewHolder bandRepositoryViewHolder = null;
        switch (viewType) {
            case DISPLAY_ITEM_VIEW:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_repositorie, parent, false);
                bandRepositoryViewHolder = new DisplayItemViewHolder(view);
                break;
            case ADD_ITEM_VIEW:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_add_repositorie, parent, false);
                bandRepositoryViewHolder = new AddItemViewHolder(view);
                break;
        }
        return bandRepositoryViewHolder;
    }

    @Override
    public void onBindViewHolder(BandRepositoryViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case DISPLAY_ITEM_VIEW:
                holder.bindData(boundServices.get(position - 1));
                break;
            case ADD_ITEM_VIEW:

                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ADD_ITEM_VIEW;
        }
        return DISPLAY_ITEM_VIEW;
    }

    @Override
    public int getItemCount() {
        return null == boundServices ? 0 : boundServices.size() + 1;
    }

    public void setItemClickListener(HomeRecycleViewItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    class BandRepositoryViewHolder extends RecyclerView.ViewHolder {
        View itemView;

        BandRepositoryViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

        }

        public void bindData(BoundService boundService) {

        }
    }

    class DisplayItemViewHolder extends BandRepositoryViewHolder {
        View itemView;
        TextView repositoryFileStyleName;
        ImageView repositoryLog;
        TextView repository_account;

        DisplayItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            findView();
            initListener();
        }

        private void initListener() {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != itemClickListener) {
                        int layoutPosition = getLayoutPosition();
                        if (layoutPosition != -1) {
                            itemClickListener.onSelectRepository(boundServices.get(layoutPosition - 1));
                        }
                    }
                }
            });
        }

        private void findView() {
            repositoryLog = itemView.findViewById(R.id.repository_log);
            repositoryFileStyleName = itemView.findViewById(R.id.repository_file_style_name);
            repository_account = itemView.findViewById(R.id.repository_account);
        }

        public void bindData(BoundService boundService) {
            // for repo test
            // format:
            //          repoTypeName
            //          repoNickName
            {
                String baseName;
                baseName = boundService.alias;
                String rmsName = boundService.rmsNickName;
                if (rmsName != null && !rmsName.isEmpty()) {
//                    baseName += System.lineSeparator() + rmsName;
                } else {
                    if (boundService.type == BoundService.ServiceType.GOOGLEDRIVE && boundService.account != null) {
//                        baseName += System.lineSeparator() + boundService.account;
                        rmsName = boundService.account;
                    }
                }
                repositoryFileStyleName.setText(baseName);
                repository_account.setText(rmsName);
            }
            // by osmond, remove reauth feature
            if (!boundService.isValidRepo()) {
                repositoryLog.setImageResource(R.drawable.repo_warning);
                return;
            }
            switch (boundService.type) {
                case DROPBOX:
                    repositoryLog.setImageResource(R.drawable.bottom_sheet_dropbox);
                    break;
                case SHAREPOINT_ONLINE:
                    repositoryLog.setImageResource(R.drawable.bottom_sheet_sharepoint_online);
                    break;
                case SHAREPOINT:
                    repositoryLog.setImageResource(R.drawable.bottom_sheet_sharepoint);
                    break;
                case ONEDRIVE:
                    repositoryLog.setImageResource(R.drawable.bottom_sheet_onedrive);
                    break;
                case GOOGLEDRIVE:
                    repositoryLog.setImageResource(R.drawable.bottom_sheet_googledrive);
                    break;
                case MYDRIVE:
                    break;
                case BOX:
                    repositoryLog.setImageResource(R.drawable.bottom_sheet_box);
                    break;
                default:
                    repositoryLog.setImageResource(R.drawable.bottom_sheet_onedrive);
                    break;
            }
        }
    }

    class AddItemViewHolder extends BandRepositoryViewHolder {
        View itemView;
        ImageView addRepository;

        AddItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            findView();
            initListener();
        }

        private void findView() {
            addRepository = (ImageView) itemView.findViewById(R.id.add_repository);
            addRepository.setColorFilter(mContext.getResources().getColor(R.color.white));
            addRepository.setImageResource(R.drawable.icon_connect3);
        }

        private void initListener() {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != itemClickListener) {
                        itemClickListener.onSelectConnectRepository();
                    }
                }
            });
        }

        public void bindData(BoundService boundService) {

        }
    }
}
