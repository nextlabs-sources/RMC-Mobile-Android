package com.skydrm.sap;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;

import com.sap.ve.DVLCore;
import com.sap.ve.DVLRenderer;
import com.sap.ve.DVLScene;
import com.sap.ve.DVLTypes;
import com.sap.ve.SDVLImage;
import com.sap.ve.SDVLMatrix;
import com.sap.ve.SDVLPartsListInfo;
import com.sap.ve.SDVLProcedure;
import com.sap.ve.SDVLProceduresInfo;
import com.sap.ve.SDVLStep;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class CustomRenderer implements GLSurfaceView.Renderer {
    private Context m_context;
    private DVLCore m_core;
    private DVLRenderer m_renderer;
    private DVLScene m_scene;
    private long m_curTime = 0L;

    private GestureHandler m_gestures;
    private String m_filePath;

    private IRetrieveThumbnail mIRetrieveThumbnail;
    private boolean onInitializeFailed;

    CustomRenderer(Context context, DVLCore core, GestureHandler gestures, String filePath) {
        this.m_context = context;
        this.m_core = core;
        this.m_gestures = gestures;
        this.m_filePath = filePath;
    }

    void activateStep(long id) {
        m_scene.ActivateStep(id, false, false);
    }

    void retrieveThumbnail(long id, SDVLImage image) {
        m_scene.RetrieveThumbnail(id, image);
    }

    void setIRetrieveThumbnail(IRetrieveThumbnail retrieveThumbnail) {
        this.mIRetrieveThumbnail = retrieveThumbnail;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        DVLTypes.DVLRESULT res = m_core.InitRenderer();
        if (res.Failed()) {
            onInitializeFailed = true;
            if (mIRetrieveThumbnail != null) {
                mIRetrieveThumbnail.onInitializeError(res);
            }
            return;
        }

        m_renderer = m_core.GetRenderer();
        m_renderer.SetBackgroundColor(50.0f / 255.0f, 50.0f / 255.0f, 50.0f / 255.0f, 1.0f, 1.0f, 1.0f);
        m_renderer.SetOption(DVLTypes.DVLRENDEROPTION.SHOW_SHADOW, true);

        m_scene = new DVLScene(0, m_context);
        res = m_core.LoadScene("file://" + m_filePath, null, m_scene);
        if (res.Failed()) {
            onInitializeFailed = true;
            if (mIRetrieveThumbnail != null) {
                mIRetrieveThumbnail.onInitializeError(res);
            }
            return;
        }

        m_renderer.AttachScene(m_scene);

        SDVLProceduresInfo proceduresInfo = new SDVLProceduresInfo();
        m_scene.RetrieveProcedures(proceduresInfo);

        if (proceduresInfo.portfolios.size() > 0) {
            m_scene.ActivateStep(proceduresInfo.portfolios.get(0).steps.get(0).id, true, true);
        } else {
            return;
        }

        if (mIRetrieveThumbnail != null) {
            mIRetrieveThumbnail.onDisplay(proceduresInfo);
        }

        SDVLPartsListInfo partsListInfo = new SDVLPartsListInfo();
        m_scene.BuildPartsList(DVLTypes.DVLPARTSLIST.RECOMMENDED_uMaxParts, DVLTypes.DVLPARTSLIST.RECOMMENDED_uMaxNodesInSinglePart, DVLTypes.DVLPARTSLIST.RECOMMENDED_uMaxPartNameLength,
                DVLTypes.DVLPARTSLISTTYPE.ALL, DVLTypes.DVLPARTSLISTSORT.NAME_ASCENDING, DVLTypes.DVLID_INVALID, "", partsListInfo);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        m_renderer.SetDimensions(w, h);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //If initialize file failed, just return.
        if (onInitializeFailed) {
            return;
        }
        m_gestures.update(m_renderer);

        SDVLMatrix matView = new SDVLMatrix();
        SDVLMatrix matProj = new SDVLMatrix();

        m_renderer.GetCameraMatrices(matView, matProj);
        m_renderer.RenderFrame();
    }
}
