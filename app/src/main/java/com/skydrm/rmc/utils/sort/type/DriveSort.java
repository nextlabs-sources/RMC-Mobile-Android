package com.skydrm.rmc.utils.sort.type;

import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.utils.sort.IBaseSort;
import com.skydrm.rmc.utils.sort.IRepoFileSortable;
import com.skydrm.rmc.utils.sort.SortedItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DriveSort implements IBaseSort<SortedItem> {
    private List<SortedItem> mSortItem;

    public DriveSort(List<SortedItem> sortItem) {
        this.mSortItem = sortItem;
    }

    @Override
    public void onSortFile(List<SortedItem> target) {
        Collections.sort(target, new DriveComparator());
    }

    @Override
    public List<SortedItem> doSort() {
        onSortFile(mSortItem);
        return mSortItem;
    }

    class DriveComparator implements Comparator<SortedItem> {

        @Override
        public int compare(SortedItem l, SortedItem r) {
            IRepoFileSortable rfl = (IRepoFileSortable) l.mTarget;
            IRepoFileSortable rfr = (IRepoFileSortable) r.mTarget;
            //Always keep myDrive top display.
            if (rfl.getBoundService().type == BoundService.ServiceType.MYDRIVE
                    && rfr.getBoundService().type != BoundService.ServiceType.MYDRIVE) {
                return -1;
            }
            if (rfl.getBoundService().type != BoundService.ServiceType.MYDRIVE
                    && rfr.getBoundService().type == BoundService.ServiceType.MYDRIVE) {
                return 1;
            }
            //If repo type is the same,the sorted by fileName.
            if (rfl.getBoundService().type == rfr.getBoundService().type) {
                //Folder display first.
                if (rfl.isFolder() && !rfr.isFolder()) {
                    return -1;
                }
                if (!rfl.isFolder() && rfr.isFolder()) {
                    return 1;
                }
                //Folder display by dic order.
                //Non-folder display by dic order.
                return rfl.getSortableName().compareToIgnoreCase(rfr.getSortableName());
            }
            return Integer.compare(rfl.getBoundService().type.value(), rfr.getBoundService().type.value());
        }
    }
}
