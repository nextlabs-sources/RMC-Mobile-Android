package com.skydrm.rmc.ui.project.feature.summary;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.ui.common.NxlFileItem;

import java.util.List;

interface ISummaryContact {
    interface IView {
        void onInitialize(boolean active);

        void showLoadingIndicator(boolean show);

        void showProjectSpace(long quota, long usage);

        void displayMember(List<IMember> members);

        void showEmpty(boolean active);

        void update(List<NxlFileItem> recent);

        void showErrorView(Exception e);

        void restoreView();
    }

    interface IPresenter extends IDestroyable {
        void initialize();

        void listMemberAndFile();

        void refresh();

        void delete(INxlFile f);
    }
}
