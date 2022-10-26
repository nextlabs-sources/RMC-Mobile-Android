package com.skydrm.rmc.reposystem.types;

import java.util.List;


public class NXFolder extends NxFileBase {
    {
        super.setIsFolder(true);
    }

    public NXFolder() {
    }

    public NXFolder(String localPath, String cloudPath, String name, long size) {
        super.setDisplayPath(localPath);
        super.setLocalPath(localPath);
        super.setCloudPath(cloudPath);
        super.setmCloudPathID(cloudPath);
        super.setName(name);
        super.setSize(size);
        super.setIsFolder(true);

    }

    public NXFolder(INxFile f) {
        super.setDisplayPath(f.getDisplayPath());
        super.setLocalPath(f.getLocalPath());
        super.setCloudPath(f.getCloudPath());
        super.setmCloudPathID(f.getCloudFileID());
        super.setName(f.getName());
        super.setSize(f.getSize());
        super.setIsFolder(true);
        super.setBoundService(f.getService());
    }


    // recursive to find the node
    //
    @Override
    public INxFile findNode(String path) {
        INxFile rt;
        // recursive way out
        if (getLocalPath().equalsIgnoreCase(path))
            return this;
        else {
            List<INxFile> children = this.getChildren();
            for (INxFile obj : children) {
                //remove '/' sign
                if (path.endsWith("/"))
                    path = path.substring(0, path.length() - 1);
                // Match test
                if (path.regionMatches(true, 0, obj.getLocalPath(), 0, obj.getLocalPath().length())) {
                    rt = obj.findNode(path);
                    if (rt != null)
                        return rt;
                }
                continue;
            }
            return null;

        }
    }

}
