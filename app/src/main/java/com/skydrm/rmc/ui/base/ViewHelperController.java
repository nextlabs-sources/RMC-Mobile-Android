package com.skydrm.rmc.ui.base;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.base.BaseViewHelperImpl;
import com.skydrm.rmc.ui.base.IBaseViewHelper;

/**
 * Created by hhu on 5/3/2017.
 */

public class ViewHelperController {
    private IBaseViewHelper helper;

    public ViewHelperController(View view) {
        this(new BaseViewHelperImpl(view));
    }

    public ViewHelperController(IBaseViewHelper helper) {
        this.helper = helper;
    }

    public void showNetworkError(View.OnClickListener onClickListener) {
        View layout = helper.inflate(R.layout.message);
        TextView textView = layout.findViewById(R.id.message_info);
        textView.setText(helper.getContext().getResources().getString(R.string.common_no_network_msg));

        ImageView imageView = layout.findViewById(R.id.message_icon);
        imageView.setImageResource(R.drawable.ic_error_page);

        if (null != onClickListener) {
            layout.setOnClickListener(onClickListener);
        }
        helper.showLayout(layout);
    }

    public void showError(String errorMsg, View.OnClickListener onClickListener) {
        View layout = helper.inflate(R.layout.message);
        TextView textView = (TextView) layout.findViewById(R.id.message_info);
        if (!TextUtils.isEmpty(errorMsg)) {
            textView.setText(errorMsg);
        } else {
            textView.setText(helper.getContext().getResources().getString(R.string.common_error_msg));
        }

        ImageView imageView = layout.findViewById(R.id.message_icon);
        imageView.setImageResource(R.drawable.ic_error_page);

        if (null != onClickListener) {
            layout.setOnClickListener(onClickListener);
        }
        helper.showLayout(layout);
    }

    public void showEmpty(String emptyMsg) {
        View layout = helper.inflate(R.layout.layout_empty_folder);
        TextView textView = layout.findViewById(R.id.nxfile_empty_descript);
        if (!TextUtils.isEmpty(emptyMsg)) {
            textView.setText(emptyMsg);
        } else {
            textView.setText(helper.getContext().getResources().getString(R.string.empty_folder));
        }
        helper.showLayout(layout);
    }

    public void showLoading(String msg) {
        View layout = helper.inflate(R.layout.loading);
        if (!TextUtils.isEmpty(msg)) {
            TextView textView = layout.findViewById(R.id.loading_msg);
            textView.setText(msg);
        }
        helper.showLayout(layout);
    }

    public void showNoRepositoryView(String msg, View.OnClickListener onClickListener) {
        View layout = helper.inflate(R.layout.layout_empty_repos);
        if (null != onClickListener) {
            layout.findViewById(R.id.bt_select_repo).setOnClickListener(onClickListener);
        }
        if (!TextUtils.isEmpty(msg)) {
            TextView textView = layout.findViewById(R.id.nxfile_norepo_description);
            textView.setText(msg);
        }
        helper.showLayout(layout);
    }

    public void restore() {
        helper.restoreView();
    }
}
