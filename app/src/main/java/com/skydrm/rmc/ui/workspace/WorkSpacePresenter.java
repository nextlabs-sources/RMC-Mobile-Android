package com.skydrm.rmc.ui.workspace;

import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.ui.base.NxlBaseFilePresenter;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.utils.sort.SortType;

class WorkSpacePresenter extends NxlBaseFilePresenter {

    WorkSpacePresenter(IFileContact.IView view, SortType sortType) {
        this(view, (IDataService) RepoFactory.getRepo(RepoType.TYPE_WORKSPACE));
        mSortType = sortType;
    }

    private WorkSpacePresenter(IFileContact.IView view, IDataService... service) {
        super(view, service);
    }

}
