package com.skydrm.rmc.domain;

import java.io.File;

/**
 * Created by hhu on 2/24/2017.
 */

public interface ILocalFile {
    File getFile();

    boolean isChecked();

    void setSelected(boolean selected);

    boolean isVisible();

    boolean isFolder();

    boolean cached();

    void setCached(boolean cache);

    int getColor();
}
