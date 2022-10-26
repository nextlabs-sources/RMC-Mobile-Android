package com.skydrm.rmc.reposystem.types;


public class NXDocument extends NxFileBase {
    {
        super.setIsFolder(false);
    }

    public NXDocument() {

    }

    public NXDocument(INxFile f) {
        super.setDisplayPath(f.getDisplayPath());
        super.setLocalPath(f.getLocalPath());
        super.setCloudPath(f.getCloudPath());
        super.setmCloudPathID(f.getCloudFileID());
        super.setName(f.getName());
        super.setSize(f.getSize());
        super.setUserDefinedStr(f.getUserDefinedStr());
        super.setBoundService(f.getService());
    }


    @Override
    public NxFileBase findNode(String path) {
        if (getLocalPath().equalsIgnoreCase(path))
            return this;
        else
            return null;
    }
}
