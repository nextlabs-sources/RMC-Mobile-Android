package com.skydrm.rmc.ui.service.share.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class ProjectListAdapter extends BaseExpandableListAdapter {
    private List<ProjectGroupItem> mData = new ArrayList<>();
    private List<ProjectGroupItem> mCreateByMeData = new ArrayList<>();
    private List<ProjectGroupItem> mInvitedByOtherData = new ArrayList<>();
    private int mIgnoreId = -1;

    private List<String> mSelectedRecipients;

    public List<ProjectGroupItem> getData() {
        return mData;
    }

    public boolean isCreateByMeEmpty() {
        return mCreateByMeData.isEmpty();
    }

    void setIgnoreDisplayProjectId(int projectId) {
        this.mIgnoreId = projectId;
    }

    void setSelectedRecipients(List<String> recipients) {
        this.mSelectedRecipients = recipients;
    }

    List<IProject> getSelectData() {
        List<IProject> ret = new ArrayList<>();
        if (mData == null || mData.size() == 0) {
            return ret;
        }
        for (ProjectGroupItem group : mData) {
            if (group == null) {
                continue;
            }
            List<ProjectNormalItem> children = group.getChildren();
            if (children == null || children.isEmpty()) {
                continue;
            }
            for (ProjectNormalItem child : children) {
                if (child == null) {
                    continue;
                }
                if (child.isSelected()) {
                    ret.add(child.getProject());
                }
            }
        }
        return ret;
    }

    List<IProject> getAll() {
        List<IProject> ret = new ArrayList<>();
        if (mData == null || mData.size() == 0) {
            return ret;
        }
        for (ProjectGroupItem group : mData) {
            if (group == null) {
                continue;
            }
            List<ProjectNormalItem> children = group.getChildren();
            if (children == null || children.isEmpty()) {
                continue;
            }
            for (ProjectNormalItem child : children) {
                if (child == null) {
                    continue;
                }
                ret.add(child.getProject());
            }
        }
        return ret;
    }

    void setOwnerByMeData(Context ctx, List<IProject> data) {
        mData.clear();
        if (data != null && !data.isEmpty()) {
            List<IProject> filtered = new ArrayList<>();
            for (IProject p : data) {
                if (p == null) {
                    continue;
                }
                // filter pending invite.
                if (p.isPendingInvite()) {
                    continue;
                }
                if (p.getId() == mIgnoreId) {
                    continue;
                }
                filtered.add(p);
            }
            mCreateByMeData.clear();
            mCreateByMeData.add(ProjectGroupItem.newByCreateByMeData(ctx, filtered, mSelectedRecipients));
            mData.addAll(mCreateByMeData);
        }
        mData.addAll(mInvitedByOtherData);
        notifyDataSetChanged();
    }

    void setOwnerByOtherData(Context ctx, List<IProject> data) {
        mData.clear();
        mData.addAll(mCreateByMeData);
        if (data != null && !data.isEmpty()) {
            List<IProject> filtered = new ArrayList<>();
            for (IProject p : data) {
                if (p == null) {
                    continue;
                }
                // filter pending invite.
                if (p.isPendingInvite()) {
                    continue;
                }
                if (p.getId() == mIgnoreId) {
                    continue;
                }
                filtered.add(p);
            }
            mInvitedByOtherData.clear();
            mInvitedByOtherData.add(ProjectGroupItem.newByInvitedByOtherData(ctx, filtered, mSelectedRecipients));
            mData.addAll(mInvitedByOtherData);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ProjectGroupItem groupItem = mData.get(groupPosition);
        if (groupItem == null) {
            return 0;
        }
        return groupItem.getChildren().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ProjectGroupItem groupItem = mData.get(groupPosition);
        if (groupItem == null) {
            return null;
        }
        return groupItem.getChildren().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_list_group, parent, false);
            vh = new GroupViewHolder();
            vh.tvTitle = convertView.findViewById(R.id.tv_group_title);
            convertView.setTag(vh);
        } else {
            vh = (GroupViewHolder) convertView.getTag();
        }
        ProjectGroupItem groupItem = mData.get(groupPosition);
        if (groupItem != null) {
            vh.tvTitle.setText(groupItem.getTitle());
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_list_normal, parent, false);
            vh = new ChildViewHolder();
            vh.ivProjectIcon = convertView.findViewById(R.id.iv_project_icon);
            vh.tvName = convertView.findViewById(R.id.tv_project_name);
            vh.tvDate = convertView.findViewById(R.id.tv_date);
            vh.tvFileNums = convertView.findViewById(R.id.tv_file_num);
            vh.ivSelected = convertView.findViewById(R.id.iv_selected);
            convertView.setTag(vh);
        } else {
            vh = (ChildViewHolder) convertView.getTag();
        }
        ProjectGroupItem groupItem = mData.get(groupPosition);
        if (groupItem == null) {
            return convertView;
        }
        List<ProjectNormalItem> children = groupItem.getChildren();
        if (children == null || children.isEmpty()) {
            return convertView;
        }
        ProjectNormalItem normalItem = children.get(childPosition);
        if (normalItem == null) {
            return convertView;
        }
        IProject p = normalItem.getProject();
        if (p == null) {
            return convertView;
        }
        vh.tvName.setText(p.getDisplayName());
        vh.tvDate.setText(TimeUtil.formatLibraryFileDate(p.getCreationTime()));
        vh.tvFileNums.setText(String.valueOf(p.getTotalFiles()));
        vh.ivProjectIcon.setImageResource(p.isOwnedByMe() ? R.drawable.icon_project_created_by_me
                : R.drawable.icon_project_invited_by_other);
        vh.ivSelected.setVisibility(normalItem.isSelected() ? View.VISIBLE : View.INVISIBLE);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class GroupViewHolder {
        TextView tvTitle;
    }

    private static class ChildViewHolder {
        ImageView ivProjectIcon;
        TextView tvName;
        TextView tvDate;
        TextView tvFileNums;
        ImageView ivSelected;
    }

}
