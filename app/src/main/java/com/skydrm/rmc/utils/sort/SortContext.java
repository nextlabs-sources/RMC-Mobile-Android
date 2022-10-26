package com.skydrm.rmc.utils.sort;

import android.support.annotation.NonNull;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.library.LibraryFile;
import com.skydrm.rmc.datalayer.repo.library.LibraryNode;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.Member;
import com.skydrm.rmc.datalayer.repo.project.Project;
import com.skydrm.rmc.datalayer.repo.project.ProjectFile;
import com.skydrm.rmc.datalayer.repo.project.ProjectNode;
import com.skydrm.rmc.datalayer.repo.project.SharedWithProjectFile;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeFile;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceFile;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceNode;
import com.skydrm.rmc.domain.ILocalFile;
import com.skydrm.rmc.domain.LocalFileItem;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.domain.impl.LocalFileImpl;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NxFileBase;
import com.skydrm.rmc.ui.common.NxlFileItem;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.IVaultFileLog;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.VaultFileLogImpl;
import com.skydrm.rmc.ui.project.feature.member.MemberItem;
import com.skydrm.rmc.ui.service.contact.impl.Contact;
import com.skydrm.rmc.ui.service.contact.impl.ContactItem;
import com.skydrm.rmc.ui.service.favorite.model.IFavoriteFile;
import com.skydrm.rmc.ui.service.favorite.model.bean.FavoriteItem;
import com.skydrm.rmc.utils.sort.type.DriveSort;
import com.skydrm.rmc.utils.sort.type.LogAccessResultSort;
import com.skydrm.rmc.utils.sort.type.LogOperationSort;
import com.skydrm.rmc.utils.sort.type.NameSort;
import com.skydrm.rmc.utils.sort.type.SharedBySort;
import com.skydrm.rmc.utils.sort.type.SizeSort;
import com.skydrm.rmc.utils.sort.type.TimeSort;

import java.util.ArrayList;
import java.util.List;

public class SortContext {
    private static IBaseSort<SortedItem> mBaseSort;

    public static List<IProject> sortProject(List<IProject> targets, SortType sortType) {
        List<IProject> retVal = new ArrayList<>();
        if (targets == null || targets.size() == 0) {
            return retVal;
        }
        List<SortedItem> sortedItems = sort(adapt2ProjectSortableItem(targets), sortType);
        if (sortedItems == null || sortedItems.size() == 0) {
            return retVal;
        }
        for (SortedItem item : sortedItems) {
            if (item == null) {
                continue;
            }
            IBaseSortable target = item.mTarget;
            if (target == null) {
                continue;
            }
            if (target instanceof Project) {
                retVal.add((Project) target);
            }
        }
        return retVal;
    }

    public static List<NXFileItem> sortRepoFile(List<INxFile> targets, SortType sortType) {
        List<NXFileItem> ret = new ArrayList<>();
        if (targets == null || targets.size() == 0) {
            return ret;
        }
        List<SortedItem> sortedItems = sort(adapt2RepoSortableItem(targets), sortType);
        for (SortedItem i : sortedItems) {
            if (i == null) {
                continue;
            }
            ret.add(new NXFileItem((NxFileBase) i.mTarget, i.mTitle));
        }
        return ret;
    }

