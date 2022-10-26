package com.skydrm.rmc.utils.sort.type;

import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.sort.IBaseSort;
import com.skydrm.rmc.utils.sort.SortedItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NameSort implements IBaseSort<SortedItem> {
    private List<SortedItem> mSortItems;
    private boolean mReverse;

    public NameSort(List<SortedItem> sortItems, boolean reverse) {
        this.mSortItems = sortItems;
        this.mReverse = reverse;
    }

    @Override
    public void onSortFile(List<SortedItem> target) {
        Collections.sort(target, new NameComparator(mReverse));
    }

    @Override
    public List<SortedItem> doSort() {
        List<SortedItem> ret = new ArrayList<>();

        List<SortedItem> numItems = new ArrayList<>();
        List<SortedItem> others = new ArrayList<>();

        List<SortedItem> members = new ArrayList<>();
        List<SortedItem> pending = new ArrayList<>();
        for (SortedItem i : mSortItems) {
            switch (i.mTitle) {
                case "#":
                    numItems.add(i);
                    break;
                case IMember.TITLE_ACTIVE:
                    members.add(i);
                    break;
                case IMember.TITLE_PENDING:
                    pending.add(i);
                    break;
                default:
                    others.add(i);
                    break;
            }
        }

        ret.clear();
        if (members.size() != 0) {
            List<SortedItem> memberRet = new ArrayList<>();
            List<SortedItem> memberNumItems = new ArrayList<>();
            List<SortedItem> memberOthers = new ArrayList<>();
            for (SortedItem i : members) {
                if (FileUtils.getLetter(i.mTarget.getSortableName()).equals("#")) {
                    memberNumItems.add(i);
                } else {
                    memberOthers.add(i);
                }
            }
            memberRet.clear();
            onSortFile(memberNumItems);
            memberRet.addAll(memberNumItems);
            onSortFile(memberOthers);
            memberRet.addAll(memberOthers);

            ret.addAll(memberRet);
        }
        if (pending.size() != 0) {
            List<SortedItem> pendingRet = new ArrayList<>();
            List<SortedItem> pendingNumItems = new ArrayList<>();
            List<SortedItem> pendingOthers = new ArrayList<>();

            for (SortedItem i : pending) {
                if (FileUtils.getLetter(i.mTarget.getSortableName()).equals("#")) {
                    pendingNumItems.add(i);
                } else {
                    pendingOthers.add(i);
                }
            }

            pendingRet.clear();
            onSortFile(pendingNumItems);
            pendingRet.addAll(pendingNumItems);
            onSortFile(pendingOthers);
            pendingRet.addAll(pendingOthers);

            ret.addAll(pendingRet);
        }

        onSortFile(numItems);
        ret.addAll(numItems);
        onSortFile(others);
        ret.addAll(others);

        return ret;
    }

    class NameComparator implements Comparator<SortedItem> {
        private boolean mReverse;

        NameComparator(boolean reverse) {
            this.mReverse = reverse;
        }

        @Override
        public int compare(SortedItem l, SortedItem r) {
            //Folder display first.
            if (l.mTitle.equalsIgnoreCase(r.mTitle)) {
                //In the same section, folder display first.
                if (l.mTarget.isFolder() && !r.mTarget.isFolder()) {
                    return -1;
                }
                if (!l.mTarget.isFolder() && r.mTarget.isFolder()) {
                    return 1;
                }
            }

            return mReverse ? r.mTarget.getSortableName().compareToIgnoreCase(l.mTarget.getSortableName()) :
                    l.mTarget.getSortableName().compareToIgnoreCase(r.mTarget.getSortableName());
        }
    }
}
