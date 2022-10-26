package com.skydrm.rmc.utils.sort;

import com.skydrm.rmc.utils.FileUtils;

public class SortedItem {
    public String mTitle;
    public IBaseSortable mTarget;

    private SortedItem(String title, IBaseSortable target) {
        this.mTitle = title;
        this.mTarget = target;
    }

    static SortedItem adapt2NameItem(IBaseSortable sortable) {
        if (sortable instanceof IMemberSortable) {
            return new SortedItem(((IMemberSortable) sortable).getMemberType(), sortable);
        }
        return new SortedItem(FileUtils.getLetter(sortable.getSortableName()), sortable);
    }

    static SortedItem adapt2SizeItem(IBaseSortable sortable) {
        return new SortedItem("", sortable);
    }

    static SortedItem adapt2TimeItem(IBaseSortable sortable) {
        if (sortable instanceof IMemberSortable) {
            return new SortedItem(((IMemberSortable) sortable).getMemberType(), sortable);
        }
        return new SortedItem(FileUtils.convertTime(sortable, false), sortable);
    }

    static SortedItem adapt2SharedByItem(IBaseSortable sortable) {
        return new SortedItem(FileUtils.getLetter(((ISharedWithMeSortable) sortable).getSortableShareBy()), sortable);
    }

    static SortedItem adapt2DriveItem(IBaseSortable sortable) {
        return new SortedItem(((IRepoFileSortable) sortable).getBoundService().alias, sortable);
    }
}
