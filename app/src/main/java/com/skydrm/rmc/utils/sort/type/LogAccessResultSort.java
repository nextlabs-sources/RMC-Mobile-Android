package com.skydrm.rmc.utils.sort.type;

import com.skydrm.rmc.utils.sort.IBaseSort;
import com.skydrm.rmc.utils.sort.ILogSortable;
import com.skydrm.rmc.utils.sort.SortedItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LogAccessResultSort implements IBaseSort<SortedItem> {
    private List<SortedItem> mSortItem;
    private boolean mReverse;

    public LogAccessResultSort(List<SortedItem> sortItem, boolean reverse) {
        this.mSortItem = sortItem;
        this.mReverse = reverse;
    }

    @Override
    public void onSortFile(List<SortedItem> target) {
        Collections.sort(target, new AccessResultComparator(mReverse));
    }

    @Override
    public List<SortedItem> doSort() {
        onSortFile(mSortItem);
        return mSortItem;
    }

    class AccessResultComparator implements Comparator<SortedItem> {
        private boolean mReverse;

        AccessResultComparator(boolean reverse) {
            this.mReverse = reverse;
        }

        @Override
        public int compare(SortedItem l, SortedItem r) {
            ILogSortable ll = (ILogSortable) l.mTarget;
            ILogSortable lr = (ILogSortable) r.mTarget;

            return mReverse ? lr.getSortableResult().compareToIgnoreCase(ll.getSortableResult()) :
                    ll.getSortableResult().compareToIgnoreCase(lr.getSortableResult());
        }
    }
}
