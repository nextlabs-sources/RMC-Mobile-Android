package com.skydrm.rmc.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.reposystem.types.BoundService;

import java.util.List;

/**
 * Created by hhu on 5/10/2017.
 */

public class LibrarySelectAdapter extends ArrayAdapter<BoundService> {
    private int mResourceId;
    private List<BoundService> objects;

    public LibrarySelectAdapter(@NonNull Context context, @LayoutRes int resource, List<BoundService> objects) {
        super(context, resource, objects);
        this.mResourceId = resource;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BoundService boundService = objects.get(position);
        // sanity check
        if (boundService == null) {
            throw new NullPointerException("LibrarySelectAdapter,in getView, repo is null");
        }
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(mResourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.repoThumbnail = (ImageView) view.findViewById(R.id.bottom_sheet_home_repo_thumbnail);
            viewHolder.repoName = (TextView) view.findViewById(R.id.bottom_sheet_home_repo_name);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (boundService.type.equals(BoundService.ServiceType.MYDRIVE)) {
            viewHolder.repoName.setText(boundService.getDisplayName());
        } else {
            viewHolder.repoName.setText(boundService.rmsNickName);
        }

        if (boundService.type.equals(BoundService.ServiceType.DROPBOX)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_dropbox);
            viewHolder.repoThumbnail.setColorFilter(Color.BLACK);
        } else if (boundService.type.equals(BoundService.ServiceType.ONEDRIVE)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_onedrive);
            viewHolder.repoThumbnail.setColorFilter(Color.BLACK);
        } else if (boundService.type.equals(BoundService.ServiceType.SHAREPOINT)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_sharepoint);
            viewHolder.repoThumbnail.setColorFilter(Color.BLACK);
        } else if (boundService.type.equals(BoundService.ServiceType.SHAREPOINT_ONLINE)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_sharepoint_online);
            viewHolder.repoThumbnail.setColorFilter(Color.BLACK);
        } else if (boundService.type.equals(BoundService.ServiceType.GOOGLEDRIVE)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_googledrive);
            viewHolder.repoThumbnail.setColorFilter(Color.BLACK);
        } else if (boundService.type.equals(BoundService.ServiceType.MYDRIVE)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_my_drive);
        } else if (boundService.type.equals(BoundService.ServiceType.BOX)) {
            viewHolder.repoThumbnail.setImageResource(R.drawable.bottom_sheet_box);
        }
        return view;
    }

    class ViewHolder {
        ImageView repoThumbnail;
        TextView repoName;
    }
}
