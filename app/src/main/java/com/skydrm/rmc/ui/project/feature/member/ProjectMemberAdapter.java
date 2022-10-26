package com.skydrm.rmc.ui.project.feature.member;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IPendingMember;
import com.skydrm.rmc.ui.widget.avatar.AvatarPlaceholder;
import com.skydrm.rmc.ui.widget.avatar.AvatarView;
import com.skydrm.rmc.ui.widget.swipelayout.SwipeMenuLayout;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class ProjectMemberAdapter extends RecyclerView.Adapter<ProjectMemberAdapter.ViewHolder> {
    private static final int TYPE_GROUP_ITEM = 1;
    private static final int TYPE_NORMAL_ITEM = 2;

    private Context mCtx;
    private List<MemberItem> mData = new ArrayList<>();

    public ProjectMemberAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    private OnItemClickListener mListener;

    public void setData(List<MemberItem> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_GROUP_ITEM:
                return new ItemGroupViewHolder(LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_project_title_item, parent, false));
            case TYPE_NORMAL_ITEM:
                return new ItemContentViewHolder(LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.layout_project_membres_item, parent, false));
        }
        throw new IllegalArgumentException("Unrecognized view type " + viewType + "performed.");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bandData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_GROUP_ITEM;
        }
        String currentTitle = mData.get(position).title;
        boolean isDifferent = !mData.get(position - 1).title.equals(currentTitle);
        return isDifferent ? TYPE_GROUP_ITEM : TYPE_NORMAL_ITEM;
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bandData(MemberItem item);
    }

    class ItemGroupViewHolder extends ItemContentViewHolder {
        TextView tvTitle;
        View itemView;

        ItemGroupViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvTitle = itemView.findViewById(R.id.group_item_name);
        }

        @Override
        void bandData(MemberItem item) {
            super.bandData(item);
            tvTitle.setText(item.title);
        }
    }

    class ItemContentViewHolder extends ViewHolder {
        TextView tvName;
        TextView tvDate;
        SwipeMenuLayout swipeMenuLayout;
        AvatarView roundHead;
        ImageView ivMemberInfo;
        TextView tvPeopleHost;
        TextView tvActionName;

        ItemContentViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_file_name_project);
            tvDate = itemView.findViewById(R.id.people_date);
            tvActionName = itemView.findViewById(R.id.tv_action_name);
            swipeMenuLayout = itemView.findViewById(R.id.swipe_menu_layout_my_vault);
            roundHead = itemView.findViewById(R.id.members_round_Head);
            tvPeopleHost = itemView.findViewById(R.id.people_host);
            ivMemberInfo = itemView.findViewById(R.id.members_info_button);
            ivMemberInfo.setVisibility(View.GONE);
            //ivMemberInfo.setEnabled(false);
            //ivMemberInfo.setClickable(false);
            swipeMenuLayout.setSwipeLeftEnable(false);
            swipeMenuLayout.setSwipeRightEnable(false);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(mData.get(getLayoutPosition()).member);
                    }
                }
            });
        }

        @Override
        void bandData(MemberItem item) {
            IMember member = item.member;

            if (member.isPending()) {
                IPendingMember pending = (IPendingMember) member;
                tvName.setText(pending.getInviteeEmail().trim());
                tvDate.setText(TimeUtil.formatData(pending.getInviteTime()));
                roundHead.setImageDrawable(new AvatarPlaceholder(mCtx, pending.getInviteeEmail(),
                        30, "", " "));
                tvPeopleHost.setVisibility(View.GONE);
                ivMemberInfo.setImageResource(R.drawable.icon_setting_v3);
                tvActionName.setText(mCtx.getString(R.string.Invited));
            } else {
                tvName.setText(member.getDisplayName().trim());
                tvDate.setText(TimeUtil.formatData(member.getCreationTime()));
                if (member.isOwner()) {
                    tvPeopleHost.setVisibility(View.VISIBLE);
                } else {
                    tvPeopleHost.setVisibility(View.GONE);
                }
                tvActionName.setText(mCtx.getString(R.string.Joined));
                roundHead.setImageDrawable(new AvatarPlaceholder(mCtx, member.getDisplayName(),
                        30, "", " "));
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(IMember member);
    }
}
