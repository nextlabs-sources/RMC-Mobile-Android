package com.skydrm.rmc.ui.service.offline.filter;

import android.text.TextUtils;

import com.skydrm.rmc.ui.service.offline.architecture.IOfflineFilter;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineStatus;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OfflineFileFilter implements IOfflineFilter {
    private static final String[] FILE_TYPE_TEXTS = new String[]{
            "txt", "cpp", "java", "c",
            "h", "html", "htm",
            "xml", "log", "py", "md",
            "m", "swift", "err", "sql",
            "vb", "json", "csv"
    };
    private static final String[] FILE_TYPE_IMAGES = new String[]{
            "jpg", "jpeg", "png", "bmp", "webp", "gif"
    };
    private static final String[] FILE_TYPE_VIDEOS = new String[]{
            "flv", "mp4", "ts", "3gp", "mov", "avi", "wmv"
    };
    private static final String[] FILE_TYPE_AUDIOS = new String[]{
            "mp3"
    };
    private static final String[] FILE_TYPE_3DS = new String[]{
            "hsf", "stl", "obj", "vds",
            "pdf", "prc", "u3d", "step",
            "jt", "iges", "ifc", "ifczip",
            "x_b", "x_t", "x_mt", "xmt_txt"
    };
    private static final String[] FILE_TYPE_OFFICES = new String[]{
            "doc", "docm", "docx", "dotx",
            "xls", "xlsx", "xlsb", "xlsm",
            "xlt", "xltx", "xltm", "ppt",
            "pptx", "potm", "potx"
    };
    private static final String[] FILE_TYPE_CADS_SUPPORTED_RMS = new String[]{
            "prt", "sldprt", "sldasm", "catpart", "catshape",
            "cgr", "neu", "par", "psm", "ipt",
            "igs", "stp", "3dxml", "vsd"
    };
    private static final String[] FILE_TYPE_IMAGES_SUPPORTED_RMS = new String[]{
            "tif", "tiff", "properties"
    };
    private static final String[] FILE_TYPE_TEXT_SUPPORTED_RMS = new String[]{
            "js", "rtf"
    };
    private static final String[] FILE_TYPE_UN_SUPPORTED = new String[]{
            "key", "numbers", "pages"
    };

    @Override
    public boolean accept(String filename) throws OfflineException {
        String tmpFileName = checkNoNull(filename);//1.3-2018-01-25-08-00-36.pdf.nxl
        int idx = tmpFileName.lastIndexOf(".");
        if (idx == -1) {
            return false;
        }
        String nameSuffixNormal = tmpFileName.substring(0, idx);//1.3-2018-01-25-08-00-36.pdf
        String extension = getExtensionByName(nameSuffixNormal);
        if (TextUtils.isEmpty(extension)) {
            return false;
        }
        String extenLowcase = extension.toLowerCase();
        return isLocalSupportedExtensions(extenLowcase);
    }

    private boolean isLocalSupportedExtensions(String extension) throws OfflineException {
        Set<String> remoteTypes = buildRMSSupportedTypes();
        Set<String> unSupportedTypes = buildUnSupportedTypes();
        if (remoteTypes.contains(extension) || unSupportedTypes.contains(extension)) {
            return false;
        }
        Set<String> localTypes = buildLocalSupportedTypes();
        if (localTypes.contains(extension)) {
            return true;
        }
        throw new OfflineException(OfflineStatus.STATUS_FILTER_FAILED_UNKNOWN_TYPE, "The file type: " + extension + " currently is unknown.");
    }

    private Set<String> buildLocalSupportedTypes() {
        Set<String> supported = new HashSet<>();
        supported.addAll(Arrays.asList(FILE_TYPE_TEXTS));
        supported.addAll(Arrays.asList(FILE_TYPE_IMAGES));
        supported.addAll(Arrays.asList(FILE_TYPE_VIDEOS));
        supported.addAll(Arrays.asList(FILE_TYPE_AUDIOS));
        supported.addAll(Arrays.asList(FILE_TYPE_3DS));
        return supported;
    }

    private Set<String> buildRMSSupportedTypes() {
        Set<String> supported = new HashSet<>();
        supported.addAll(Arrays.asList(FILE_TYPE_OFFICES));
        supported.addAll(Arrays.asList(FILE_TYPE_CADS_SUPPORTED_RMS));
        supported.addAll(Arrays.asList(FILE_TYPE_IMAGES_SUPPORTED_RMS));
        supported.addAll(Arrays.asList(FILE_TYPE_TEXT_SUPPORTED_RMS));
        return supported;
    }

    private Set<String> buildUnSupportedTypes() {
        Set<String> supported = new HashSet<>(Arrays.asList(FILE_TYPE_UN_SUPPORTED));
        return supported;
    }

    /**
     * @param fileName file name
     * @return extension
     */
    private String getExtensionByName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }

        int dot = fileName.lastIndexOf('.');
        if (dot > -1 && dot < fileName.length()) {
            return fileName.substring(dot + 1);
        }
        return "";
    }

    private String checkNoNull(String value) {
        if (TextUtils.isEmpty(value)) {
            throw new NullPointerException("nxlPath must not be null.");
        }
        return value;
    }
}
