package com.skydrm.rmc.presenter;

import com.skydrm.rmc.domain.LocalFileItem;

import java.io.File;
import java.util.List;

/**
 * Created by hhu on 6/9/2017.
 */

public interface IFileListsPresenter {
    List<LocalFileItem> loadFile(File root);
}
