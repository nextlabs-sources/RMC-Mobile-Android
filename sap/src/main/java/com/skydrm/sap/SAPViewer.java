package com.skydrm.sap;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.sap.ve.DVLCore;
import com.sap.ve.SDVLImage;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class SAPViewer extends GLSurfaceView {
    private DVLCore m_DVLCore;
    private CustomRenderer mCustomRenderer;
    private GestureHandler m_gestures;

    public SAPViewer(Context context) {
        super(context);
        init();
    }

    public SAPViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        m_DVLCore = new DVLCore(getContext().getApplicationContext());
        m_gestures = new GestureHandler();
        setEGLContextFactory(new ContextFactory());
        setEGLConfigChooser(new ConfigChooser());

        setOnTouchListener(m_gestures);
    }

    public DVLCore getDVLCore() {
        return m_DVLCore;
    }

    public void display(String filePath, IRetrieveThumbnail retrieveThumbnail) {
        mCustomRenderer = new CustomRenderer(getContext(), m_DVLCore, m_gestures, filePath);
        mCustomRenderer.setIRetrieveThumbnail(retrieveThumbnail);
        setRenderer(mCustomRenderer);
    }

    public void activateStep(long id) {
        if (mCustomRenderer != null) {
            mCustomRenderer.activateStep(id);
        }
    }

    public void retrieveThumbnail(long id, SDVLImage image) {
        if (mCustomRenderer != null) {
            mCustomRenderer.retrieveThumbnail(id, image);
        }
    }

    private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser {
        private static int EGL_OPENGL_ES2_BIT = 4;
        private static int[] s_configAttribs2 =
                {
                        EGL10.EGL_RED_SIZE, 5,//minimum is 5 bits per component  (8 bits preferred)
                        EGL10.EGL_GREEN_SIZE, 5,//minimum is 5 bits per component (8 bits preferred)
                        EGL10.EGL_BLUE_SIZE, 5,//minimum is 5 bits per component (8 bits preferred)
                        EGL10.EGL_DEPTH_SIZE, 16,//minimum 16 bits for Z Buffer (24 bits preferred)
                        EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                        EGL10.EGL_NONE
                };

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

            int numConfigs = num_config[0];
            if (numConfigs <= 0) {
                throw new IllegalArgumentException("No matching OpenGL configurations");
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs, num_config);
            return chooseConfig(egl, display, configs);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            EGLConfig best_config = null;
            int best_z = 0, best_s = 64, best_r = 0, best_g = 0, best_b = 0;

            for (EGLConfig config : configs) {
                int z = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
                int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
                //Z buffer bitness has precedence over RGB bitness (because 16 bit z buffer is not good enough for DVL)
                if ((z < best_z) || (s > best_s) || (r < best_r) || (b < best_b) || (g < best_g))
                    continue;

                best_z = z;
                best_s = s;
                best_r = r;
                best_g = g;
                best_b = b;
                best_config = config;
            }

            return best_config;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }

            return defaultValue;
        }

        private int[] mValue = new int[1];
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (m_DVLCore != null) {
                m_DVLCore.dispose();
            }
        } finally {
            m_DVLCore = null;
            super.finalize();
        }
    }
}

