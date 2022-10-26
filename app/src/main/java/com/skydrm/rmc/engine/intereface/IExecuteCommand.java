package com.skydrm.rmc.engine.intereface;

import android.content.Context;
import android.support.annotation.Nullable;

import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;

import java.io.File;
import java.util.List;

/**
 * Created by aning on 5/12/2017.
 *  The interface used to execute file operate action, such as protect,share, command protect, command share
 *  command Add and so on.
 */

public interface IExecuteCommand {
    /**
     * @param context context
     * @param workingFile file that operate currently
     * @param rights the rights that user select
     * @param obligations flag the overlay
     * @param expiry expiry date.
     * @param clickFileItem file node item
     * @param callback protect callback
     */
    @Deprecated
    void protect(Context context,
                 File workingFile,
                 Rights rights,
                 Obligations obligations,
                 Expiry expiry,
                 final INxFile clickFileItem,
                 final IProtectComplete callback);

    /**
     * Share local file command
     * @param context context
     * @param workingFile file that operate currently
     * @param emailList the email address that will share
     * @param rights the rights that user select, is null if is nxl file
     * @param obligations flag the overlay, is null if is nxl file
     * @param comment the comment for sharing
     * @param expiry  expiry date
     * @param callback share callback
     */
    void shareLocalFile(Context context,
               File workingFile ,
               List<String>emailList,
               @Nullable Rights rights,
               @Nullable Obligations obligations,
               @Nullable String comment,
               @Nullable Expiry expiry,
               IShareComplete callback);

    /**
     * Share repository file command, when share a nxl file or do re-share, don't need to pass watermark and expiry.
     * @param context
     * @param fileName file name.
     * @param repositoryId repository id
     * @param filePathId  file path id
     * @param filePath  file path
     * @param permissions  the rights value
     * @param emails  the email address will be shared
     * @param comment  the comment about share(optional)
     * @param watermark watermark value, can is null when share nxl file
     * @param expiry  expiry date, can is null when share nxl file
     * @param callback  share call back.
     */
    void shareRepoFile(Context context,
                       final String fileName,
                       final String repositoryId,
                       final String filePathId,
                       final String filePath,
                       final int permissions,
                       final List<String> emails,
                       @Nullable final String comment,
                       @Nullable final String watermark,
                       @Nullable final Expiry expiry,
                       IShareComplete callback);

    /**
     * @param context context
     * @param boundService the selected repo that will add file into
     * @param parentFolder the parent folder of the added file
     * @param addFile added file
     * @param  callback add file callback
     */
    void AddFile(Context context,
                 BoundService boundService,
                 INxFile parentFolder,
                 File addFile,
                 IAddComplete callback);
}
