package com.skydrm.rmc.ui.repository.contact;

import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.base.NxlBaseFilePresenter;
import com.skydrm.rmc.ui.project.feature.files.IFileContact;

public class LibraryFilePresenter extends NxlBaseFilePresenter {

    public LibraryFilePresenter(IDataService service, IFileContact.IView view, String root) {
        super(view, service);
        mRoot = root;
    }

}
