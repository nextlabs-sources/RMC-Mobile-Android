package com.skydrm.rmc.utils.sort.type;

import com.skydrm.rmc.utils.sort.IBaseSort;
import com.skydrm.rmc.utils.sort.SortedItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SizeSort implements IBaseSort<SortedItem> {
    private List<SortedItem> mSortItems;
    private boolean mReverse;

    public SizeSort(List<SortedItem> sortedItems, boolean reverse) {
        this.mSortItems = sortedItems;
        this.mReverse = reverse;
    }

    @Override
    public void onSortFile(List<SortedItem> target) {
        Collections.sort(target, new SizeComparator(mReverse));
    }

    @Override
    public List<SortedItem> doSort() {
        onSortFile(mSortItems);
        return mSortItems;
    }

    class SizeComparator implements Comparator<SortedItem> {
        private boolean mReverse;

        SizeComparator(boolean reverse) {
            this.mReverse = reverse;
        }

        @Override
        public int compare(SortedItem l, SortedItem r) {
            //Folders display first
            if (l.mTarget.isFolder() && !r.mTarget.isFolder()) {
                return -1;
            }
            if (!l.mTarget.isFolder() && r.mTarget.isFolder()) {
                return 1;
            }
            //If size is the same then order by name.
            return mReverse ? (r.mTarget.getSortableSize() == l.mTarget.getSortableSize() ?
                    r.mTarget.getSortableName().compareToIgnoreCase(l.mTarget.getSortableName()) :
                    Long.compare(r.mTarget.getSortableSize(), l.mTarget.getSortableSize()))
                    : (r.mTarget.getSortableSize() == l.mTarget.getSortableSize() ?
                    l.mTarget.getSortableName().compareToIgnoreCase(r.mTarget.getSortableName()) :
                    Long.compare(l.mTarget.getSortableSize(), r.mTarget.getSortableSize()));
        }
    }
}
