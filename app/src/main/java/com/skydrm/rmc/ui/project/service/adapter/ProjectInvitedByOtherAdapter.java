package com.skydrm.rmc.ui.project.service.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IInvitePending;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.Project;
import com.skydrm.rmc.ui.widget.ProjectInflateIconHelper;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;

import java.util.ArrayList;
import java.util.List;

public class ProjectInvitedByOtherAdapter extends RecyclerView.Adapter<ProjectInvitedByOtherAdapter.ViewHolder> {
    private static final int TYPE_PENDING = 0;
    private static final int TYPE_NORMAL = 1;

    private List<IProject> mData = new ArrayList<>();
    private ProjectInflateIconHelper mIconHelper;

    private OnItemClickListener mOnItemClickListener;
    private OnInvitationItemClickListener mInvitationClickListener;

    private int maxLimit = -1;

    public ProjectInvitedByOtherAdapter(Context ctx) {
        this.mIconHelper = new ProjectInflateIconHelper(ctx);
    }

    public void setMaxLimit(int limit) {
        this.maxLimit = limit;
    }

    public void setData(List<IProject> data) {
        mData.clear();
        if (data != null && data.size() != 0) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnInvitationItemClickListener(OnInvitationItemClickListener listener) {
        this.mInvitationClickListener = listener;
    }

    @NonNull
    @Override
    public ProjectInvitedByOtherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_PENDING) {
            return new InvitePendingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_projects_pending_invitation,
                    parent, false));
        } else if (viewType == TYPE_NORMAL) {
            return new NormalViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_projects_owner_by_other,
                    parent, false));
        }
        throw new IllegalArgumentException("Unrecognized view type" + viewType + "performed.");
    }

    @Override
    public void onBindViewHolder(ProjectInvitedByOtherAdapter.ViewHolder holder, int position) {
        holder.bandData(mData.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).isPendingInvite() ? TYPE_PENDING : TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        if (maxLimit != -1) {
            return maxLimit > mData.size() ? mData.size() : maxLimit;
        }
        return mData == null ? 0 : mData.size();
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bandData(IProject project);
    }

    class NormalViewHolder extends ViewHolder {
        TextView tvProjectName;
        FlowLayout flProjectMember;
        TextView tvTotalFiles;

        NormalViewHolder(View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.accepted_Name);
            flProjectMember = itemView.findViewById(R.id.accepted_round_head);
            tvTotalFiles = itemView.findViewById(R.id.project_invited_by_other_total_files);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mData.get(getLayoutPosition()), getLayoutPosition());
                    }
                }
            });
        }

        @Override
        void bandData(IProject project) {
            tvProjectName.setText(project.getDisplayName());
            tvTotalFiles.setText(String.valueOf(project.getTotalFiles()));
            mIconHelper.inflateInitial(flProjectMember, project.listMember());
        }
    }

    public class InvitePendingViewHolder extends ViewHolder {
        TextView tvProjectName;
        TextView tvOwnerName;
        TextView tvInviterName;
        Button btAcceptInvitation;
        Button btDenyInvitation;
        RelativeLayout mLoadingProgressBar;

        InvitePendingViewHolder(View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.project_name);
            tvOwnerName = itemView.findViewById(R.id.owner_name);
            tvInviterName = itemView.findViewById(R.id.invited_by_name);
            btAcceptInvitation = itemView.findViewById(R.id.accept_invitation);
            btDenyInvitation = itemView.findViewById(R.id.ignore);
            mLoadingProgressBar = itemView.findViewById(R.id.invited_by_other_progressBar);

            btAcceptInvitation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = getLayoutPosition();
                    if (layoutPosition == -1) {
                        return;
                    }
                    IInvitePending pending = (Project) mData.get(layoutPosition);
                    if (mInvitationClickListener != null) {
                        mInvitationClickListener.onAccept(pending, mLoadingProgressBar, layoutPosition);
                    }
                }
            });
            btDenyInvitation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = getLayoutPosition();
                    if (layoutPosition == -1) {
                        return;
                    }
                    IInvitePending pending = (Project) mData.get(layoutPosition);
                    if (mInvitationClickListener != null) {
                        mInvitationClickListener.onDeny(pending, mLoadingProgressBar, layoutPosition);
                    }
                }
            });
        }

        @Override
        void bandData(IProject project) {
            Project p = (Project) project;
            tvProjectName.setText(project.getDisplayName());
            tvOwnerName.setText(project.getOwner().getName());
            tvInviterName.setText(p.getInviterDisplayName());
        }
    }


    public interface OnItemClickListener {
        void onItemClick(IProject p, int position);
    }

    public interface OnInvitationItemClickListener {
        void onAccept(IInvitePending pending, View loadingBar, int pos);

        void onDeny(IInvitePending pending, View loadingBar, int pos);
    }
}
