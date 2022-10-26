package com.skydrm.rmc.ui.repository.feature;

import android.os.Bundle;
import android.os.Environment;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.ui.repository.contact.LibraryFilePresenter;

public class LibraryFileFragment extends NxlBaseFragment {
    private String mRootPathId = Environment.getExternalStorageDirectory().getPath().concat("/");
    private OnItemClickListener mOnItemClickListener;
    private OnPopupFragmentListener mOnPopupFragmentListener;

    public static LibraryFileFragment newInstance() {
        return new LibraryFileFragment();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnPopupFragmentListener(OnPopupFragmentListener listener) {
        this.mOnPopupFragmentListener = listener;
    }

    @Override
    public void showSortMenu() {

    }

    @Override
    protected boolean showToolbar() {
        return true;
    }

    @Override
    protected boolean resolveBundle(Bundle arguments) {
        return true;
    }

    @Override
    protected IFileContact.IPresenter createPresenter() {
        return new LibraryFilePresenter((IDataService) RepoFactory.getRepo(RepoType.TYPE_LIBRARY),
                this, mRootPathId);
    }

    @Override
    protected int getContentViewLayoutID() {
        return super.getContentViewLayoutID();
    }

    @Override
    protected int getFileType() {
        return NxlFileType.ALL.getValue();
    }

    @Override
    protected String getRootPathId() {
        return mRootPathId;
    }

    @Override
    public String getCurrentPathId() {
        return super.getCurrentPathId();
    }

    @Override
    protected void onToolbarNavigationClick() {
        interceptOrPopup();
    }

    @Override
    protected void onFileItemClick(INxlFile f, int pos) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(f, pos);
        }
    }

    @Override
    protected void initViewAndEvents() {
        super.initViewAndEvents();
        mToolbar.setTitle(getString(R.string.Library));
        mAdapter.setDisableLeftSwipeMenu(true);
        mAdapter.setDisableRightSwipeMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOnItemClickListener != null) {
            mOnItemClickListener = null;
        }
    }

    private void interceptOrPopup() {
        if (needInterceptBackPress()) {
            interceptBackPress();
        } else {
            if (mOnPopupFragmentListener != null) {
                mOnPopupFragmentListener.onPopup();
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(INxlFile f, int pos);
    }

    public interface OnPopupFragmentListener {
        void onPopup();
    }

}
