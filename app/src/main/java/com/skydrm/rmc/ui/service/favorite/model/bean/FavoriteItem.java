package com.skydrm.rmc.ui.service.favorite.model.bean;

import com.skydrm.rmc.ui.service.favorite.model.IFavoriteFile;

/**
 * Created by hhu on 8/23/2017.
 */

public class FavoriteItem {
    public String title;
    public IFavoriteFile file;

    public FavoriteItem(String title, IFavoriteFile f) {
        this.title = title;
        this.file = f;
    }
}
