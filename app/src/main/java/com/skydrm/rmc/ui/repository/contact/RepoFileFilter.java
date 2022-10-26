package com.skydrm.rmc.ui.repository.contact;

import com.skydrm.rmc.ui.repository.IFileFilter;

public class RepoFileFilter implements IFileFilter {

    @Override
    public boolean accept(String name) {
        return name != null && !name.isEmpty();
    }

}
