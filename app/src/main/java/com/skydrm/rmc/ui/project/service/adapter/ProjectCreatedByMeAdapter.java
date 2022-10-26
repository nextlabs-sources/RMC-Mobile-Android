package com.skydrm.rmc.ui.project.service.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.ui.widget.ProjectInflateIconHelper;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;

import java.util.ArrayList;
import java.util.List;

public class ProjectCreatedByMeAdapter extends RecyclerView.Adapter<ProjectCreatedByMeAdapter.ViewHolder> {
    private List<IProject> mData = new ArrayList<>();
    private final ProjectInflateIconHelper mIconHelper;
    private OnItemClickListener mListener;

    private int maxLimit = -1;

    public ProjectCreatedByMeAdapter(Context ctx) {
        this.mIconHelper = new ProjectInflateIconHelper(ctx);
    }

    public void setMaxLimit(int limit) {
        this.maxLimit = limit;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<IProject> data) {
        mData.clear();
        if (data != null && data.size() != 0) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_projects_owner_by_me, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        IProject p = mData.get(position);
        holder.displayName.setText(p.getDisplayName());
        holder.totalFiles.setText(String.valueOf(p.getTotalFiles()));
        mIconHelper.inflateInitial(holder.headPortrait, p.listMember());
    }

    @Override
    public int getItemCount() {
        if (maxLimit != -1) {
            return maxLimit > mData.size() ? mData.size() : maxLimit;
        }
        return mData == null ? 0 : mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView displayName;
        TextView totalFiles;
        FlowLayout headPortrait;

        public ViewHolder(View itemView) {
            super(itemView);
            findView(itemView);
            initListener(itemView);
        }

        private void initListener(View itemView) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.OnItemClick(mData.get(getLayoutPosition()));
                    }
                }
            });
        }

        private void findView(View itemView) {
            displayName = itemView.findViewById(R.id.displayName);
            headPortrait = itemView.findViewById(R.id.head_portrait);
            totalFiles = itemView.findViewById(R.id.total_Files);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(IProject p);
    }
}
