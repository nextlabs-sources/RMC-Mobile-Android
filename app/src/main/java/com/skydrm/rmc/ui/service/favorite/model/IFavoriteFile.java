package com.skydrm.rmc.ui.service.favorite.model;

public interface IFavoriteFile {
    String getName();

    long getSize();

    long getLastModifiedTime();

    String getDisplayPath();

    boolean isFavorite();

    boolean isOffline();

    int getOperationStatus();
}
