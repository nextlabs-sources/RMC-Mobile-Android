package com.skydrm.rmc.ui.fragment;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.fragment.protect.NormalViewBuilder;
import com.skydrm.rmc.ui.fragment.protect.NxlViewBuilder;

import butterknife.BindView;

public class CmdProtectFragment extends CmdBaseFragment {
    @BindView(R.id.bt_create_protected_file)
    Button mBtCreateProtectedFile;

    public static CmdProtectFragment newInstance() {
        return new CmdProtectFragment();
    }


    @Override
    protected void onUserFirstVisible() {

    }

    @Override
    protected void onUserVisible() {

    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.layout_cmd_protect_fragment;
    }

    @Override
    protected IViewBuilder createNxlViewBuilder(Context ctx, View root) {
        return new NxlViewBuilder(ctx);
    }

    @Override
    protected IViewBuilder createNormalViewBuilder(Context ctx, View root, boolean upload) {
        if (upload) {
            return new NormalViewBuilder(ctx, root, mBoundService, mDestFolder);
        }
        return new NormalViewBuilder(ctx, root, mRepoFile);
    }

    @Override
    protected Button getProtectedOrShareButton() {
        return mBtCreateProtectedFile;
    }
}
