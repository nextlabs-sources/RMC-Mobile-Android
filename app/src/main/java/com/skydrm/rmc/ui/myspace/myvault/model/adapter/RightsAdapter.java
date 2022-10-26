package com.skydrm.rmc.ui.myspace.myvault.model.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.RightsBean;
import com.skydrm.sdk.INxlFileFingerPrint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhu on 11/21/2017.
 */

public class RightsAdapter extends BaseAdapter {
    private boolean hideValidity;
    private Context mCtx;
    private List<RightsBean> mRights = new ArrayList<>();

    public RightsAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public void setHideValidity(boolean hideValidity) {
        this.hideValidity = hideValidity;
    }

    public void showRights(List<String> rights) {
        if (rights == null || rights.size() == 0) {
            return;
        }
        mRights.clear();
        if (rights.contains(Constant.RIGHTS_VIEW)) {
            mRights.add(new RightsBean(R.drawable.right_view_icon3,
                    mCtx.getString(R.string.View)));
        }
        if (rights.contains(Constant.RIGHTS_PRINT)) {
            mRights.add(new RightsBean(R.drawable.right_print_icon3,
                    mCtx.getString(R.string.Print)));
        }
        if (rights.contains(Constant.RIGHTS_EDIT)) {
            mRights.add(new RightsBean(R.drawable.icon_rights_edit,
                    mCtx.getString(R.string.Edit)));
        }
        if (rights.contains(Constant.RIGHTS_DOWNLOAD)) {
            mRights.add(new RightsBean(R.drawable.icon_rights_save_as,
                    mCtx.getString(R.string.Save_as)));
        }
        if (rights.contains(Constant.RIGHTS_SHARE)) {
            mRights.add(new RightsBean(R.drawable.right_share_icon3,
                    mCtx.getString(R.string.Re_share)));
        }
        if (rights.contains(Constant.RIGHTS_DECRYPT)) {
            mRights.add(new RightsBean(R.drawable.icon_rights_extract,
                    mCtx.getString(R.string.Extract)));
        }
        if (rights.contains(Constant.RIGHTS_WATERMARK)) {
            mRights.add(new RightsBean(R.drawable.right_watermark_new_icon,
                    mCtx.getString(R.string.Watermark)));
        }
        if (!hideValidity) {
            mRights.add(new RightsBean(R.drawable.right_validity_icon,
                    mCtx.getString(R.string.Validity)));
        }
        notifyDataSetChanged();
    }

    public void showRights(INxlFileFingerPrint fp) {
        List<String> rights = new ArrayList<>();
        if (fp.hasView()) {
            rights.add(Constant.RIGHTS_VIEW);
        }
        if (fp.hasPrint()) {
            rights.add(Constant.RIGHTS_PRINT);
        }
        if (fp.hasEdit()) {
            rights.add(Constant.RIGHTS_EDIT);
        }
        if (fp.hasShare()) {
            rights.add(Constant.RIGHTS_SHARE);
        }
        if (fp.hasDownload()) {
            rights.add(Constant.RIGHTS_DOWNLOAD);
        }
        if (fp.hasDecrypt()) {
            rights.add(Constant.RIGHTS_DECRYPT);
        }
        if (fp.hasWatermark()) {
            rights.add(Constant.RIGHTS_WATERMARK);
        }
        hideValidity = !fp.hasRights();
        showRights(rights);
    }

    @Override
    public int getCount() {
        return mRights.size();
    }

    @Override
    public Object getItem(int position) {
        return mRights.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RightsBean rightsBean = mRights.get(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rights_view, parent, false);
            ImageView right_icon = view.findViewById(R.id.rights_icon);
            TextView right_text = view.findViewById(R.id.rights_text);
            viewHolder = new ViewHolder(right_icon, right_text);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.icon.setImageResource(rightsBean.getRights_iconID());
        viewHolder.text.setText(rightsBean.getRights_text());
        return view;
    }

    class ViewHolder {
        ImageView icon;
        TextView text;

        ViewHolder(ImageView icon, TextView text) {
            this.icon = icon;
            this.text = text;
        }
    }
}
