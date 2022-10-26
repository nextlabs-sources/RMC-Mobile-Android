package com.skydrm.rmc.presenter.impl;

import android.text.TextUtils;

import com.skydrm.rmc.domain.ILocalFile;
import com.skydrm.rmc.domain.LocalFileItem;
import com.skydrm.rmc.domain.impl.LocalFileImpl;
import com.skydrm.rmc.presenter.IFileListsPresenter;
import com.skydrm.rmc.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hhu on 6/9/2017.
 */

@Deprecated
public class FileListsPresenter implements IFileListsPresenter {

    @Override
    public List<LocalFileItem> loadFile(File root) {
        List<ILocalFile> mLocalFiles = new ArrayList<>();
        File[] files = root.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (!file.isHidden()) {
                    LocalFileImpl localFileImpl = new LocalFileImpl();
                    localFileImpl.setFolder(file.isDirectory());
                    localFileImpl.setFile(file);
                    mLocalFiles.add(localFileImpl);
                }
            }
            return sort(translateFileItems(mLocalFiles));
        }
        return null;
    }

    public List<LocalFileItem> sort(List<LocalFileItem> items) {
        List<LocalFileItem> alNormalItems = new ArrayList<>();
        List<LocalFileItem> alSpecialItems = new ArrayList<>();
        for (LocalFileItem localFileItem : items) {
            File file = localFileItem.getLocalFile().getFile();
            String letter = FileUtils.getLetter(file.getName());
            if (TextUtils.equals(letter, "#")) {
                alSpecialItems.add(localFileItem);
            } else {
                alNormalItems.add(localFileItem);
            }
        }
        items.clear();
        sortTempItems(alSpecialItems);
        items.addAll(alSpecialItems);
        sortTempItems(alNormalItems);
        items.addAll(alNormalItems);
        List<LocalFileItem> sortResults = new ArrayList<>();
        for (LocalFileItem item : items) {
            ILocalFile localFile = item.getLocalFile();
            sortResults.add(new LocalFileItem(localFile,
                    FileUtils.getLetter(localFile.getFile().getName())));
        }
        return sortResults;
    }

    private void sortTempItems(List<LocalFileItem> localFileItems) {
        if (localFileItems == null || localFileItems.size() == 0) {
            return;
        }
        Collections.sort(localFileItems, new Comparator<LocalFileItem>() {
            @Override
            public int compare(LocalFileItem lhs, LocalFileItem rhs) {
                if (lhs == null || rhs == null) {
                    return 0;
                }
                ILocalFile lLocalFile = lhs.getLocalFile();
                ILocalFile rLocalFile = rhs.getLocalFile();
                if (lLocalFile == null || rLocalFile == null) {
                    return 0;
                }
                File lFile = lLocalFile.getFile();
                File rFile = rLocalFile.getFile();
                if (TextUtils.isEmpty(lFile.getName()) || TextUtils.isEmpty(rFile.getName())) {
                    return 0;
                }
                return lFile.getName().compareToIgnoreCase(rFile.getName());
            }
        });
    }

    private List<LocalFileItem> translateFileItems(List<ILocalFile> localFiles) {
        List<ILocalFile> temps = new ArrayList<>(localFiles);
        List<LocalFileItem> items = new ArrayList<>();
        for (ILocalFile localFile : temps) {
            items.add(new LocalFileItem(localFile, FileUtils.getLetter(localFile.getFile().getName())));
        }
        return items;
    }
}
