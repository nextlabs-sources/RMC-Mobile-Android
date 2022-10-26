package com.skydrm.rmc.ui.fragment.preview;

import android.content.Context;
import android.widget.RelativeLayout;

import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.Render.Audio;
import com.skydrm.rmc.engine.Render.FileRenderProxy;
import com.skydrm.rmc.engine.Render.IFileRender;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.ui.fragment.IPreview;

import java.io.File;

public class PreviewImpl implements IPreview {
    // file render proxy
    private FileRenderProxy mFileRenderProxy;
    private IFileRender mIFileRender;
    private File mWorkingFile;

    @Override
    public void buildRender(Context ctx, RelativeLayout mainLayout, final File file) {
        this.mWorkingFile = file;
        boolean bIsNxl = RenderHelper.isNxlFile(file.getPath());
        mFileRenderProxy = new FileRenderProxy(ctx, mainLayout, bIsNxl, file,
                file.getName(), Constant.VIEW_TYPE_PREVIEW);
        mFileRenderProxy.buildRender(new FileRenderProxy.IBuildRenderCallback() {
            @Override
            public void onBuildRenderFinish() {
                mIFileRender = mFileRenderProxy.getIFileRender();
                // render file
                if (mIFileRender != null) {
                    mIFileRender.fileRender();
                }
            }
        });
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        // Stop mp3 play when this activity is in background or destroy
        if (mWorkingFile != null
                && RenderHelper.judgeFileType(mWorkingFile) == FileRenderProxy.FileType.FILE_TYPE_AUDIO
                && mIFileRender instanceof Audio) {
            ((Audio) mIFileRender).onStopAudioPreview();
        }
    }
}
