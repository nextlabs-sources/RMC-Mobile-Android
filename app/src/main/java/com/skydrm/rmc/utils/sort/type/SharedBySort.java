package com.skydrm.rmc.utils.sort.type;

import com.skydrm.rmc.utils.sort.IBaseSort;
import com.skydrm.rmc.utils.sort.ISharedWithMeSortable;
import com.skydrm.rmc.utils.sort.SortedItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SharedBySort implements IBaseSort<SortedItem> {
    private List<SortedItem> mSortItem;
    private boolean mReverse;

    public SharedBySort(List<SortedItem> sortItem, boolean reverse) {
        this.mSortItem = sortItem;
        this.mReverse = reverse;
    }

    @Override
    public void onSortFile(List<SortedItem> target) {
        Collections.sort(target, new SharedByComparator(mReverse));
    }

    @Override
    public List<SortedItem> doSort() {
        onSortFile(mSortItem);
        return mSortItem;
    }

    class SharedByComparator implements Comparator<SortedItem> {
        boolean mReverse;

        SharedByComparator(boolean reverse) {
            this.mReverse = reverse;
        }

        @Override
        public int compare(SortedItem l, SortedItem r) {
            ISharedWithMeSortable sl = (ISharedWithMeSortable) l.mTarget;
            ISharedWithMeSortable sr = (ISharedWithMeSortable) r.mTarget;
            return mReverse ? sr.getSortableShareBy().compareToIgnoreCase(sl.getSortableShareBy()) :
                    sl.getSortableShareBy().compareToIgnoreCase(sr.getSortableShareBy());
        }
    }
}
