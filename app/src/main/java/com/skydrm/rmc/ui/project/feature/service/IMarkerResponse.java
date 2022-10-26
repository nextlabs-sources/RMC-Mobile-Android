package com.skydrm.rmc.ui.project.feature.service;

import com.skydrm.rmc.ui.project.feature.service.core.MarkException;

public interface IMarkerResponse {
    void onMarkStart();

    void onMarkAllow();

    void onMarkFailed(MarkException e);

    void onMarkCanceled();
}
