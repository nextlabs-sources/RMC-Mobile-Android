package com.skydrm.rmc.ui.service.search;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IPendingMember;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.NxlBaseFilePresenter;
import com.skydrm.rmc.ui.common.NxlFileItem;
import com.skydrm.rmc.ui.service.favorite.iteractor.FavoritePresenter;
import com.skydrm.rmc.ui.service.favorite.model.bean.FavoriteItem;
import com.skydrm.rmc.ui.fragment.AllPresenter;
import com.skydrm.rmc.ui.service.log.LogLoadManager;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.IVaultFileLog;
import com.skydrm.rmc.ui.project.feature.member.MemberItem;
import com.skydrm.rmc.ui.project.feature.member.MemberPresenter;
import com.skydrm.rmc.ui.project.service.ProjectPresenter;

import java.util.ArrayList;
import java.util.List;

public class SearchPresenter implements ISearchContact.IPresenter {
    private ISearchContact.IView mView;
    private String mAction;
    private List<NxlFileItem> mNxlData = new ArrayList<>();
    private List<NxlFileItem> mNxlSearchResult = new ArrayList<>();

    private List<FavoriteItem> mFavData = new ArrayList<>();
    private List<FavoriteItem> mFavSearchResult = new ArrayList<>();

    private List<NXFileItem> mReoSystemData = new ArrayList<>();
    private List<NXFileItem> mReoSystemSearchResult = new ArrayList<>();

    private List<MemberItem> mMemberData = new ArrayList<>();
    private List<MemberItem> mMemberSearchResult = new ArrayList<>();

    private List<IVaultFileLog> mLogData = new ArrayList<>();
    private List<IVaultFileLog> mLogSearchData = new ArrayList<>();

    private List<IProject> mProjectData = new ArrayList<>();
    private List<IProject> mProjectSearchData = new ArrayList<>();

    private boolean showEmptyView;
    private boolean showNoSearchResultView;

    SearchPresenter(ISearchContact.IView v) {
        this.mView = v;
    }

    @Override
    public void initialize(String action) {
        mAction = action;
        initializeByAction();
    }

    private void initializeByAction() {
        switch (mAction) {
            case Constant.ACTION_SEARCH_MYVAULT:
            case Constant.ACTION_SEARCH_SHARED_BY_ME:
            case Constant.ACTION_SEARCH_OFFLINE:
            case Constant.ACTION_SEARCH_SHARED_WITH_ME:
            case Constant.ACTION_SEARCH_PROJECT_FILES:
            case Constant.ACTION_SEARCH_PROJECT_OFFLINE_FILES:
                mNxlData.clear();
                mNxlData.addAll(NxlBaseFilePresenter.getSearchItem());
                break;
            case Constant.ACTION_SEARCH_FAVORITE:
                mFavData.clear();
                mFavData.addAll(FavoritePresenter.getSearchItem());
                break;
            case Constant.ACTION_SEARCH_REPO_SYSTEM:
                mReoSystemData.clear();
                mReoSystemData.addAll(AllPresenter.getSearchItems());
                break;
            case Constant.ACTION_SEARCH_PROJECT_MEMBERS:
                mMemberData.clear();
                mMemberData.addAll(MemberPresenter.getSearchItem());
                break;
            case Constant.ACTION_SEARCH_ACTIVITY_LOG:
                mLogData.clear();
                mLogData.addAll(LogLoadManager.getInstance().getSearchItem());
                break;
            case Constant.ACTION_SEARCH_ALL_PROJECTS:
                mProjectData.clear();
                mProjectData.addAll(ProjectPresenter.getSearchData());
                break;
            case Constant.ACTION_SEARCH_WORKSPACE_FILES:
            case Constant.ACTION_SEARCH_WORKSPACE_OFFLINE_FILES:
                mNxlData.clear();
                mNxlData.addAll(NxlBaseFilePresenter.getSearchItem());
                break;
            default:
                break;
        }
    }

    @Override
    public void searchByName(String filter) {
        switch (mAction) {
            case Constant.ACTION_SEARCH_FAVORITE:
                searchByFavName(filter);
                break;
            case Constant.ACTION_SEARCH_REPO_SYSTEM:
                searchByRepoFileName(filter);
                break;
            case Constant.ACTION_SEARCH_PROJECT_MEMBERS:
                searchByMemberName(filter);
                break;
            case Constant.ACTION_SEARCH_ACTIVITY_LOG:
                searchByLog(filter);
                break;
            case Constant.ACTION_SEARCH_ALL_PROJECTS:
                searchByProjectName(filter);
                break;
            default:
                searchByNxlName(filter);
                break;
        }
    }

