package com.skydrm.rmc.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.reposystem.types.BoundService;

import java.util.List;

/**
 * Created by hhu on 12/6/2016.
 */

public class HomeRepoAdapter extends ArrayAdapter<BoundService> {
    private static final boolean DEBUG = SkyDRMApp.getInstance().isDebug();
    private static final String TAG = "HomeRepoAdapter";
    private int mResourceId;
    private OnRepoSelectListener mOnRepoSelectListener;

    public HomeRepoAdapter(Context context, int resourceId, List<BoundService> objects) {
        super(context, resourceId, objects);
        mResourceId = resourceId;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final BoundService boundService = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(mResourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.repoThumbnail = view.findViewById(R.id.bottom_sheet_home_repo_thumbnail);
            viewHolder.repoName = view.findViewById(R.id.bottom_sheet_home_repo_name);
            viewHolder.repoAccountID = view.findViewById(R.id.bottom_sheet_home_repo_account_id);
            viewHolder.repoAccount = view.findViewById(R.id.bottom_sheet_home_repo_account);
            viewHolder.repoChecked = view.findViewById(R.id.bottom_sheet_home_repo_checked);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (boundService != null) {
            if (boundService.type.equals(BoundService.ServiceType.MYDRIVE)) {
                viewHolder.repoName.setText(boundService.getDisplayName());
            } else {
                viewHolder.repoName.setText(boundService.rmsNickName);
            }
            viewHolder.repoAccount.setText(boundService.account);
            if (boundService.type.equals(BoundService.ServiceType.DROPBOX)) {
                viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_dropbox);
                viewHolder.repoAccountID.setVisibility(View.GONE);
            } else if (boundService.type.equals(BoundService.ServiceType.ONEDRIVE)) {
                viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_onedrive);
                viewHolder.repoAccountID.setVisibility(View.GONE);
            } else if (boundService.type.equals(BoundService.ServiceType.SHAREPOINT)) {
                viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_sharepoint);
                viewHolder.repoAccountID.setVisibility(View.VISIBLE);
            } else if (boundService.type.equals(BoundService.ServiceType.SHAREPOINT_ONLINE)) {
                viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_sharepoint_online);
                viewHolder.repoAccountID.setVisibility(View.VISIBLE);
            } else if (boundService.type.equals(BoundService.ServiceType.GOOGLEDRIVE)) {
                viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_googledrive);
                viewHolder.repoAccountID.setVisibility(View.GONE);
            } else if (boundService.type.equals(BoundService.ServiceType.MYDRIVE)) {
                viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_my_drive);
                viewHolder.repoAccountID.setVisibility(View.GONE);
            } else if (boundService.type.equals(BoundService.ServiceType.BOX)) {
                viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_box);
                viewHolder.repoAccountID.setVisibility(View.GONE);
            }
            // check if need set as warning icon
            if (!boundService.isValidRepo()) {
                viewHolder.repoThumbnail.setImageResource(R.drawable.repo_warning);
                viewHolder.repoChecked.setVisibility(View.GONE);
            } else {
                viewHolder.repoChecked.setVisibility(View.VISIBLE);
            }
            viewHolder.repoAccountID.setText(boundService.accountID);
            viewHolder.repoChecked.setOnCheckedChangeListener(null);
            viewHolder.repoChecked.setChecked(boundService.isSelected());
        }

        viewHolder.repoChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mOnRepoSelectListener != null) {
                    mOnRepoSelectListener.onRepoSelect(isChecked, position);
                }
            }
        });
        return view;
    }

    public void setOnRepoSelectListener(OnRepoSelectListener onRepoSelectListener) {
        this.mOnRepoSelectListener = onRepoSelectListener;
    }

    public interface OnRepoSelectListener {
        void onRepoSelect(boolean isChecked, int position);
    }

    class ViewHolder {
        ImageView repoThumbnail;
        TextView repoName;
        TextView repoAccountID;
        TextView repoAccount;
        CheckBox repoChecked;
    }
}
