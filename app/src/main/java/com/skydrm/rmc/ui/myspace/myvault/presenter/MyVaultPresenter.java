package com.skydrm.rmc.ui.myspace.myvault.presenter;

import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.base.NxlBaseFilePresenter;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;
import com.skydrm.rmc.utils.sort.SortType;

public class MyVaultPresenter extends NxlBaseFilePresenter {

    public MyVaultPresenter(IFileContact.IView view, SortType sortType) {
        this(view, (IDataService) RepoFactory.getRepo(RepoType.TYPE_MYVAULT));
        mSortType = sortType;
    }

    private MyVaultPresenter(IFileContact.IView view, IDataService... service) {
        super(view, service);
    }

}
