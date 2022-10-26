package com.skydrm.rmc.database;

import android.database.sqlite.SQLiteOpenHelper;

import com.skydrm.rmc.database.table.log.ActivityLogDao;
import com.skydrm.rmc.database.table.membership.MembershipDao;
import com.skydrm.rmc.database.table.myvault.MyVaultFileDao;
import com.skydrm.rmc.database.table.project.ProjectDao;
import com.skydrm.rmc.database.table.project.ProjectFileDao;
import com.skydrm.rmc.database.table.project.ProjectFileExDao;
import com.skydrm.rmc.database.table.project.SharedWithProjectFileDao;
import com.skydrm.rmc.database.table.server.ServerDao;
import com.skydrm.rmc.database.table.sharedwithme.SharedWithMeFileDao;
import com.skydrm.rmc.database.table.user.UserDao;
import com.skydrm.rmc.database.table.workspace.WorkSpaceFileDao;

public class DaoSession {
    private ServerDao mServerDao;
    private UserDao mUserDao;
    private MembershipDao mMembershipDao;
    private MyVaultFileDao mMyVaultFileDao;
    private SharedWithMeFileDao mSharedWithMeFileDao;
    private ProjectDao mProjectDao;
    private ProjectFileDao mProjectFileDao;
    private ActivityLogDao mActivityLogDao;
    private WorkSpaceFileDao mWorkSpaceFileDao;
    private ProjectFileExDao mProjectFileExDao;
    private SharedWithProjectFileDao mSharedWithProjectFileDao;

    DaoSession(SQLiteOpenHelper helper) {
        mServerDao = new ServerDao(helper);
        mUserDao = new UserDao(helper);
        mMembershipDao = new MembershipDao(helper);
        mMyVaultFileDao = new MyVaultFileDao(helper);
        mSharedWithMeFileDao = new SharedWithMeFileDao(helper);
        mProjectDao = new ProjectDao(helper);
        mProjectFileDao = new ProjectFileDao(helper);
        mActivityLogDao = new ActivityLogDao(helper);
        mWorkSpaceFileDao = new WorkSpaceFileDao(helper);
        mProjectFileExDao = new ProjectFileExDao(helper);
        mSharedWithProjectFileDao = new SharedWithProjectFileDao(helper);
    }

    public void initDatabase() {
        mServerDao.createTable();
        mUserDao.creteTable();
        mMembershipDao.createTable();
        mMyVaultFileDao.createTable();
        mSharedWithMeFileDao.createTable();
        mProjectDao.createTable();
        mProjectFileDao.createTable();
        mActivityLogDao.createTable();
        mWorkSpaceFileDao.createTable();
        mProjectFileExDao.createTable();
        mSharedWithProjectFileDao.createTable();
    }

    public ServerDao getServerDao() {
        return mServerDao;
    }

    public UserDao getUserDao() {
        return mUserDao;
    }

    public MyVaultFileDao getMyVaultFileDao() {
        return mMyVaultFileDao;
    }

    public SharedWithMeFileDao getSharedWithMeFileDao() {
        return mSharedWithMeFileDao;
    }

    public ProjectDao getProjectDao() {
        return mProjectDao;
    }

    public ProjectFileDao getProjectFileDao() {
        return mProjectFileDao;
    }

    public ActivityLogDao getActivityLogDao() {
        return mActivityLogDao;
    }

    public WorkSpaceFileDao getWorkSpaceFileDao() {
        return mWorkSpaceFileDao;
    }

    public ProjectFileExDao getProjectFileExDao() {
        return mProjectFileExDao;
    }

    public SharedWithProjectFileDao getSharedWithProjectFileDao() {
        return mSharedWithProjectFileDao;
    }

}
