package com.skydrm.sdk.policy;


import com.skydrm.sdk.INxlRights;

import java.util.ArrayList;
import java.util.List;

public final class Rights implements INxlRights {
    private boolean hasView;
    private boolean hasEdit;
    private boolean hasPrint;
    private boolean hasShare;
    private boolean hasDownload;
    private boolean hasWatermark;
    private boolean hasDecrypt;
    private boolean hasClassify;

    // rights value may return from rms
    private int permissions = 0;

    public Rights() {
        clear();
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    /**
     * Parse the rights from the permissions value.
     * Added by allen ning on 7/6/2017
     */
    public void IntegerToRights() {
        clear();

        if ((permissions & INxlRights.VIEW) == INxlRights.VIEW) {
            hasView = true;
        }

        if ((permissions & INxlRights.EDIT) == INxlRights.EDIT) {
            hasEdit = true;
        }

        if ((permissions & INxlRights.PRINT) == INxlRights.PRINT) {
            hasPrint = true;
        }

        if ((permissions & INxlRights.SHARE) == INxlRights.SHARE) {
            hasShare = true;
        }

        if ((permissions & INxlRights.DOWNLOAD) == INxlRights.DOWNLOAD) {
            hasDownload = true;
        }

        // Rights to waterMark.
        if ((permissions & INxlRights.WATERMARK) == INxlRights.WATERMARK) {
            hasWatermark = true;
        }

        // Rights to decrypt
        if ((permissions & INxlRights.DECRYPT) == INxlRights.DECRYPT) {
            hasDecrypt = true;
        }

        if ((permissions & INxlRights.CLASSIFY) == INxlRights.CLASSIFY) {
            hasClassify = true;
        }
    }

    public void clear() {
        hasView = false;
        hasEdit = false;
        hasPrint = false;
        hasShare = false;
        hasDownload = false;
        hasWatermark = false;
        hasDecrypt = false;
        hasClassify = false;
    }

    @Override
    public boolean hasView() {
        return hasView;
    }

    public void setView(boolean bView) {
        this.hasView = bView;
    }

    @Override
    public boolean hasEdit() {
        return hasEdit;
    }

    public void setEdit(boolean bEdit) {
        this.hasEdit = bEdit;
    }

    @Override
    public boolean hasPrint() {
        return hasPrint;
    }

    public void setPrint(boolean bPrint) {
        this.hasPrint = bPrint;
    }

    @Override
    public boolean hasShare() {
        return hasShare;
    }

    public void setShare(boolean bShare) {
        this.hasShare = bShare;
    }

    public void setDecrypt(boolean decrypt) {
        this.hasDecrypt = decrypt;
    }

    @Override
    public boolean hasDecrypt() {
        return hasDecrypt;
    }

    @Override
    public boolean hasDownload() {
        return hasDownload;
    }

    public void setDownload(boolean bDownload) {
        this.hasDownload = bDownload;
    }

    @Override
    public boolean hasWatermark() {
        return hasWatermark;
    }

    public void setWatermark(boolean bWatermark) {
        this.hasWatermark = bWatermark;
    }

    public boolean hasClassify() {
        return hasClassify;
    }

    public void setClassify(boolean classify) {
        this.hasClassify = classify;
    }

    @Override
    public int toInteger() {
        int rt = 0;
        if (hasView) {
            rt += INxlRights.VIEW;
        }
        if (hasDownload) {
            rt += INxlRights.DOWNLOAD;
        }
        if (hasShare) {
            rt += INxlRights.SHARE;
        }
        if (hasPrint) {
            rt += INxlRights.PRINT;
        }
        if (hasEdit) {
            rt += INxlRights.EDIT;
        }
        if (hasWatermark) {
            rt += INxlRights.WATERMARK;
        }
        if (hasDecrypt) {
            rt += INxlRights.DECRYPT;
        }
        if (hasClassify) {
            rt += INxlRights.CLASSIFY;
        }
        return rt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rights:[");
        if (hasView) {
            sb.append(INxlRights.RIGHT_VIEW + " ");
        }
        if (hasEdit) {
            sb.append(INxlRights.RIGHT_EDIT + " ");
        }
        if (hasPrint) {
            sb.append(INxlRights.RIGHT_PRINT + " ");
        }
        if (hasShare) {
            sb.append(INxlRights.RIGHT_SHARE + " ");
        }
        if (hasDownload) {
            sb.append(INxlRights.RIGHT_DOWNLOAD + " ");
        }
        if (hasWatermark) {
            sb.append(INxlRights.RIGHT_WATERMARK + " ");
        }
        if (hasDecrypt) {
            sb.append(INxlRights.DECRYPT);
        }
        if (hasClassify) {
            sb.append(INxlRights.CLASSIFY);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public List<String> toList() {
        List<String> list = new ArrayList<String>();
        if (hasView) {
            list.add(INxlRights.RIGHT_VIEW);
        }
        if (hasEdit) {
            list.add(INxlRights.RIGHT_EDIT);
        }
        if (hasPrint) {
            list.add(INxlRights.RIGHT_PRINT);
        }
        if (hasShare) {
            list.add(INxlRights.RIGHT_SHARE);
        }
        if (hasDownload) {
            list.add(INxlRights.RIGHT_DOWNLOAD);
        }
        if (hasWatermark) {
            list.add(INxlRights.RIGHT_WATERMARK);
        }
        if (hasDecrypt) {
            list.add(INxlRights.RIGHT_DECRYPT);
        }
        if (hasClassify) {
            list.add(INxlRights.RIGHT_CLASSIFY);
        }
        return list;
    }

    /**
     * Parse the rights from the string list.
     */
    public void listToRights(List<String> list) {
        for (String right : list) {
            switch (right) {
                case INxlRights.RIGHT_VIEW:
                    hasView = true;
                    break;
                case INxlRights.RIGHT_EDIT:
                    hasEdit = true;
                    break;
                case INxlRights.RIGHT_PRINT:
                    hasPrint = true;
                    break;
                case INxlRights.RIGHT_SHARE:
                    hasShare = true;
                    break;
                case INxlRights.RIGHT_DOWNLOAD:
                    hasDownload = true;
                    break;
                case INxlRights.RIGHT_WATERMARK:
                    hasWatermark = true;
                    break;
                case INxlRights.RIGHT_DECRYPT:
                    hasDecrypt = true;
                    break;
                case INxlRights.RIGHT_CLASSIFY:
                    hasClassify = true;
                    break;
                default:
                    break;
            }
        }
    }
}