    public static List<NxlFileItem> sortNxlItem(List<INxlFile> targets, SortType type) {
        List<NxlFileItem> ret = new ArrayList<>();
        List<SortedItem> sortedItems = sort(adapt2NxlSortableItem(targets), type);
        for (SortedItem i : sortedItems) {
            if (i == null) {
                continue;
            }
            IBaseSortable sortable = i.mTarget;
            if (sortable instanceof MyVaultFile) {
                ret.add(new NxlFileItem((MyVaultFile) sortable, i.mTitle));
            } else if (sortable instanceof SharedWithMeFile) {
                ret.add(new NxlFileItem((SharedWithMeFile) sortable, i.mTitle));
            } else if (sortable instanceof ProjectFile) {
                ret.add(new NxlFileItem((ProjectFile) sortable, i.mTitle));
            } else if (sortable instanceof ProjectNode) {
                ret.add(new NxlFileItem((ProjectNode) sortable, i.mTitle));
            } else if (sortable instanceof WorkSpaceFile) {
                ret.add(new NxlFileItem((WorkSpaceFile) sortable, i.mTitle));
            } else if (sortable instanceof WorkSpaceNode) {
                ret.add(new NxlFileItem((WorkSpaceNode) sortable, i.mTitle));
            } else if (sortable instanceof SharedWithProjectFile) {
                ret.add(new NxlFileItem((SharedWithProjectFile) sortable, i.mTitle));
            } else if (sortable instanceof LibraryFile) {
                ret.add(new NxlFileItem((LibraryFile) sortable, i.mTitle));
            } else if (sortable instanceof LibraryNode) {
                ret.add(new NxlFileItem((LibraryNode) sortable, i.mTitle));
            }
        }
        return ret;
    }

    public static List<FavoriteItem> sortFavoriteFile2(List<IFavoriteFile> targets, SortType sortType) {
        List<FavoriteItem> ret = new ArrayList<>();
        List<SortedItem> sortedItems = sort(adapt2FavSortableItem2(targets), sortType);
        for (SortedItem i : sortedItems) {
            IBaseSortable sortable = i.mTarget;
            if (sortable instanceof NxFileBase) {
                ret.add(new FavoriteItem(i.mTitle, (NxFileBase) sortable));
            } else if (sortable instanceof MyVaultFile) {
                ret.add(new FavoriteItem(i.mTitle, (MyVaultFile) sortable));
            }
        }
        return ret;
    }

    public static List<IVaultFileLog> sortLog(List<IVaultFileLog> targets, SortType sortType) {
        List<IVaultFileLog> ret = new ArrayList<>();
        List<SortedItem> sortedItems = sort(adapt2LogSortableItem(targets), sortType);
        for (SortedItem i : sortedItems) {
            ret.add((VaultFileLogImpl) i.mTarget);
        }
        return ret;
    }

    public static List<MemberItem> sortMember(List<IMember> targets, SortType sortType) {
        List<MemberItem> ret = new ArrayList<>();
        List<SortedItem> sortedItems = sort(adapt2MemberSortableItem(targets), sortType);
        for (SortedItem i : sortedItems) {
            ret.add(new MemberItem(i.mTitle, (Member) i.mTarget));
        }
        return ret;
    }

    public static List<ContactItem> sortContacts(List<Contact> targets, SortType sortType) {
        List<ContactItem> ret = new ArrayList<>();
        if (targets == null || targets.size() == 0) {
            return ret;
        }
        List<SortedItem> sortedItems = sort(adapt2ContactSortableItem(targets), sortType);
        for (SortedItem i : sortedItems) {
            ret.add(new ContactItem(i.mTitle, (Contact) i.mTarget));
        }
        return ret;
    }

    public static List<LocalFileItem> sortLocalLibraryFile(List<ILocalFile> targets, SortType sortType) {
        List<LocalFileItem> ret = new ArrayList<>();
        if (targets == null || targets.size() == 0) {
            return ret;
        }
        List<SortedItem> sortedItems = sort(adapt2LocalFileSortableItem(targets), sortType);
        for (SortedItem i : sortedItems) {
            if (i.mTarget instanceof LocalFileImpl) {
                ret.add(new LocalFileItem((LocalFileImpl) i.mTarget, i.mTitle));
            }
        }
        return ret;
    }

