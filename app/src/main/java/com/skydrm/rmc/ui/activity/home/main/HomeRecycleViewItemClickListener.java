package com.skydrm.rmc.ui.activity.home.main;

import com.skydrm.rmc.reposystem.types.BoundService;


public interface HomeRecycleViewItemClickListener {
    void onSelectMySpace();

    void onSelectMyDrive();

    void onSelectMyVault();

    void onSelectWorkSpace();

    void onSelectRepository(BoundService boundService);

    void onSelectConnectRepository();
}
