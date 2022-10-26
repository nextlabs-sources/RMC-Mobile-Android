package com.skydrm.pdf.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import com.tom_roush.pdfbox.cos.COSDictionary;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Utils {
    private static final String TYPE_E3D_3D = "3D";
    private static final String TYPE_E3D_3D_NODE = "3DNode";
    private static final String TYPE_E3D_3D_RENDER_MODE = "3DRenderMode";
    private static final String TYPE_E3D_3D_VIEW = "3DView";

    private static final String SUBTYPE_E3D_U3D = "U3D";
    private static final String SUBTYPE_E3D_CAD = "CAD";
    private static final String SUBTYPE_E3D_3D = "3D";

    /**
     * Returns the in memory size of the given {@link Bitmap} in bytes.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static int getBitmapByteSize(@NonNull Bitmap bitmap) {
        // The return value of getAllocationByteCount silently changes for recycled bitmaps from the
        // internal buffer size to row bytes * height. To avoid random inconsistencies in caches, we
        // instead assert here.
        if (bitmap.isRecycled()) {
            throw new IllegalStateException(
                    "Cannot obtain size for recycled Bitmap: "
                            + bitmap
                            + "["
                            + bitmap.getWidth()
                            + "x"
                            + bitmap.getHeight()
                            + "] "
                            + bitmap.getConfig());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Workaround for KitKat initial release NPE in Bitmap, fixed in MR1. See issue #148.
            try {
                return bitmap.getAllocationByteCount();
            } catch (
                    @SuppressWarnings("PMD.AvoidCatchingNPE")
                            NullPointerException e) {
                // Do nothing.
            }
        }
        return bitmap.getHeight() * bitmap.getRowBytes();
    }

    private static int getBytesPerPixel(@Nullable Bitmap.Config config) {
        // A bitmap by decoding a GIF has null "config" in certain environments.
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }

        int bytesPerPixel;
        switch (config) {
            case ALPHA_8:
                bytesPerPixel = 1;
                break;
            case RGB_565:
            case ARGB_4444:
                bytesPerPixel = 2;
                break;
            case RGBA_F16:
                bytesPerPixel = 8;
                break;
            case ARGB_8888:
            default:
                bytesPerPixel = 4;
                break;
        }
        return bytesPerPixel;
    }

    public static int getScreenWidth(Context ctx) {
        int w = 0;
        if (ctx instanceof Activity) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            w = displaymetrics.widthPixels;
        }
        return w;
    }

    public static int getScreenHeight(Context ctx) {
        int w = 0;
        if (ctx instanceof Activity) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            w = displaymetrics.heightPixels;
        }
        return w;
    }

    public static boolean isAnAsset(String path) {
        return !path.startsWith("/");
    }

    public static File fileFromAsset(Context context, String assetName) throws IOException {
        File outFile = new File(context.getCacheDir(), assetName + "-pdfview.pdf");
        if (assetName.contains("/")) {
            outFile.getParentFile().mkdirs();
        }
        copy(context.getAssets().open(assetName), outFile);
        return outFile;
    }

    public static void copy(InputStream inputStream, File output) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(output);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }
    }

    public static boolean is3DPdf(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File f = new File(path);
        if (!f.exists() || f.isDirectory()) {
            return false;
        }
        try (PDDocument doc = PDDocument.load(f)) {
            if (doc == null) {
                return false;
            }
            if (doc.isEncrypted()) {
                return false;
            }

            for (PDPage page : doc.getPages()) {
                if (page == null) {
                    continue;
                }
                List<PDAnnotation> annotations = page.getAnnotations();
                if (contains3DElements(annotations)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean contains3DElements(List<PDAnnotation> annotations) {
        if (annotations == null || annotations.size() == 0) {
            return false;
        }
        for (PDAnnotation annotation : annotations) {
            if (annotation == null) {
                continue;
            }

            COSDictionary cosObject = annotation.getCOSObject();
            COSName cosName = cosObject.getCOSName(COSName.SUBTYPE);

            if (cosName == null || cosName.isEmpty()) {
                continue;
            }

            String subtype = cosName.getName();
            if (subtype == null || subtype.isEmpty()) {
                continue;
            }

            if (SUBTYPE_E3D_U3D.equalsIgnoreCase(subtype)
                    || SUBTYPE_E3D_CAD.equalsIgnoreCase(subtype)
                    || SUBTYPE_E3D_3D.equalsIgnoreCase(subtype)) {
                return true;
            }
        }
        return false;
    }
}
