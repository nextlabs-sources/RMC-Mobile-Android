package com.skydrm.sdk;


import java.util.List;

public interface INxlRights {
    //Right to view plain content                   0x1
    int VIEW = 0X1;
    String RIGHT_VIEW = "VIEW";
    //Right to modify plain content                 0x2
    int EDIT = 0x2;
    String RIGHT_EDIT = "EDIT";
    //Right to print document                       0x4
    int PRINT = 0x4;
    String RIGHT_PRINT = "PRINT";
    //Right to copy plain content to clipboard      0X8
    int CLIPBOARD = 0X8;
    String RIGHT_CLIPBOARD = "CLIPBOARD";
    //Right to save document to new file or format  0X10
    int SAVEAS = 0X10;
    String RIGHT_SAVEAS = "SAVEAS";
    //Right to decrypt document                     0X20
    int DECRYPT = 0X20;
    String RIGHT_DECRYPT = "DECRYPT";
    //Right to take a screen snapshot of document   0X40
    int SCREENCAP = 0X40;
    String RIGHT_SCREENCAP = "SCREENCAP";
    //Right to send document                        0X80
    int SEND = 0X80;
    String RIGHT_SEND = "SEND";
    //Right to classify document                    0X100
    int CLASSIFY = 0X100;
    String RIGHT_CLASSIFY = "CLASSIFY";
    //Right to share document                       0X200
    int SHARE = 0X200;
    String RIGHT_SHARE = "SHARE";
    //Right to download document                    0X400
    int DOWNLOAD = 0X400;
    String RIGHT_DOWNLOAD = "DOWNLOAD";

    //Right to watermark
    int WATERMARK = 0X40000000; // (1 << 30)
    String RIGHT_WATERMARK = "WATERMARK";

    boolean hasView();

    boolean hasEdit();

    boolean hasPrint();

    boolean hasShare();

    boolean hasDecrypt();

    boolean hasDownload();

    boolean hasWatermark();

    boolean hasClassify();

    int toInteger();

    List<String> toList();
}
