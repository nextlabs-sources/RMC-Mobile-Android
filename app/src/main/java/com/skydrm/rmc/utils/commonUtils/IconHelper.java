package com.skydrm.rmc.utils.commonUtils;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.widget.ImageView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.reposystem.types.BoundService;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by hhu on 4/18/2017.
 */

public class IconHelper {
    private static final String TAG_NXL_ICONS = "NxlIcons";
    private static final String TAG_NORMAL_ICONS = "NormalIcons";
    private static final String TAG_ICON = "Icon";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_RESOURCE = "resource";
    private static final String NAME_NXL_MISSING = ".missing.nxl";
    private static final String NAME_NORMAL_MISSING = ".missing";

    private static final Map<String, Integer> mIcons = new HashMap<>();

    private static String[] docFormat = {".dotm", ".dot"};
    private static String[] excelFormat = {".xlam"};
    private static String[] pptFormat = {".pptm", ".ppt", ".pot", ".ppsm", ".pps", ".ppam", ".ppa"};

    static {
        mIcons.putAll(stringIndex2Id(SkyDRMApp.getInstance(),
                getIconsFromXML(SkyDRMApp.getInstance())));
    }

    private static Map<String, String> getIconsFromXML(Context ctx) {
        Map<String, String> ret = new HashMap<>();
        if (ctx == null) {
            return ret;
        }
        try (XmlResourceParser xpp = ctx.getResources().getXml(R.xml.icons)) {
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    System.out.println("Start read document.");
                } else if (eventType == XmlPullParser.START_TAG) {
                    String tagName = xpp.getName();
                    if (TAG_NXL_ICONS.contentEquals(tagName)) {
                        System.out.println("Start read tag " + tagName);
                    } else if (TAG_NORMAL_ICONS.contentEquals(tagName)) {
                        System.out.println("Start read tag " + tagName);
                    } else if (TAG_ICON.contentEquals(tagName)) {
                        System.out.println("Start read tag " + tagName);
                        String extension = xpp.getAttributeValue(null, ATTRIBUTE_NAME);
                        String resourceId = xpp.getAttributeValue(null, ATTRIBUTE_RESOURCE);
                        ret.put(extension, resourceId);
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    System.out.println("Read tag finished.");
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void setRepoThumbnail(BoundService s, ImageView serviceThumbnail) {
        if (s == null || serviceThumbnail == null) {
            return;
        }
//        serviceThumbnail.setColorFilter(Color.BLACK);
        if (s.type.equals(BoundService.ServiceType.DROPBOX)) {
            serviceThumbnail.setImageResource(R.drawable.bottom_sheet_dropbox);
        } else if (s.type.equals(BoundService.ServiceType.ONEDRIVE)) {
            serviceThumbnail.setImageResource(R.drawable.bottom_sheet_onedrive);
        } else if (s.type.equals(BoundService.ServiceType.SHAREPOINT)) {
            serviceThumbnail.setImageResource(R.drawable.bottom_sheet_sharepoint);
        } else if (s.type.equals(BoundService.ServiceType.SHAREPOINT_ONLINE)) {
            serviceThumbnail.setImageResource(R.drawable.bottom_sheet_sharepoint_online);
        } else if (s.type.equals(BoundService.ServiceType.GOOGLEDRIVE)) {
            serviceThumbnail.setImageResource(R.drawable.bottom_sheet_googledrive);
        } else if (s.type.equals(BoundService.ServiceType.BOX)) {
            serviceThumbnail.setImageResource(R.drawable.bottom_sheet_box);
        } else if (s.type.equals(BoundService.ServiceType.MYDRIVE)) {
            serviceThumbnail.setImageResource(R.drawable.bottom_sheet_my_drive);
        }
    }

    public static int getNxlIconResourceIdByExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return getMissingTypeIcon(false);
        }
        if (mIcons.size() == 0) {
            return R.drawable.icon_missing_normal;
        }
        String manualExtension;
        String tmpName = fileName.toLowerCase();
        if (tmpName.contains(".nxl")) {
            int lDotIdx = tmpName.lastIndexOf(".");
            if (lDotIdx != -1) {
                String remains = tmpName.substring(0, lDotIdx);
                int tmpIdx = tmpName.lastIndexOf(".");
                if (tmpIdx != -1) {
                    String nxl = tmpName.substring(tmpIdx);
                    int typeIdx = remains.lastIndexOf(".");
                    if (typeIdx != -1) {
                        String typeName = remains.substring(typeIdx);
                        manualExtension = typeName + nxl;
                    } else {
                        manualExtension = nxl;
                    }
                } else {
                    manualExtension = tmpName;
                }
            } else {
                manualExtension = tmpName;
            }
        } else {
            int idx = tmpName.lastIndexOf(".");
            if (idx != -1) {
                manualExtension = tmpName.substring(idx);
            } else {
                manualExtension = tmpName;
            }
        }
        if (manualExtension.isEmpty()) {
            return getMissingTypeIcon(false);
        }
        if (mIcons.containsKey(manualExtension)) {
            Integer id = mIcons.get(manualExtension);
            if (id == null) {
                return getMissingTypeIcon(manualExtension.contains("nxl"));
            }
            return id;
        } else {
            return getMissingTypeIcon(manualExtension.contains("nxl"));
        }
    }

    private static Map<String, Integer> stringIndex2Id(Context ctx, Map<String, String> icons) {
        Map<String, Integer> map = new HashMap<>();
        if (icons == null || icons.size() == 0) {
            return map;
        }
        if (ctx == null) {
            return map;
        }
        Set<String> keys = icons.keySet();
        for (String name : keys) {
            if (name == null || name.isEmpty()) {
                continue;
            }
            //R.drawable.xxx
            String idIndex = icons.get(name);
            String drawableName = "";
            if (idIndex != null && idIndex.contains("R.drawable.")) {
                drawableName = idIndex.substring(idIndex.lastIndexOf(".") + 1);
            } else {
                drawableName = idIndex;
            }
            if (drawableName == null || drawableName.isEmpty()) {
                continue;
            }
            int drawableId = ctx.getResources().getIdentifier(drawableName, "drawable",
                    ctx.getPackageName());
            if (drawableId == 0) {
                continue;
            }
            map.put(name, drawableId);
        }
        return map;
    }

    private static Integer getMissingTypeIcon(boolean nxl) {
        return mIcons.get(nxl ? NAME_NXL_MISSING : NAME_NORMAL_MISSING);
    }

    private static boolean isExists(String dest, OFFICETYPE type) {
        String[] temp = null;
        switch (type) {
            case WORDDOC:
                temp = docFormat;
                break;
            case EXCELDOC:
                temp = excelFormat;
                break;
            case PPTDOC:
                temp = pptFormat;
                break;
        }
        if (temp != null) {
            for (String suffix : temp) {
                if (dest.endsWith(suffix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private enum OFFICETYPE {
        WORDDOC(0),
        EXCELDOC(1),
        PPTDOC(2);
        private int value = 0;

        OFFICETYPE(int type) {
            value = type;
        }

        public static OFFICETYPE valueOf(int value) {
            switch (value) {
                case 0:
                    return WORDDOC;
                case 1:
                    return EXCELDOC;
                case 2:
                    return PPTDOC;
                default:
                    throw new IllegalArgumentException("value" + value + " is not a legal value to convert to OFFICETYPE");
            }
        }

        public int value() {
            return this.value;
        }
    }
}
