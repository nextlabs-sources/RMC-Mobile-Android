package com.skydrm.rmc.utils.sort.type;

import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.utils.sort.IBaseSort;
import com.skydrm.rmc.utils.sort.SortedItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimeSort implements IBaseSort<SortedItem> {
    private List<SortedItem> mSortItems;
    private boolean mReverse;

    public TimeSort(List<SortedItem> sortItems, boolean reverse) {
        this.mSortItems = sortItems;
        this.mReverse = reverse;
    }

    @Override
    public void onSortFile(List<SortedItem> target) {
        Collections.sort(target, new TimeComparator(mReverse));
    }

    @Override
    public List<SortedItem> doSort() {
        List<SortedItem> ret = new ArrayList<>();

        List<SortedItem> normal = new ArrayList<>();
        List<SortedItem> members = new ArrayList<>();
        List<SortedItem> pending = new ArrayList<>();
        for (SortedItem i : mSortItems) {
            switch (i.mTitle) {
                case IMember.TITLE_ACTIVE:
                    members.add(i);
                    break;
                case IMember.TITLE_PENDING:
                    pending.add(i);
                    break;
                default:
                    normal.add(i);
                    break;
            }
        }

        if (members.size() != 0) {
            onSortFile(members);
            ret.addAll(members);
        }
        if (pending.size() != 0) {
            onSortFile(pending);
            ret.addAll(pending);
        }
        if (normal.size() != 0) {
            onSortFile(normal);
            ret.addAll(normal);
        }

        return ret;
    }

    class TimeComparator implements Comparator<SortedItem> {
        private boolean mReverse;

        TimeComparator(boolean reverse) {
            this.mReverse = reverse;
        }

        @Override
        public int compare(SortedItem l, SortedItem r) {
            if (l.mTitle.equalsIgnoreCase(r.mTitle)) {
                //One section folder display first.
                if (l.mTarget.isFolder() && !r.mTarget.isFolder()) {
                    return -1;
                }
                if (!l.mTarget.isFolder() && r.mTarget.isFolder()) {
                    return 1;
                }
            }

            return mReverse ? Long.compare(r.mTarget.getSortableTime(), l.mTarget.getSortableTime()) :
                    Long.compare(l.mTarget.getSortableTime(), r.mTarget.getSortableTime());
        }
    }
}
