package com.skydrm.hoops;

public class MobileApp {
    private static native void shutdownV();

    private static native void setFontDirectoryS(String fontDir);

    private static native void setMaterialsDirectoryS(String materialsDir);

    public static void shutdown() {
        shutdownV();
    }

    public static void setFontDirectory(String fontDir) {
        setFontDirectoryS(fontDir);
    }

    public static void setMaterialsDirectory(String materialsDir) {
        setMaterialsDirectoryS(materialsDir);
    }
}