    @Override
    public void deleteProjectFile(INxlFile file) {
        NxlFileItem removed = null;
        for (NxlFileItem item : mNxlSearchResult) {
            if (item == null) {
                continue;
            }
            if (item.getNxlFile().getPathDisplay().equals(file.getPathDisplay())) {
                removed = item;
                break;
            }
        }
        if (removed != null) {
            mNxlData.remove(removed);
            mNxlSearchResult.remove(removed);
        }
        if (mNxlSearchResult == null || mNxlSearchResult.size() == 0) {
            if (mView != null) {
                mView.showEmptyView(true);
                showEmptyView = true;
            }
            return;
        }
        dismissEmptyView();
        if (mView != null) {
            mView.updateNxlItem(mNxlSearchResult);
        }
    }

    @Override
    public void deleteMyVaultFile(INxlFile file, String vaultType) {
        if (Constant.TAB_MY_VAULT_ACTIVE_SHARES.equals(vaultType)) {
            NxlFileItem removed = null;
            for (NxlFileItem item : mNxlSearchResult) {
                if (item == null) {
                    continue;
                }
                if (item.getNxlFile().getPathDisplay().equals(file.getPathDisplay())) {
                    removed = item;
                    break;
                }
            }
            if (removed != null) {
                mNxlData.remove(removed);
                mNxlSearchResult.remove(removed);
            }
            if (mNxlSearchResult == null || mNxlSearchResult.size() == 0) {
                if (mView != null) {
                    mView.showEmptyView(true);
                    showEmptyView = true;
                }
                return;
            }
        } else {
            for (NxlFileItem item : mNxlSearchResult) {
                if (item == null) {
                    continue;
                }
                if (item.getNxlFile().getPathDisplay().equals(file.getPathDisplay())) {
                    INxlFile nxlFile = item.getNxlFile();
                    if (nxlFile instanceof MyVaultFile) {
                        MyVaultFile vaultFile = (MyVaultFile) nxlFile;
                        vaultFile.setDeleted(true);
                        vaultFile.setRevoked(true);
                    }
                    break;
                }
            }
        }

        dismissEmptyView();
        if (mView != null) {
            mView.updateNxlItem(mNxlSearchResult);
        }
    }

    @Override
    public void deleteMyDriveFile(INxFile file) {
        NXFileItem removed = null;
        for (NXFileItem item : mReoSystemData) {
            if (item == null) {
                continue;
            }
            if (item.getNXFile().getLocalPath().equals(file.getLocalPath())) {
                removed = item;
                break;
            }
        }
        if (removed != null) {
            mReoSystemSearchResult.remove(removed);
            mReoSystemData.remove(removed);
        }
        if (mReoSystemSearchResult == null || mReoSystemSearchResult.isEmpty()) {
            if (mView != null) {
                mView.showEmptyView(true);
                showEmptyView = true;
            }
            return;
        }
        dismissEmptyView();
        if (mView != null) {
            mView.updateRepoItem(mReoSystemSearchResult);
        }
    }

    @Override
    public void updateProjectMemberItem(IMember target) {
        MemberItem toBeRemoved = null;
        for (MemberItem i : mMemberSearchResult) {
            if (i == null) {
                continue;
            }
            IMember member = i.member;
            if (member == null) {
                continue;
            }
            if (member instanceof IPendingMember) {
                IPendingMember pm1 = (IPendingMember) member;
                IPendingMember pm2 = (IPendingMember) target;
                String inviteeEmail1 = pm1.getInviteeEmail();
                String inviteeEmail2 = pm2.getInviteeEmail();
                if (inviteeEmail1 != null && pm1.getInviteeEmail().equals(inviteeEmail2)) {
                    toBeRemoved = i;
                    break;
                }

                String displayName1 = member.getDisplayName();
                if (displayName1 != null && displayName1.equals(target.getDisplayName())) {
                    toBeRemoved = i;
                    break;
                }
            }
        }
        if (toBeRemoved != null) {
            mMemberSearchResult.remove(toBeRemoved);
        }

        if (mMemberSearchResult.size() == 0) {
            if (mView != null) {
                mView.showNoSearchResultView(true);
                showNoSearchResultView = true;
            }
            return;
        }
        dismissEmptyView();
        if (mView != null) {
            mView.updateMemberItem(mMemberSearchResult);
        }
    }

    @Override
    public void onReleaseResource() {
        if (mView != null) {
            mView = null;
        }
    }

    private void searchByRepoFileName(String filter) {
        if (mReoSystemData == null || mReoSystemData.size() == 0) {
            if (mView != null) {
                mView.showEmptyView(true);
                showEmptyView = true;
            }
            return;
        }
        mReoSystemSearchResult.clear();
        for (NXFileItem i : mReoSystemData) {
            if (i == null) {
                continue;
            }
            String name = i.getNXFile().getName();
            if (name.toLowerCase().contains(filter.toLowerCase())) {
                mReoSystemSearchResult.add(i);
            }
        }
        if (mReoSystemSearchResult.size() == 0) {
            if (mView != null) {
                mView.showNoSearchResultView(true);
                showNoSearchResultView = true;
            }
            return;
        }
        dismissEmptyView();
        if (mView != null) {
            mView.updateRepoItem(mReoSystemSearchResult);
        }
    }

