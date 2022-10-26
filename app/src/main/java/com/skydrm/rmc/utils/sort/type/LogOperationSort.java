package com.skydrm.rmc.utils.sort.type;

import com.skydrm.rmc.utils.sort.IBaseSort;
import com.skydrm.rmc.utils.sort.ILogSortable;
import com.skydrm.rmc.utils.sort.SortedItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LogOperationSort implements IBaseSort<SortedItem> {
    private List<SortedItem> mSortedItem;
    private boolean mReverse;

    public LogOperationSort(List<SortedItem> sortedItem, boolean reverse) {
        this.mSortedItem = sortedItem;
        this.mReverse = reverse;
    }

    @Override
    public void onSortFile(List<SortedItem> target) {
        Collections.sort(target, new LogOperationComparator(mReverse));
    }

    @Override
    public List<SortedItem> doSort() {
        onSortFile(mSortedItem);
        return mSortedItem;
    }

    class LogOperationComparator implements Comparator<SortedItem> {
        private boolean mReverse;

        LogOperationComparator(boolean reverse) {
            this.mReverse = reverse;
        }

        @Override
        public int compare(SortedItem l, SortedItem r) {
            ILogSortable ll = (ILogSortable) l.mTarget;
            ILogSortable lr = (ILogSortable) r.mTarget;

            return mReverse ? lr.getSortableOperation().compareToIgnoreCase(ll.getSortableOperation()) :
                    ll.getSortableOperation().compareToIgnoreCase(lr.getSortableOperation());
        }
    }
}
