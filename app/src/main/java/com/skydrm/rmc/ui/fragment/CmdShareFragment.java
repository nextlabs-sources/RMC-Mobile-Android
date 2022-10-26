package com.skydrm.rmc.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.eventBusMsg.ChangeExpiryDateEvent;
import com.skydrm.rmc.engine.eventBusMsg.ShowConvertProgressEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.activity.CmdOperateFileActivity2;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.fragment.share.NormalViewBuilder;
import com.skydrm.rmc.ui.fragment.share.NxlViewBuilder;
import com.skydrm.rmc.ui.project.feature.service.protect.IFileDownloader;
import com.skydrm.rmc.ui.project.feature.service.protect.download.LibraryFileDownloader;
import com.skydrm.rmc.ui.project.feature.service.protect.download.WorkSpaceFileDownloader;
import com.skydrm.rmc.utils.ViewUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.sdk.INxlFileFingerPrint;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Locale;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;

public class CmdShareFragment extends CmdBaseFragment {
    @BindView(R.id.bt_share_protected_file)
    Button mBtShareProtectedFile;

    public static CmdShareFragment newInstance() {
        return new CmdShareFragment();
    }

    public void updateContactParcel(Intent data) {
        if (mViewBuilder != null) {
            mViewBuilder.wrapContacts(data);
        }
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
        return R.layout.layout_cmd_share_fragment;
    }


    @Override
    protected IViewBuilder createNxlViewBuilder(Context ctx, View root) {
        return new NxlViewBuilder(ctx, root);
    }

    @Override
    protected IViewBuilder createNormalViewBuilder(Context ctx, View root, boolean upload) {
        if (upload) {
            return new NormalViewBuilder(ctx,root,mBoundService,mDestFolder);
        }
        return new NormalViewBuilder(ctx, root);
    }

    @Override
    protected Button getProtectedOrShareButton() {
        return mBtShareProtectedFile;
    }
}
