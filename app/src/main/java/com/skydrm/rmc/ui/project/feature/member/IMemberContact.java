package com.skydrm.rmc.ui.project.feature.member;

import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.List;

public interface IMemberContact {
    interface IView {
        void onInitialize(boolean active);

        void showLoadingIndicator(boolean active);

        void update(List<MemberItem> data);

        void showEmptyView(boolean active);

        void showErrorView(Exception e);

        void restoreView();

        boolean isActive();
    }

    interface IPresenter extends IDestroyable {
        void initialize();

        void refresh();

        void sort(SortType type);
    }
}