    public static List<SortedItem> sort(List<IBaseSortable> sortableList, SortType type) {
        switch (type) {
            case NAME_ASCEND:
                mBaseSort = new NameSort(adapt2NameItem(sortableList), false);
                break;
            case NAME_DESCEND:
                mBaseSort = new NameSort(adapt2NameItem(sortableList), true);
                break;
            case SIZE_ASCEND:
                mBaseSort = new SizeSort(adapt2SizeItem(sortableList), false);
                break;
            case SIZE_DESCEND:
                mBaseSort = new SizeSort(adapt2SizeItem(sortableList), true);
                break;
            case TIME_ASCEND:
                mBaseSort = new TimeSort(adapt2TimeItem(sortableList), false);
                break;
            case TIME_DESCEND:
                mBaseSort = new TimeSort(adapt2TimeItem(sortableList), true);
                break;
            case SHARED_BY_ASCEND:
                mBaseSort = new SharedBySort(adapt2SharedByItem(sortableList), false);
                break;
            case SHARED_BY_DESCEND:
                mBaseSort = new SharedBySort(adapt2SharedByItem(sortableList), true);
                break;
            case DRIVER_TYPE:
                mBaseSort = new DriveSort(adapt2DriveItem(sortableList));
                break;
            case LOG_SORT_OPERATION_ASCEND:
                mBaseSort = new LogOperationSort(adapt2LogItem(sortableList), false);
                break;
            case LOG_SORT_OPERATION_DESCEND:
                mBaseSort = new LogOperationSort(adapt2LogItem(sortableList), true);
                break;
            case LOG_SORT_RESULT_ASCEND:
                mBaseSort = new LogAccessResultSort(adapt2LogItem(sortableList), false);
                break;
            case LOG_SORT_RESULT_DESCEND:
                mBaseSort = new LogAccessResultSort(adapt2LogItem(sortableList), true);
                break;
        }
        return mBaseSort.doSort();
    }

    private static List<IBaseSortable> adapt2ProjectSortableItem(List<IProject> pl) {
        List<IBaseSortable> retVal = new ArrayList<>();
        if (pl == null || pl.size() == 0) {
            return retVal;
        }
        for (IProject p : pl) {
            if (p == null) {
                continue;
            }
            if (!(p instanceof Project)) {
                continue;
            }
            retVal.add((Project) p);
        }
        return retVal;
    }

    private static List<IBaseSortable> adapt2RepoSortableItem(List<INxFile> nl) {
        List<IBaseSortable> ret = new ArrayList<>();
        if (nl == null || nl.size() == 0) {
            return ret;
        }
        for (INxFile f : nl) {
            if (f == null) {
                continue;
            }
            if (!(f instanceof NxFileBase)) {
                continue;
            }
            ret.add((NxFileBase) f);
        }
        return ret;
    }

    private static List<IBaseSortable> adapt2ProjectFileSortableItem2(List<INxlFile> fl) {
        List<IBaseSortable> ret = new ArrayList<>();
        for (INxlFile f : fl) {
            if (f.isFolder()) {
                ret.add((ProjectNode) f);
            } else {
                ret.add((ProjectFile) f);
            }
        }
        return ret;
    }

    private static List<IBaseSortable> adapt2ShareSortableItem2(List<INxlFile> fl) {
        List<IBaseSortable> ret = new ArrayList<>();
        for (INxlFile f : fl) {
            ret.add((SharedWithMeFile) f);
        }
        return ret;
    }

    private static List<IBaseSortable> adapt2NxlSortableItem(List<INxlFile> fl) {
        List<IBaseSortable> ret = new ArrayList<>();
        for (INxlFile f : fl) {
            if (f == null) {
                continue;
            }
            if (f instanceof MyVaultFile) {
                ret.add((MyVaultFile) f);
            } else if (f instanceof SharedWithMeFile) {
                ret.add((SharedWithMeFile) f);
            } else if (f instanceof ProjectFile) {
                ret.add((ProjectFile) f);
            } else if (f instanceof ProjectNode) {
                ret.add((ProjectNode) f);
            } else if (f instanceof WorkSpaceFile) {
                ret.add((WorkSpaceFile) f);
            } else if (f instanceof WorkSpaceNode) {
                ret.add((WorkSpaceNode) f);
            } else if (f instanceof SharedWithProjectFile) {
                ret.add((SharedWithProjectFile) f);
            } else if (f instanceof LibraryFile) {
                ret.add((LibraryFile) f);
            } else if (f instanceof LibraryNode) {
                ret.add((LibraryNode) f);
            }
        }
        return ret;
    }

