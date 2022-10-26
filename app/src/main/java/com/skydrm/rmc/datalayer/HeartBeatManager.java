package com.skydrm.rmc.datalayer;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeat;
import com.skydrm.rmc.datalayer.repo.base.IBaseRepo;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.user.UserService;

import java.util.LinkedList;
import java.util.List;

public class HeartBeatManager {
    private static final DevLog mLog = new DevLog(HeartBeatManager.class.getName());
    private static HeartBeatManager mInstance;

    public static HeartBeatManager getInstance() {
        if (mInstance == null) {
            synchronized (HeartBeatManager.class) {
                if (mInstance == null) {
                    return mInstance = new HeartBeatManager();
                }
            }
        }
        return mInstance;
    }

    public void onHeartBeat() {
        List<IHeartBeat> heartBeatTasks = getHeartBeatTasks();
        if (heartBeatTasks == null || heartBeatTasks.size() == 0) {
            return;
        }
        for (IHeartBeat heartBeat : heartBeatTasks) {
            if (heartBeat == null) {
                continue;
            }
            try {
                heartBeat.onHeatBeat(null);
            } catch (Exception e) {
                mLog.e(e.getMessage(), e);
            }
        }
    }

    private List<IHeartBeat> getHeartBeatTasks() {
        List<IHeartBeat> ret = new LinkedList<>();
        IBaseRepo myVault = RepoFactory.getRepo(RepoType.TYPE_MYVAULT);
        if (myVault != null) {
            ret.add(myVault);
        }
        IBaseRepo project = RepoFactory.getRepo(RepoType.TYPE_PROJECT);
        if (project != null) {
            ret.add(project);
        }
        IBaseRepo sharedWithMe = RepoFactory.getRepo(RepoType.TYPE_SHARED_WITH_ME);
        if (sharedWithMe != null) {
            ret.add(sharedWithMe);
        }
        if (SkyDRMApp.getInstance().isOnPremise()) {
            IBaseRepo workSpace = RepoFactory.getRepo(RepoType.TYPE_WORKSPACE);
            if (workSpace != null) {
                ret.add(workSpace);
            }
        }
        ret.add(new UserService());

        return ret;
    }
}
