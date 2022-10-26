package com.skydrm.rmc.engine.intereface;

import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.types.NXDocument;

/**
 * Created by aning on 5/14/2017.
 */

public interface IAddComplete {
    void onAddFileComplete(boolean taskStatus, final NXDocument uploadedDoc,FileUploadException e);
}