    private static List<IBaseSortable> adapt2FavSortableItem2(List<IFavoriteFile> fl) {
        List<IBaseSortable> ret = new ArrayList<>();
        if (fl == null || fl.size() == 0) {
            return ret;
        }
        for (IFavoriteFile f : fl) {
            if (f instanceof MyVaultFile) {
                ret.add((MyVaultFile) f);
            } else if (f instanceof NxFileBase) {
                ret.add((NxFileBase) f);
            }
        }
        return ret;
    }

    private static List<IBaseSortable> adapt2LogSortableItem(List<IVaultFileLog> flLog) {
        List<IBaseSortable> ret = new ArrayList<>();
        for (IVaultFileLog f : flLog) {
            ret.add((VaultFileLogImpl) f);
        }
        return ret;
    }

    private static List<IBaseSortable> adapt2MemberSortableItem(List<IMember> ml) {
        List<IBaseSortable> ret = new ArrayList<>();
        if (ml == null || ml.size() == 0) {
            return ret;
        }
        for (IMember m : ml) {
            ret.add((Member) m);
        }
        return ret;
    }

    private static List<IBaseSortable> adapt2ContactSortableItem(List<Contact> cl) {
        List<IBaseSortable> ret = new ArrayList<>();
        if (cl == null || cl.size() == 0) {
            return ret;
        }
        ret.addAll(cl);
        return ret;
    }

    private static List<IBaseSortable> adapt2LocalFileSortableItem(List<ILocalFile> fl) {
        List<IBaseSortable> ret = new ArrayList<>();
        if (fl == null || fl.size() == 0) {
            return ret;
        }
        for (ILocalFile f : fl) {
            if (f instanceof LocalFileImpl) {
                ret.add((LocalFileImpl) f);
            }
        }
        return ret;
    }

    private static List<SortedItem> adapt2NameItem(@NonNull List<IBaseSortable> sl) {
        List<SortedItem> ret = new ArrayList<>();
        for (IBaseSortable s : sl) {
            ret.add(SortedItem.adapt2NameItem(s));
        }
        return ret;
    }

    private static List<SortedItem> adapt2SizeItem(@NonNull List<IBaseSortable> sl) {
        List<SortedItem> ret = new ArrayList<>();
        for (IBaseSortable s : sl) {
            ret.add(SortedItem.adapt2SizeItem(s));
        }
        return ret;
    }

    private static List<SortedItem> adapt2TimeItem(@NonNull List<IBaseSortable> sl) {
        List<SortedItem> ret = new ArrayList<>();
        for (IBaseSortable s : sl) {
            ret.add(SortedItem.adapt2TimeItem(s));
        }
        return ret;
    }

    private static List<SortedItem> adapt2SharedByItem(@NonNull List<IBaseSortable> sl) {
        List<SortedItem> ret = new ArrayList<>();
        for (IBaseSortable s : sl) {
            if (s instanceof ISharedWithMeSortable) {
                ret.add(SortedItem.adapt2SharedByItem(s));
            }
        }
        return ret;
    }

    private static List<SortedItem> adapt2DriveItem(@NonNull List<IBaseSortable> sl) {
        List<SortedItem> ret = new ArrayList<>();
        for (IBaseSortable s : sl) {
            if (s instanceof IRepoFileSortable) {
                ret.add(SortedItem.adapt2DriveItem(s));
            }
        }
        return ret;
    }

    private static List<SortedItem> adapt2LogItem(List<IBaseSortable> sl) {
        List<SortedItem> ret = new ArrayList<>();
        for (IBaseSortable s : sl) {
            if (s instanceof ILogSortable) {
                ret.add(SortedItem.adapt2SizeItem(s));
            }
        }
        return ret;
    }
}
