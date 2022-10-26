package com.skydrm.rmc.ui.myspace.sharewithme.presenter;

import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.base.NxlBaseFilePresenter;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.utils.sort.SortType;

public class SharedWithMePresenter extends NxlBaseFilePresenter {

    public SharedWithMePresenter(IFileContact.IView view, SortType sortType) {
        this(view, (IDataService) RepoFactory.getRepo(RepoType.TYPE_SHARED_WITH_ME));
        mSortType = sortType;
    }

    private SharedWithMePresenter(IFileContact.IView view, IDataService... service) {
        super(view, service);
    }

}
