package com.skydrm.rmc.ui.project.service.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.IInvitePending;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.Project;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.project.ProjectActivity;
import com.skydrm.rmc.ui.project.service.NewProjectActivity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ViewAllProjectAdapter extends RecyclerView.Adapter<ViewAllProjectAdapter.ViewHolder> {
    private static final int TYPE_PENDING_ITEM = 0;
    private static final int TYPE_OWNER_BY_ME_GROUP = 1;
    private static final int TYPE_OWNER_BY_OTHER_GROUP = 2;
    private static final int TYPE_NORMAL_ITEM = 3;

    private Context mCtx;
    private List<IProject> mData = new ArrayList<>();
    private List<IProject> mPending = new ArrayList<>();
    private List<IProject> mOwnerByMe = new ArrayList<>();
    private List<IProject> mOwnerByOther = new ArrayList<>();

    private int mOwnerByMeSize;
    private int mOwnerByOtherSize;
    private OnInvitationItemClickListener mInvitationClickListener;

    public ViewAllProjectAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public void setOnInvitationItemClickListener(OnInvitationItemClickListener listener) {
        this.mInvitationClickListener = listener;
    }

    public void setOwnerByMeData(List<IProject> data) {
        mOwnerByMe.clear();
        mOwnerByMeSize = 0;
        if (data != null) {
            mOwnerByMe.addAll(data);
            mOwnerByMeSize = data.size();
        }
        mData.clear();
        mData.addAll(mPending);
        if (data != null) {
            mData.addAll(data);
        }
        mData.addAll(mOwnerByOther);
        notifyDataSetChanged();
    }

    public void setOwnerByOtherAndPendingData(List<IProject> pending, List<IProject> other) {
        mPending.clear();
        mOwnerByOther.clear();
        mOwnerByOtherSize = 0;
        if (other != null) {
            mPending.addAll(pending);
            mOwnerByOther.addAll(other);
            mOwnerByOtherSize = other.size();
        }
        mData.clear();
        mData.addAll(pending);
        mData.addAll(mOwnerByMe);
        if (other != null) {
            mData.addAll(other);
        }
        notifyDataSetChanged();
    }

    public void setAllData(List<IProject> data) {
        mData.clear();
        mOwnerByMeSize = 0;
        mOwnerByOtherSize = 0;

        if (data != null) {
            for (IProject p : data) {
                if (p.isOwnedByMe()) {
                    mOwnerByMeSize++;
                } else {
                    mOwnerByOtherSize++;
                }
                mData.add(p);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        IProject current = mData.get(position);
        if (position == 0) {
            if (current.isPendingInvite()) {
                return TYPE_PENDING_ITEM;
            }
            if (current.isOwnedByMe()) {
                return TYPE_OWNER_BY_ME_GROUP;
            }
            return TYPE_OWNER_BY_OTHER_GROUP;
        }
        IProject preview = mData.get(position - 1);
        if (preview.isPendingInvite() && current.isPendingInvite()) {
            return TYPE_PENDING_ITEM;
        }
        if (preview.isPendingInvite() && !current.isPendingInvite()) {
            if (current.isOwnedByMe()) {
                return TYPE_OWNER_BY_ME_GROUP;
            }
            return TYPE_OWNER_BY_OTHER_GROUP;
        }
        if (preview.isOwnedByMe() && current.isOwnedByMe()) {
            return TYPE_NORMAL_ITEM;
        }
        if (preview.isOwnedByMe() && !current.isOwnedByMe()) {
            return TYPE_OWNER_BY_OTHER_GROUP;
        }
        return TYPE_NORMAL_ITEM;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_PENDING_ITEM:
                return new PendingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_project,
                        parent, false));
            case TYPE_OWNER_BY_ME_GROUP:
                return new OwnerByMeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_by_me_group,
                        parent, false));
            case TYPE_OWNER_BY_OTHER_GROUP:
                return new OwnerByOtherViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_by_other_group,
                        parent, false));
            case TYPE_NORMAL_ITEM:
                return new NormalViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project,
                        parent, false));

        }
        throw new IllegalArgumentException("Unrecognized view type " + viewType + " performed.");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bandData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bandData(IProject project);
    }

    class PendingViewHolder extends ViewHolder {
        private final TextView tvInviteDetail;
        private final Button btAccept;
        private final View btIgnore;
        private final View loadingBar;


        PendingViewHolder(View itemView) {
            super(itemView);
            tvInviteDetail = itemView.findViewById(R.id.invite_detail);
            btAccept = itemView.findViewById(R.id.accept_invitation);
            btIgnore = itemView.findViewById(R.id.ignore);

            loadingBar = itemView.findViewById(R.id.pending_progressBar);
            btAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = getLayoutPosition();
                    if (layoutPosition == -1) {
                        return;
                    }
                    IInvitePending pending = (IInvitePending) mData.get(layoutPosition);
                    if (mInvitationClickListener != null) {
                        mInvitationClickListener.onAccept(pending, loadingBar, layoutPosition);
                    }
                }
            });
            btIgnore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = getLayoutPosition();
                    if (layoutPosition == -1) {
                        return;
                    }
                    IInvitePending pending = (IInvitePending) mData.get(layoutPosition);
                    if (mInvitationClickListener != null) {
                        mInvitationClickListener.onDeny(pending, loadingBar, layoutPosition);
                    }
                }
            });
        }

        @Override
        void bandData(IProject p) {
            IInvitePending pending = (Project) p;

            String format = MessageFormat.format(mCtx.getString(R.string.has_invited_you_to_join_his_project)
                    , pending.getInviterDisplayName()
                    , p.getDisplayName());

            SpannableString spannableString = new SpannableString(format);
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ff33b5e5")),
                    0, pending.getInviterDisplayName().length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ff33b5e5")),
                    spannableString.length() - p.getDisplayName().length(),
                    spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            tvInviteDetail.setText(spannableString);
        }
    }

    class NormalViewHolder extends ViewHolder {
        private final TextView tvProjectName;
        private final TextView tvOwnerName;
        private final TextView tvFileMemberSize;

        NormalViewHolder(View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.project_name);
            tvOwnerName = itemView.findViewById(R.id.owner_name);
            tvFileMemberSize = itemView.findViewById(R.id.file_members_size);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewProject(mData.get(getLayoutPosition()));
                }
            });
        }

        @Override
        void bandData(IProject p) {
            tvProjectName.setText(p.getDisplayName());
            tvOwnerName.setText(p.getOwner().getName());
            tvFileMemberSize.setText(MessageFormat.format(mCtx.getString(R.string.File_Member)
                    , String.valueOf(p.getTotalFiles())
                    , String.valueOf(p.getTotalMembers())));
        }
    }

    class OwnerByMeViewHolder extends NormalViewHolder {
        private final TextView tvTt1;
        private final TextView tvTt2;
        private final TextView tvTt3;
        private final CardView cvNewProjectBg;

        OwnerByMeViewHolder(View itemView) {
            super(itemView);
            tvTt1 = itemView.findViewById(R.id.item_project_title_1);
            tvTt2 = itemView.findViewById(R.id.item_project_title_2);
            tvTt3 = itemView.findViewById(R.id.item_project_title_3);
            cvNewProjectBg = itemView.findViewById(R.id.cv_new_project_bg);
            cvNewProjectBg.setVisibility(SkyDRMApp.getInstance().isOnPremise() ? View.GONE : View.VISIBLE);
            cvNewProjectBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    go2NewProject();
                }
            });
        }

        @Override
        void bandData(IProject p) {
            super.bandData(p);
            tvTt1.setText(mCtx.getString(R.string.Projects_create_by).concat(" "));
            tvTt2.setText(mCtx.getString(R.string.me));
            String titleStringThree = mCtx.getString(R.string.Projects_create_by_me_count);
            tvTt3.setText(String.format(titleStringThree, String.valueOf(mOwnerByMeSize)));
        }
    }

    private void go2NewProject() {
        Intent intent = new Intent(mCtx, NewProjectActivity.class);
        mCtx.startActivity(intent);
    }

    class OwnerByOtherViewHolder extends NormalViewHolder {
        private final TextView tvTt1;
        private final TextView tvTt2;
        private final TextView tvTt3;

        OwnerByOtherViewHolder(View itemView) {
            super(itemView);
            tvTt1 = itemView.findViewById(R.id.item_project_title_1);
            tvTt2 = itemView.findViewById(R.id.item_project_title_2);
            tvTt3 = itemView.findViewById(R.id.item_project_title_3);
        }

        @Override
        void bandData(IProject p) {
            super.bandData(p);
            tvTt1.setText(mCtx.getString(R.string.Projects_invited_by).concat(" "));
            tvTt2.setText(mCtx.getString(R.string.others));
            String titleStringThree = mCtx.getString(R.string.Projects_invited_by_other_count);
            tvTt3.setText(String.format(titleStringThree, String.valueOf(mOwnerByOtherSize)));
        }
    }

    private void viewProject(IProject p) {
        Intent intentProjectActivity = new Intent(mCtx, ProjectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constant.PROJECT_DETAIL, (Parcelable) p);
        bundle.putInt(Constant.PROJECT_INDEX, 0);
        intentProjectActivity.putExtras(bundle);
        mCtx.startActivity(intentProjectActivity);
    }

    public interface OnInvitationItemClickListener {
        void onAccept(IInvitePending pending, View loadingBar, int pos);

        void onDeny(IInvitePending pending, View loadingBar, int pos);
    }
}
