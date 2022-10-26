package com.skydrm.rmc.ui.project.feature.files;

import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.base.NxlBaseFilePresenter;
import com.skydrm.rmc.utils.sort.SortType;

public class ProjectFilePresenter extends NxlBaseFilePresenter {

    public ProjectFilePresenter(IProject p, IFileContact.IView view, SortType sortType) {
        this(view, (IDataService) p);
        mSortType = sortType;
    }

    private ProjectFilePresenter(IFileContact.IView view, IDataService... service) {
        super(view, service);
    }

}
