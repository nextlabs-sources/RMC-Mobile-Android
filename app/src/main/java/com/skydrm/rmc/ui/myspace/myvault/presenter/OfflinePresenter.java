package com.skydrm.rmc.ui.myspace.myvault.presenter;

import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.base.NxlBaseFilePresenter;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.utils.sort.SortType;

public class OfflinePresenter extends NxlBaseFilePresenter {

    public OfflinePresenter(IFileContact.IView view, SortType sortType) {
        this(view, (IDataService) RepoFactory.getRepo(RepoType.TYPE_MYVAULT),
                (IDataService) RepoFactory.getRepo(RepoType.TYPE_SHARED_WITH_ME));
        mSortType = sortType;
    }

    private OfflinePresenter(IFileContact.IView view, IDataService... service) {
        super(view, service);
    }

}
