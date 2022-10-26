package com.skydrm.rmc.presenter;

import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.domain.NXFileItem;

import java.util.List;

/**
 * Created by hhu on 5/11/2017.
 */
@Deprecated
public interface ILibraryDataPresenter {
    List<NXFileItem> loadRoots(BoundService service);

    List<NXFileItem> loadFilterRoots(BoundService service, String filter);

    List<NXFileItem> getChildren(INxFile subSite);

    List<NXFileItem> getFilterChildren(INxFile subSite, String filter);

    List<NXFileItem> getParentFile();

    List<NXFileItem> getFilterParentFile(String filter);

    String getDisplayPath();

    //    String getDestFolder();
    INxFile getDestFolder();

    boolean isRoot();
}
