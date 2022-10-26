package com.skydrm.rmc.ui.myspace.myvault.data;

import com.skydrm.rmc.ui.myspace.myvault.ICommand;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileResult;
import com.skydrm.sdk.rms.rest.myVault.MyVaultFileListResult;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;
import com.skydrm.sdk.utils.ParseJsonUtils;

import java.util.List;

/**
 * Created by hhu on 4/28/2018.
 */

public class Result {
    public static class ListResult implements ICommand.IResult {
        public MyVaultFileListResult result;

        public ListResult(MyVaultFileListResult result) {
            this.result = result;
        }
    }

    public static class UploadResult implements ICommand.IResult {
        public MyVaultUploadFileResult result;

        public UploadResult(MyVaultUploadFileResult result) {
            this.result = result;
        }
    }

    public static class MetaDataResult implements ICommand.IResult {
        public MyVaultMetaDataResult result;

        public MetaDataResult(MyVaultMetaDataResult result) {
            this.result = result;
        }
    }

    public static class DeleteResult implements ICommand.IResult {

    }

    public static class FileListCacheResult<T> implements ICommand.IResult {
        List<T> result;

        public FileListCacheResult(List<T> result) {
            this.result = result;
        }
    }

    public static class FavFetchResult implements ICommand.IResult {
        public List<ParseJsonUtils.AllRepoFavoListBean> result;

        public FavFetchResult(List<ParseJsonUtils.AllRepoFavoListBean> result) {
            this.result = result;
        }
    }
}
