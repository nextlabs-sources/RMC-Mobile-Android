package com.skydrm.rmc.ui.base;

import com.skydrm.rmc.ui.common.IDestroyable;

import java.util.List;

public interface IBaseContract {
    interface IBaseView<T> {
        void initialize(boolean active);

        void update(List<T> fls);

        void setEmptyView(boolean active);

        void setLoadingIndicator(boolean active);

        void notifyItemDelete(int pos);

        boolean isActive();

        void showErrorView(Exception e);
    }

    interface IBasePresenter<ItemType, SortType> extends IDestroyable {
        void initialize(int type);

        void sort(SortType sortType);

        void list(int type, String pathId);

        void refresh(int type, String pathId);

        void delete(ItemType f, int pos);
    }
}
