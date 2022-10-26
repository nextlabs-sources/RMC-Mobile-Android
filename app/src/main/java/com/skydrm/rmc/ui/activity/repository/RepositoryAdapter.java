package com.skydrm.rmc.ui.activity.repository;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.base.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhu on 5/10/2017.
 */

public class RepositoryAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<Section> sections = new ArrayList<>();
    private OnItemClickListener itemClickListener;
    private OnItemClickListener itemDelListener;

    public RepositoryAdapter(Context context, List<BoundService> supportedRepos, List<BoundService> nextLabsRepos) {
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        sections.clear();
        sections.add(new Section(context.getString(R.string.connected_repositories), supportedRepos));
        sections.add(new Section(context.getString(R.string.nextlabs_repos), nextLabsRepos));
//        notifyAllDataSetChanged();
    }

    @Override
    public RecyclerHeaderViewHolder onCreateRecyclerHeaderViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerHeaderViewHolder(mLayoutInflater.inflate(R.layout.layout_recycler_header_repo, parent, false));
    }

    @Override
    public SectionHeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        return new SectionHeaderViewHolder(mLayoutInflater.inflate(R.layout.layout_recyler_item_header_repo, parent, false));
    }

    @Override
    public SectionItemViewHolder onCreateSectionItemViewHolder(ViewGroup parent, int viewType) {
        return new SectionItemViewHolder(mLayoutInflater.inflate(R.layout.list_item_repository_activity3, parent, false));
    }

    public void onBindSectionHeaderViewHolder(BaseAdapter.SectionHeaderViewHolder sectionHeaderViewHolder, int position, int itemViewType) {
        SectionHeaderViewHolder viewHolder = (SectionHeaderViewHolder) sectionHeaderViewHolder;
        viewHolder.headerTitle.setText(sections.get(position).sectionHeader);
    }

    @Override
    public RecyclerFooterViewHolder onCreateRecyclerFooterViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerFooterViewHolder(mLayoutInflater.inflate(R.layout.list_recyclerview_footer2, parent, false));
    }

    public void onBindSectionItemViewHolder(BaseAdapter.SectionItemViewHolder sectionItemViewHolder, final int position, int sectionIndex, int positionInSection) {
        SectionItemViewHolder viewHolder = (SectionItemViewHolder) sectionItemViewHolder;
        final BoundService boundService = sections.get(sectionIndex).mServices.get(positionInSection);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position, boundService);
                }
            }
        });
        viewHolder.iconDel.setVisibility(View.GONE);

        if (boundService.type.equals(BoundService.ServiceType.MYDRIVE)) {
            viewHolder.repoName.setText(boundService.getDisplayName());
        } else {
            viewHolder.repoName.setText(boundService.rmsNickName);
        }

        viewHolder.repoAccount.setText(boundService.account);
        if (boundService.type.equals(BoundService.ServiceType.DROPBOX)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_dropbox);
            viewHolder.repoThumbnail.setColorFilter(Color.BLACK);
            viewHolder.repoAccountID.setVisibility(View.GONE);
            viewHolder.repoAccount.setVisibility(View.VISIBLE);
        } else if (boundService.type.equals(BoundService.ServiceType.ONEDRIVE)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_onedrive);
            viewHolder.repoThumbnail.setColorFilter(Color.BLACK);
            viewHolder.repoAccountID.setVisibility(View.GONE);
            viewHolder.repoAccount.setVisibility(View.VISIBLE);
        } else if (boundService.type.equals(BoundService.ServiceType.SHAREPOINT)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_sharepoint);
            viewHolder.repoThumbnail.setColorFilter(Color.BLACK);
            viewHolder.repoAccountID.setVisibility(View.VISIBLE);
            viewHolder.repoAccount.setVisibility(View.VISIBLE);
        } else if (boundService.type.equals(BoundService.ServiceType.SHAREPOINT_ONLINE)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_sharepoint_online);
            viewHolder.repoThumbnail.setColorFilter(Color.BLACK);
            viewHolder.repoAccountID.setVisibility(View.VISIBLE);
            viewHolder.repoAccount.setVisibility(View.VISIBLE);
        } else if (boundService.type.equals(BoundService.ServiceType.GOOGLEDRIVE)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_googledrive);
            viewHolder.repoThumbnail.setColorFilter(Color.BLACK);
            viewHolder.repoAccountID.setVisibility(View.GONE);
            viewHolder.repoAccount.setVisibility(View.VISIBLE);
        } else if (boundService.type.equals(BoundService.ServiceType.BOX)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_box);
            viewHolder.repoThumbnail.setColorFilter(Color.BLACK);
            viewHolder.repoAccountID.setVisibility(View.GONE);
            viewHolder.repoAccount.setVisibility(View.VISIBLE);
        } else if (boundService.type.equals(BoundService.ServiceType.MYDRIVE)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_my_drive);
            //dismiss repo account of MyDrive
            viewHolder.repoAccount.setVisibility(View.GONE);
            viewHolder.repoAccountID.setVisibility(View.GONE);
        }
        viewHolder.repoAccountID.setText(boundService.accountID);
        // check if need set as warning icon
        if (!boundService.isValidRepo()) {
            viewHolder.repoThumbnail.setColorFilter(Color.TRANSPARENT);
            viewHolder.repoThumbnail.setImageResource(R.drawable.repo_warning);
            viewHolder.iconDel.setVisibility(View.VISIBLE);
            viewHolder.iconDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemDelListener != null) {
                        itemDelListener.onItemClick(position, boundService);
                    }
                }
            });
        }
    }

    @Override
    public int getNumberOfSections() {
        return sections.size();
    }

    @Override
    public int getNumberOfItemsInSection(int sectionIndex) {
        return sections.get(sectionIndex).mServices.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnDelItemListener(OnItemClickListener listener) {
        this.itemDelListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, BoundService service);
    }

    private static class Section {
        String sectionHeader;
        List<BoundService> mServices;

        Section(String sectionHeader, List<BoundService> services) {
            this.sectionHeader = sectionHeader;
            this.mServices = services;
        }
    }

    private class RecyclerHeaderViewHolder extends BaseAdapter.RecyclerHeaderViewHolder {
        RecyclerHeaderViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        int layoutPosition = getLayoutPosition();
                        if (layoutPosition == -1) {
                            return;
                        }
                        itemClickListener.onItemClick(layoutPosition, null);
                    }
                }
            });
        }
    }

    private class RecyclerFooterViewHolder extends BaseAdapter.RecyclerFooterViewHolder {

        RecyclerFooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class SectionHeaderViewHolder extends BaseAdapter.SectionHeaderViewHolder {
        private TextView headerTitle;

        SectionHeaderViewHolder(View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.titleTextView);
        }
    }

    private class SectionItemViewHolder extends BaseAdapter.SectionItemViewHolder {
        ImageView repoThumbnail;
        TextView repoName;
        TextView repoAccountID;
        TextView repoAccount;
        ImageView iconDel;

        SectionItemViewHolder(final View itemView) {
            super(itemView);
            repoThumbnail = itemView.findViewById(R.id.bottom_sheet_home_repo_thumbnail);
            repoName = itemView.findViewById(R.id.bottom_sheet_home_repo_name);
            repoAccountID = itemView.findViewById(R.id.bottom_sheet_home_repo_account_id);
            repoAccount = itemView.findViewById(R.id.bottom_sheet_home_repo_account);
            iconDel = itemView.findViewById(R.id.ic_del);
        }
    }
}