    private void searchByFavName(String filter) {
        if (mFavData == null || mFavData.size() == 0) {
            if (mView != null) {
                mView.showEmptyView(true);
                showEmptyView = true;
            }
            return;
        }
        mFavSearchResult.clear();
        for (FavoriteItem i : mFavData) {
            if (i == null) {
                continue;
            }
            String name = i.file.getName();
            if (name.toLowerCase().contains(filter.toLowerCase())) {
                mFavSearchResult.add(i);
            }
        }
        if (mFavSearchResult.size() == 0) {
            if (mView != null) {
                mView.showNoSearchResultView(true);
                showNoSearchResultView = true;
            }
            return;
        }
        dismissEmptyView();
        if (mView != null) {
            mView.updateFavItem(mFavSearchResult);
        }
    }

    private void searchByMemberName(String filter) {
        if (mMemberData == null || mMemberData.size() == 0) {
            if (mView != null) {
                mView.showEmptyView(true);
                showEmptyView = true;
            }
            return;
        }
        mMemberSearchResult.clear();
        for (MemberItem i : mMemberData) {
            if (i == null) {
                continue;
            }
            IMember member = i.member;
            if (member == null) {
                continue;
            }

            String name = member.getDisplayName();
            if (name != null) {
                if (name.toLowerCase().contains(filter.toLowerCase())) {
                    mMemberSearchResult.add(i);
                }
            }

            if (member instanceof IPendingMember) {
                IPendingMember pm = (IPendingMember) member;
                String inviteeEmail = pm.getInviteeEmail();
                if (inviteeEmail != null) {
                    if (inviteeEmail.toLowerCase().contains(filter.toLowerCase())) {
                        mMemberSearchResult.add(i);
                    }
                }
            }

        }
        if (mMemberSearchResult.size() == 0) {
            if (mView != null) {
                mView.showNoSearchResultView(true);
                showNoSearchResultView = true;
            }
            return;
        }
        dismissEmptyView();
        if (mView != null) {
            mView.updateMemberItem(mMemberSearchResult);
        }
    }

    private void searchByLog(String filter) {
        if (mLogData == null || mLogData.size() == 0) {
            if (mView != null) {
                mView.showEmptyView(true);
                showEmptyView = true;
            }
            return;
        }
        mLogSearchData.clear();
        for (IVaultFileLog i : mLogData) {
            if (i == null) {
                continue;
            }
            String email = i.getEmail();
            String result = i.getAccessResult();
            String operation = i.getOperation();
            if (email.toLowerCase().contains(filter.toLowerCase()) ||
                    result.toLowerCase().contains(filter.toLowerCase()) ||
                    operation.toLowerCase().contains(filter.toLowerCase())) {
                mLogSearchData.add(i);
            }
        }
        if (mLogSearchData.size() == 0) {
            if (mView != null) {
                mView.showNoSearchResultView(true);
                showNoSearchResultView = true;
            }
            return;
        }
        dismissEmptyView();
        if (mView != null) {
            mView.updateLogItem(mLogSearchData);
        }
    }

    private void searchByProjectName(String filter) {
        if (mProjectData == null || mProjectData.size() == 0) {
            if (mView != null) {
                mView.showEmptyView(true);
                showEmptyView = true;
            }
            return;
        }
        mProjectSearchData.clear();
        for (IProject p : mProjectData) {
            if (p == null) {
                continue;
            }
            if (p.isPendingInvite()) {
                continue;
            }
            String email = p.getName();
            if (email.toLowerCase().contains(filter.toLowerCase())) {
                mProjectSearchData.add(p);
            }
        }
        if (mProjectSearchData.size() == 0) {
            if (mView != null) {
                mView.showNoSearchResultView(true);
                showNoSearchResultView = true;
            }
            return;
        }
        dismissEmptyView();
        if (mView != null) {
            mView.updateProjectItem(mProjectSearchData);
        }
    }

    private void searchByNxlName(String filter) {
        if (mNxlData == null || mNxlData.size() == 0) {
            if (mView != null) {
                mView.showEmptyView(true);
                showEmptyView = true;
            }
            return;
        }
        mNxlSearchResult.clear();
        for (NxlFileItem i : mNxlData) {
            if (i == null) {
                continue;
            }
            String name = i.getNxlFile().getName();
            if (name.toLowerCase().contains(filter.toLowerCase())) {
                mNxlSearchResult.add(i);
            }
        }
        if (mNxlSearchResult.size() == 0) {
            if (mView != null) {
                mView.showNoSearchResultView(true);
                showNoSearchResultView = true;
            }
            return;
        }
        dismissEmptyView();
        if (mView != null) {
            mView.updateNxlItem(mNxlSearchResult);
        }
    }

    private void dismissEmptyView() {
        if (showEmptyView) {
            showEmptyView = false;
            if (mView != null) {
                mView.showEmptyView(false);
            }
        }
        if (showNoSearchResultView) {
            showNoSearchResultView = false;
            if (mView != null) {
                mView.showNoSearchResultView(false);
            }
        }
    }
}
