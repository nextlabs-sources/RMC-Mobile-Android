/*
(C) 2018 SAP SE or an SAP affiliate company. All rights reserved.
*/
package com.sap.ve;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import com.sap.ve.DVLTypes.DVLRESULT;

import org.qtproject.qt5.android.QtNative;

import java.io.File;

import dalvik.system.DexClassLoader;

public class DVLCore {

    public static void loadLibraries(DisplayMetrics displayMetrics, Activity activity) {
        QtNative.setActivity(activity, null);
        final File optimizedDexOutputPath = activity.getDir("outdex", 0);
        DexClassLoader classLoader = new DexClassLoader("", optimizedDexOutputPath.getAbsolutePath(), null, activity.getClassLoader());
        QtNative.setClassLoader(classLoader);

        System.loadLibrary("Qt5Core");
        System.loadLibrary("Qt5Gui");
        System.loadLibrary("Qt5Widgets");
        System.loadLibrary("qtforandroid");
        System.loadLibrary("DVL");

        String libraryPath = activity.getApplicationInfo().nativeLibraryDir + "/";
        try {
            QtNative.startApplication(null, "", "DVL", libraryPath);
            QtNative.setDisplayMetrics(displayMetrics.widthPixels,
                    displayMetrics.heightPixels,
                    displayMetrics.widthPixels,
                    displayMetrics.heightPixels,
                    displayMetrics.xdpi,
                    displayMetrics.ydpi,
                    displayMetrics.scaledDensity,
                    displayMetrics.density);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long m_handle = 0;
    private Context m_context;
    private DVLRenderer m_renderer;
    private DVLLibrary m_library;
    private boolean m_sceneLoaded = false;

    public DVLCore(Context context) {
        m_context = context;
        m_handle = nativeInit();
        if (m_handle == 0)
            throw new java.lang.UnsupportedOperationException("can't instantiate core");

        m_library = new DVLLibrary(nativeGetLibrary(m_handle));
    }

    public DVLLibrary GetLibrary() {
        return m_library;
    }

    public DVLRESULT InitRenderer() {
        DVLRESULT res = DVLRESULT.fromInt(nativeInitRenderer(m_handle));
        if (res.Failed()) {
            android.util.Log.w("DVLCore", "InitRenderer failed: " + res.toString());
            return res;
        }

        long hRenderer = nativeGetRenderer(m_handle);
        if (hRenderer == 0)
            return DVLRESULT.FAIL;

        m_renderer = new DVLRenderer(hRenderer, m_context);
        return res;
    }

    public DVLRESULT DoneRenderer() {
        return DVLRESULT.fromInt(nativeDoneRenderer(m_handle));
    }

    public DVLRenderer GetRenderer() {
        return m_renderer;
    }

    public int GetMajorVersion() {
        return nativeGetMajorVersion(m_handle);
    }

    public int GetMinorVersion() {
        return nativeGetMinorVersion(m_handle);
    }

    public int GetMicroVersion() {
        return nativeGetMicroVersion(m_handle);
    }

    public int GetBuildNumber() {
        return nativeGetBuildNumber(m_handle);
    }

    public DVLRESULT LoadScene(String filename, String password, DVLScene scene) {
        m_sceneLoaded = false;
        DVLClient.startLoading();
        DVLRESULT res = DVLRESULT.fromInt(nativeLoadScene(m_handle, filename, password, scene));
        DVLClient.endLoading();
        if (res.Succeeded())
            m_sceneLoaded = true;
        return res;
    }

    // system stuff

    public void dispose() {
        if (m_renderer != null) {
            if (m_sceneLoaded)
                m_renderer.dispose();
            m_renderer = null;
        }

        m_library = null;

        if (m_handle != 0) {
            nativeDoneRenderer(m_handle);
            //nativeDone(m_handle);
            m_handle = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            dispose();
        } finally {
            super.finalize();
        }
    }

    // native stuff

    static private native long nativeInit();

    static private native void nativeDone(long handle);

    static private native long nativeGetLibrary(long handle);

    static private native int nativeInitRenderer(long handle);

    static private native int nativeDoneRenderer(long handle);

    static private native long nativeGetRenderer(long handle);

    static private native int nativeGetMajorVersion(long handle);

    static private native int nativeGetMinorVersion(long handle);

    static private native int nativeGetMicroVersion(long handle);

    static private native int nativeGetBuildNumber(long handle);

    static private native int nativeLoadScene(long handle, String filename, String password, Object scene);
}

