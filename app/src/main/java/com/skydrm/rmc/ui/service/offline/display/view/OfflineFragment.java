package com.skydrm.rmc.ui.service.offline.display.view;

import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.LinearLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeFile;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.myspace.myvault.presenter.OfflinePresenter;
import com.skydrm.rmc.ui.myspace.myvault.view.fragment.VaultBaseFragment;
import com.skydrm.rmc.ui.myspace.myvault.view.widget.MyVaultFileMenu;
import com.skydrm.rmc.ui.myspace.sharewithme.view.SharedWithMeFileMenu;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.utils.ViewUtils;

import butterknife.BindView;

public class OfflineFragment extends VaultBaseFragment {
    @BindView(R.id.ll_offline_header)
    LinearLayout mLlOfflineHeader;

    private SharedWithMeFileMenu mSharedWithMeFileCtxMenu;

    public static OfflineFragment newInstance() {
        return new OfflineFragment();
    }

    @Override
    protected IFileContact.IPresenter createPresenter() {
        return new OfflinePresenter(this, mSortType);
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_offline;
    }

    @Override
    protected int getFileType() {
        return NxlFileType.OFFLINE.getValue();
    }

    @Override
    protected void initViewAndEvents() {
        super.initViewAndEvents();
        initSharedWithMeFileCtxMenu();
    }

    @Override
    protected void showFileCtxMenu(INxlFile f, int pos) {
        if (f instanceof MyVaultFile) {
            showMyVaultFileCtxMenu(f, pos);
        } else if (f instanceof SharedWithMeFile) {
            showSharedWithMeFileCtxMenu(f, pos);
        }
    }

    @Override
    protected void unMarkAsOffline(INxlFile f, int pos) {
        f.unMarkAsOffline();
        listCurrent();
    }

    @Override
    protected void networkConnected(String extraInfo) {
        super.networkConnected(extraInfo);
        if (ViewUtils.isVisible(mLlOfflineHeader)) {
            mLlOfflineHeader.setVisibility(View.GONE);
        }
    }

    @Override
    protected void networkDisconnected() {
        super.networkDisconnected();
        if (ViewUtils.isGone(mLlOfflineHeader)) {
            mLlOfflineHeader.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void notifyItemDelete(int pos) {
        listCurrent();
    }

    private void initSharedWithMeFileCtxMenu() {
        if (mSharedWithMeFileCtxMenu == null) {
            mSharedWithMeFileCtxMenu = SharedWithMeFileMenu.newInstance();
            mSharedWithMeFileCtxMenu.setOnItemClickListener(new SharedWithMeFileMenu.OnItemClickListener() {
                @Override
                public void onSetOffline(INxlFile f, int pos, boolean offline) {
                    unMarkAsOffline(f, pos);
                }
            });
        }
    }

    private void showMyVaultFileCtxMenu(INxlFile f, int pos) {
        FragmentManager fm = getFragmentManager();
        if (fm == null) {
            return;
        }
        mFileMenu.setFile(f);
        mFileMenu.setPosition(pos);
        mFileMenu.show(fm, MyVaultFileMenu.class.getSimpleName());
    }

    private void showSharedWithMeFileCtxMenu(INxlFile f, int pos) {
        FragmentManager fm = getFragmentManager();
        if (fm == null) {
            return;
        }
        mSharedWithMeFileCtxMenu.setSharedWithMeFile(f);
        mSharedWithMeFileCtxMenu.setPosition(pos);
        mSharedWithMeFileCtxMenu.show(fm, SharedWithMeFileMenu.class.getSimpleName());
    }
}
