package com.skydrm.rmc.reposystem.sort;

import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NxFileBase;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Sorts {

    static public INxFile sort(INxFile parent, SortType type) {
        if (parent == null) {
            return null;
        }
        List<INxFile> target = parent.getChildren();
        target = sort(target, type);
        ((NxFileBase) parent).setChildren(target);
        return parent;
    }

    static public List<INxFile> sort(List<INxFile> target, SortType type) {
        if (target == null || target.isEmpty()) {
            return target;
        }
        switch (type) {
            case TIME_DESCEND:
                Collections.sort(target, new LatestComparator());
                break;
            case NAME_ASCEND:
                Collections.sort(target, new NameAscendingComparator());
                break;
            case DRIVER_TYPE:
                Collections.sort(target, new NameAscendingComparator());
                break;
        }
        return target;
    }


    static public class NameAscendingComparator implements Comparator<INxFile> {
        @Override
        public int compare(INxFile l, INxFile r) {
            String lName = l.isSite() ? l.getName().substring(1) : l.getName();
            String rName = r.isSite() ? r.getName().substring(1) : r.getName();
            return lName.compareToIgnoreCase(rName);
        }
    }

    static public class LatestComparator implements Comparator<INxFile> {
        @Override
        public int compare(INxFile l, INxFile r) {
            //should compare the fully time, so the pass parameter isBottomItem should be set to true which is fully time value.
            return Long.compare(r.getLastModifiedTimeLong(), l.getLastModifiedTimeLong());
        }
    }
}
