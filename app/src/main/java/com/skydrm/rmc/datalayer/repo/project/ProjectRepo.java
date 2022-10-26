package com.skydrm.rmc.datalayer.repo.project;

import android.util.Log;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.database.table.project.ProjectMemberBean;
import com.skydrm.rmc.datalayer.heartbeat.CommonPolicy;
import com.skydrm.rmc.datalayer.heartbeat.HeartbeatPolicyGenerator;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatListener;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatPolicy;
import com.skydrm.rmc.datalayer.heartbeat.ProjectHeartbeatPolicy;
import com.skydrm.rmc.datalayer.repo.NxlRepo;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.dbbridge.DBProvider;
import com.skydrm.rmc.dbbridge.IDBProjectItem;
import com.skydrm.rmc.dbbridge.IOwner;
import com.skydrm.rmc.dbbridge.base.Owner;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.project.AllProjectsResult;
import com.skydrm.sdk.rms.rest.project.CreateProjectResult;
import com.skydrm.sdk.rms.rest.project.ListPendingInvitationResult;
import com.skydrm.sdk.rms.rest.project.ListProjectItemResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectRepo extends NxlRepo implements IProjectService, IHeartBeatListener, IDestroyable {
    private DevLog mLog = new DevLog(ProjectRepo.class.getSimpleName());
    private static List<AllProjectsResult.ResultsBean.DetailBean> mAllProjectsTmpData = new ArrayList<>();
    private static final Queue<IProject> mTaskQueue = new ConcurrentLinkedDeque<>();
    private AtomicInteger mFinishCount = new AtomicInteger(0);
    private AtomicBoolean mEmptyData = new AtomicBoolean(true);
    private AtomicBoolean mConcurrent = new AtomicBoolean(false);
    private volatile int mTotal;
    private IHeartBeatListener mListener;

    public void registerHeartBeatListener(IHeartBeatListener l) {
        this.mListener = l;
    }

    void fireTaskToCrawlTarget(IProject p) {
        runWithLowerPriority(p);
    }

    void fireTaskToSyncClassification(IProject p) {
        runWithLowerPrioritySyncClassification(p);
    }

    public void queryProjectOwnerId(File f,
                                    QueryOwnerIdTask.ITaskCallback<QueryOwnerIdTask.Result, Exception> callback) {
        QueryOwnerIdTask task = new QueryOwnerIdTask(f, callback);
        task.run();
    }

    public List<AllProjectsResult.ResultsBean.DetailBean> getAllProjectTmpData() {
        return mAllProjectsTmpData;
    }

    /**
     * @param type 0[ownerByMe&invitedByOther]
     *             1 ownerByMe
     *             2 invitedByOther
     * @return
     */
    @Override
    public List<IProject> listProject(int type) {
        return adapt2ProjectItem(listProjectInternal(type));
    }

    @Override
    public List<IProject> syncProject(int type) throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException {
        List<ListProjectItemResult.ResultsBean.DetailBean> remotes = syncAllProject(type);
        List<IDBProjectItem> locals = listProjectInternal(type);
        mEmptyData.set(locals.size() == 0);
        boolean altered = filterOutModifiedItems(remotes, locals);
        return altered ? listProject(type) : adapt2ProjectItem(locals);
    }

    @Override
    public IProject createProject(String name, String description,
                                  List<String> emails, String invitationMsg)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        CreateProjectResult result = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .createProject(name, description, emails, invitationMsg);
        if (result == null) {
            return null;
        }
        CreateProjectResult.ResultsBean results = result.getResults();
        if (results == null) {
            return null;
        }
        int projectId = results.getProjectId();
        boolean altered = filterOutModifiedItems(syncProjectInternal(true).getResults().getDetail(),
                listProjectInternal(1));

        if (altered) {
            List<IProject> projects = listProject(1);
            for (IProject p : projects) {
                if (p.getId() == projectId) {
                    return p;
                }
            }
            return null;
        }
        return null;
    }

    @Override
    public List<INxlFile> list(int type) {
        return null;
    }

    @Override
    public List<INxlFile> list(int type, String pathId, boolean recursively) {
        return null;
    }

    @Override
    public List<INxlFile> sync(int type)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException {
        return null;
    }

    @Override
    public List<INxlFile> sync(int type, String pathId, boolean recursively)
            throws SessionInvalidException, RmsRestAPIException, InvalidRMClientException {
        return null;
    }

    public List<IInvitePending> syncPendingInvitation() throws SessionInvalidException,
            InvalidRMClientException, RmsRestAPIException {
        List<IInvitePending> ret = new ArrayList<>();
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        ListPendingInvitationResult result = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .listPendingInvitationForUser();
        if (result == null) {
            return ret;
        }
        ListPendingInvitationResult.ResultsBean results = result.getResults();
        if (results == null) {
            return ret;
        }
        List<ListPendingInvitationResult.ResultsBean.PendingInvitationsBean> pendingInvitations =
                results.getPendingInvitations();
        if (pendingInvitations == null || pendingInvitations.size() == 0) {
            return ret;
        }
        for (ListPendingInvitationResult.ResultsBean.PendingInvitationsBean i : pendingInvitations) {
            if (i == null) {
                continue;
            }
            ListPendingInvitationResult.ResultsBean.PendingInvitationsBean.ProjectBean p = i.getProject();

            int id = -1;
            String name = "";
            String description = "";
            String displayName = "";
            long creationTime = 0;

            int ownerUserId = -1;
            String ownerName = "";
            String ownerEmail = "";
            if (p != null) {
                id = p.getId();
                name = p.getName();
                description = p.getDescription();
                displayName = p.getDisplayName();
                creationTime = p.getCreationTime();

                ListPendingInvitationResult.ResultsBean.PendingInvitationsBean.ProjectBean.OwnerBean
                        owner = p.getOwner();
                if (owner != null) {
                    ownerUserId = owner.getUserId();
                    ownerName = owner.getName();
                    ownerEmail = owner.getEmail();
                }
            }

            Project pending = Project.newPendingProject(i.getInvitationId(), i.getInviteeEmail(),
                    i.getInviterDisplayName(), i.getInviterEmail(), i.getInviteTime(), i.getCode(),
                    i.getInvitationMsg(), id, name, description, displayName,
                    creationTime, ownerUserId, ownerName, ownerEmail);
            ret.add(pending);
        }
        return ret;
    }

    private List<IProject> adapt2ProjectItem(List<IDBProjectItem> items) {
        List<IProject> ret = new ArrayList<>();
        if (items == null || items.size() == 0) {
            return ret;
        }
        for (IDBProjectItem i : items) {
            ret.add(Project.newByDBItem(i));
        }
        return ret;
    }

    private boolean filterOutModifiedItems(List<ListProjectItemResult.ResultsBean.DetailBean> remotes,
                                           List<IDBProjectItem> locals) {

        long startTimeMillis = System.currentTimeMillis();
        try {
            DBProvider dbProvider = SkyDRMApp.getInstance().getDBProvider();
            //If remote return empty data.
            if (remotes == null || remotes.size() == 0) {
                //If local empty.
                if (locals == null || locals.size() == 0) {
                    return false;
                }
                //Clear local records if exist.
                int deleteAffected = 0;
                for (IDBProjectItem i : locals) {
                    dbProvider.deleteProjectItem(i.getProjectTBPK());
                    deleteAffected++;
                }
                return deleteAffected > 0;
            }
            //If local is empty,then insert all items from remote.
            if (locals == null || locals.size() == 0) {
                int insertAffected = 0;
                for (ListProjectItemResult.ResultsBean.DetailBean r : remotes) {
                    int _id = dbProvider.upsertProjectItem(r.getId(), r.getParentTenantId(), r.getParentTenantName(),
                            r.getTokenGroupName(), r.getName(), r.getDescription(), r.getDisplayName(),
                            r.getCreationTime(), r.getConfigurationModified(), r.getTotalMembers(),
                            r.getTotalFiles(), r.isOwnedByMe(), getOwnerRawJson(r.getOwner()),
                            r.getAccountType(), r.getTrialEndTime(), r.getExpiry(),
                            r.getWatermark());
                    insertAffected++;
                    batchInsertMemberItem(_id, r, dbProvider);
                }
                return insertAffected > 0;
            }

            List<Integer> upsertsAffected = new ArrayList<>();
            for (ListProjectItemResult.ResultsBean.DetailBean r : remotes) {
                if (r == null) {
                    continue;
                }
                if (contains(locals, r)) {
                    continue;
                }
                int _id = dbProvider.upsertProjectItem(r.getId(), r.getParentTenantId(), r.getParentTenantName(),
                        r.getTokenGroupName(), r.getName(), r.getDescription(), r.getDisplayName(),
                        r.getCreationTime(), r.getConfigurationModified(), r.getTotalMembers(),
                        r.getTotalFiles(), r.isOwnedByMe(), getOwnerRawJson(r.getOwner()),
                        r.getAccountType(), r.getTrialEndTime(), r.getExpiry(),
                        r.getWatermark());
                upsertsAffected.add(_id);
                if (_id != -1) {
                    ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean projectMembers =
                            r.getProjectMembers();
                    if (projectMembers != null) {
                        List<ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean.MembersBean> members =
                                projectMembers.getMembers();
                        if (members != null && members.size() != 0) {
                            for (ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean.MembersBean m : members) {
                                dbProvider.upsertProjectMemberItem(_id, m.getUserId(), m.getDisplayName(),
                                        m.getEmail(), m.getCreationTime());
                            }
                        }
                    }
                }
            }

            int deletedAffected = 0;
            for (IDBProjectItem l : locals) {
                if (l == null) {
                    continue;
                }
                if (contains(remotes, l)) {
                    continue;
                }
                if (upsertsAffected.contains(l.getProjectTBPK())) {
                    continue;
                }
                dbProvider.deleteProjectItem(l.getProjectTBPK());
                deletedAffected++;
            }
            return upsertsAffected.size() != 0 || deletedAffected > 0;
        } finally {
            long endTimeMillis = System.currentTimeMillis();
            Log.i("SyncProject", "filterOutModifiedItems time consumes[Millis]." + (endTimeMillis - startTimeMillis));
        }
    }

    private boolean batchInsertMemberItem(int _id, ListProjectItemResult.ResultsBean.DetailBean r,
                                          DBProvider dbProvider) {
        if (_id == -1 || r == null) {
            return false;
        }

        ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean projectMembers =
                r.getProjectMembers();
        if (projectMembers == null) {
            return false;
        }
        List<ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean.MembersBean> members =
                projectMembers.getMembers();
        if (members == null || members.size() == 0) {
            return false;
        }
        List<ProjectMemberBean> inserts = new ArrayList<>();
        for (ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean.MembersBean m : members) {
            inserts.add(ProjectMemberBean.getInsertItem(m));
        }
        return dbProvider.batchInsertProjectMemberItem(_id, inserts);
    }

    private boolean contains(List<IDBProjectItem> locals,
                             ListProjectItemResult.ResultsBean.DetailBean r) {
        if (locals == null || locals.size() == 0) {
            return false;
        }
        if (r == null) {
            return false;
        }
        for (IDBProjectItem l : locals) {
            if (projectEquals(l, r)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(List<ListProjectItemResult.ResultsBean.DetailBean> remotes,
                             IDBProjectItem l) {
        if (remotes == null || remotes.size() == 0) {
            return false;
        }
        if (l == null) {
            return false;
        }
        for (ListProjectItemResult.ResultsBean.DetailBean r : remotes) {
            if (projectEquals(l, r)) {
                return true;
            }
        }
        return false;
    }

    private boolean projectEquals(IDBProjectItem l, ListProjectItemResult.ResultsBean.DetailBean r) {
        if (l == null || r == null) {
            return false;
        }
        //Means is not the same project.
        //just return false.
        if (l.getId() != r.getId()) {
            return false;
        }

        filterOutMemberModified(l.getProjectTBPK(), l.getMember(),
                r.getProjectMembers().getMembers());

        return l.getName().equals(r.getName()) &&
                l.getDescription().equals(r.getDescription()) &&
                l.getDisplayName().equals(r.getDisplayName()) &&
                l.getCreationTime() == r.getCreationTime() &&
                l.getConfigurationModified() == r.getConfigurationModified() &&
                l.getTotalMembers() == r.getTotalMembers() &&
                l.getTotalFiles() == r.getTotalFiles() &&
                l.isOwnedByMe() == r.isOwnedByMe() &&
                l.getTrialEndTime() == r.getTrialEndTime() &&
                l.getExpiry().equals(r.getExpiry()) &&
                l.getWatermark().equals(r.getWatermark()) &&
                ownerEquals(l.getOwner(), r.getOwner());
    }

    private boolean ownerEquals(IOwner lo,
                                ListProjectItemResult.ResultsBean.DetailBean.OwnerBean ro) {
        if (lo == null || ro == null) {
            return false;
        }
        return lo.getUserId() == ro.getUserId() &&
                lo.getEmail().equals(ro.getEmail()) &&
                lo.getName().equals(ro.getName());
    }

    private boolean filterOutMemberModified(int projectTBPK, List<IDBProjectItem.IMember> lMembers,
                                            List<ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean.MembersBean> rMembers) {
        DBProvider dbProvider = SkyDRMApp.getInstance().getDBProvider();
        if (rMembers == null || rMembers.size() == 0) {
            if (lMembers == null || lMembers.size() == 0) {
                return false;
            }
            int deleteAffected = 0;
            for (IDBProjectItem.IMember lm : lMembers) {
                dbProvider.deleteProjectMemberItem(lm.getProjectMemberTBPK());
                deleteAffected++;
            }
            return deleteAffected > 0;
        }
        int upsertAffected = 0;
        for (ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean.MembersBean rm : rMembers) {
            if (rm == null) {
                continue;
            }
            if (contains(lMembers, rm)) {
                continue;
            }
            dbProvider.upsertProjectMemberItem(projectTBPK, rm.getUserId(),
                    rm.getDisplayName(), rm.getEmail(), rm.getCreationTime());
            upsertAffected++;
        }
        if (upsertAffected > 0) {
            return true;
        }

        int deleteAffected = 0;
        for (IDBProjectItem.IMember lm : lMembers) {
            if (lm == null) {
                continue;
            }
            if (contains(rMembers, lm)) {
                continue;
            }
            dbProvider.deleteProjectMemberItem(lm.getProjectMemberTBPK());
            deleteAffected++;
        }
        return deleteAffected > 0;
    }

    private boolean contains(List<IDBProjectItem.IMember> lMembers,
                             ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean.MembersBean rm) {
        if (lMembers == null || lMembers.size() == 0) {
            return false;
        }
        if (rm == null) {
            return false;
        }
        for (IDBProjectItem.IMember lm : lMembers) {
            if (memberEquals(lm, rm)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(List<ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean.MembersBean> rMembers,
                             IDBProjectItem.IMember lm) {
        if (rMembers == null || rMembers.size() == 0) {
            return false;
        }
        if (lm == null) {
            return false;
        }
        for (ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean.MembersBean rm : rMembers) {
            if (memberEquals(lm, rm)) {
                return true;
            }
        }
        return false;
    }

    private boolean memberEquals(IDBProjectItem.IMember lm,
                                 ListProjectItemResult.ResultsBean.DetailBean.ProjectMembersBean.MembersBean rm) {
        if (lm == null) {
            return false;
        }
        if (rm == null) {
            return false;
        }
        return lm.getUserId() == rm.getUserId() &&
                lm.getCreationTime() == rm.getCreationTime() &&
                lm.getDisplayName().equals(rm.getDisplayName()) &&
                lm.getEmail().equals(rm.getEmail());
    }

    private String getOwnerRawJson(ListProjectItemResult.ResultsBean.DetailBean.OwnerBean owner) {
        String ret = "{}";
        if (owner == null) {
            return ret;
        }
        return Owner.generateRawJson(owner.getUserId(),
                owner.getName(), owner.getEmail());
    }

    private List<ListProjectItemResult.ResultsBean.DetailBean> syncAllProject(int type)
            throws RmsRestAPIException, SessionInvalidException, InvalidRMClientException {
        long start = logWithTime("syncAllProject method start invoke.");
        try {
            List<ListProjectItemResult.ResultsBean.DetailBean> ret = new ArrayList<>();
            //Sync all owned by me.
            if (type == 0 || type == 1) {
                List<ListProjectItemResult.ResultsBean.DetailBean> ownedByMeResult =
                        parseResult(syncProjectInternal(true));
                if (ownedByMeResult != null && ownedByMeResult.size() != 0) {
                    ret.addAll(ownedByMeResult);
                }
            }

            //Sync all owned by others.
            if (type == 0 || type == 2) {
                List<ListProjectItemResult.ResultsBean.DetailBean> ownedByOtherResult =
                        parseResult(syncProjectInternal(false));
                if (ownedByOtherResult != null && ownedByOtherResult.size() != 0) {
                    ret.addAll(ownedByOtherResult);
                }
            }
            return ret;
        } finally {
            long end = logWithTime("syncAllProject end ivk.");
            logWithCostTime("syncAllProject", end - start);
        }
    }

    private ListProjectItemResult syncProjectInternal(boolean ownedByMe)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        return session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .listProject(-1, -1, "-lastActionTime", ownedByMe);
    }

    private List<ListProjectItemResult.ResultsBean.DetailBean> parseResult(ListProjectItemResult result) {
        List<ListProjectItemResult.ResultsBean.DetailBean> ret = new ArrayList<>();
        if (result == null) {
            return ret;
        }
        ListProjectItemResult.ResultsBean rb = result.getResults();
        if (rb == null) {
            return ret;
        }
        List<ListProjectItemResult.ResultsBean.DetailBean> db = rb.getDetail();
        if (db == null) {
            return ret;
        }
        ret.addAll(db);
        return ret;
    }

    private List<IDBProjectItem> listProjectInternal(int type) {
        List<IDBProjectItem> ret = new ArrayList<>();
        List<IDBProjectItem> idbProjectItems = SkyDRMApp.getInstance()
                .getDBProvider()
                .queryAllProjectItem();
        if (idbProjectItems == null || idbProjectItems.size() == 0) {
            return ret;
        }
        if (type == 0) {
            ret.addAll(idbProjectItems);
        } else if (type == 1) {
            for (IDBProjectItem i : idbProjectItems) {
                if (i.isOwnedByMe()) {
                    ret.add(i);
                }
            }
        } else if (type == 2) {
            for (IDBProjectItem i : idbProjectItems) {
                if (!i.isOwnedByMe()) {
                    ret.add(i);
                }
            }
        }
        return ret;
    }

    @Override
    public void onHeatBeat(IHeartBeatListener l) {
        if (l != null) {
            mListener = l;
        }
        IHeartBeatPolicy p = HeartbeatPolicyGenerator.
                getOne(HeartbeatPolicyGenerator.TYPE_PROJECT);
        if (p == null) {
            return;
        }
        paddingAllProjectLists();
        int type = p.getType();

        try {
            long start = logWithTime("Start heartbeat.");
            if (mListener != null) {
                mListener.onTaskBegin();
            }
            List<IProject> all = syncProject(0);
            if (mListener != null) {
                mListener.onTaskFinish();
            }
            if ((type == IHeartBeatPolicy.TYPE_NEW_USER_LOGIN) & mEmptyData.get()) {
                if (all.size() == 0) {
                    if (mListener != null) {
                        mListener.onTaskFinish();
                    }
                    mEmptyData.set(true);
                } else {
                    mEmptyData.set(false);
                    //Crawl all project data.
                    mConcurrent.set(true);
                    mTotal = all.size();
                    mTaskQueue.addAll(all);
                    startCrawl(3);
                }
            } else {
                mConcurrent.set(false);
                List<IProject> recent = listRecent();
                if (recent == null || recent.size() == 0) {
                    List<IProject> recentProjects = findHeartBeatProject(all);
                    if (recentProjects == null || recentProjects.size() == 0) {
                        if (mListener != null) {
                            mListener.onTaskFinish();
                        }
                        return;
                    }
                    mTaskQueue.addAll(recentProjects);
                    mTotal = mTaskQueue.size();
                    startCrawl(mTotal);
                } else {
                    mTaskQueue.addAll(recent);
                    mTotal = recent.size();
                    startCrawl(mTotal);
                }
            }

            long end = logWithTime("end heartbeat.");
            logWithCostTime("syncAllProject in heartbeat:", end - start);
        } catch (RmsRestAPIException e) {
            e.printStackTrace();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearCache() {
        List<IProject> projects = listProject(0);
        if (projects == null || projects.size() == 0) {
            return;
        }
        for (IProject p : projects) {
            if (p == null) {
                continue;
            }
            p.clearCache();
        }
    }

    @Override
    public long getCacheSize() {
        long ret = 0;
        //Type 0 means project owned by me& invite by other both.
        List<IProject> projects = listProject(0);
        if (projects == null || projects.size() == 0) {
            return ret;
        }
        for (IProject p : projects) {
            if (p == null) {
                continue;
            }
            ret += p.getCacheSize();
        }
        return ret;
    }

    @Override
    public void updateResetAllOperationStatus() {
        List<IProject> projects = listProject(0);
        if (projects == null || projects.size() == 0) {
            return;
        }
        for (IProject p : projects) {
            if (p == null) {
                continue;
            }
            p.updateResetAllOperationStatus();
        }
    }

    private List<IProject> findHeartBeatProject(List<IProject> all) {
        List<IProject> ret = new ArrayList<>();
        if (all == null || all.size() == 0) {
            return ret;
        }
        List<IProject> ownerByMe = new ArrayList<>();
        List<IProject> invitedByOther = new ArrayList<>();
        int ownerCount = 0;
        int otherCount = 0;
        for (IProject p : all) {
            if (p.isOwnedByMe()) {
                if (++ownerCount > 2) {
                    continue;
                }
                ownerByMe.add(p);
            } else {
                if (++otherCount > 2) {
                    continue;
                }
                invitedByOther.add(p);
            }
        }
        ret.addAll(ownerByMe);
        ret.addAll(invitedByOther);
        return ret;
    }

    private List<IProject> listRecent() {
        return adapt2ProjectItem(SkyDRMApp.getInstance().getDBProvider().queryRecentProjectItem());
    }

    private List<AllProjectsResult.ResultsBean.DetailBean> syncAllProjectLists()
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        AllProjectsResult allProjectsResult = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .listAllProjects();
        List<AllProjectsResult.ResultsBean.DetailBean> ret = new ArrayList<>();
        if (allProjectsResult == null) {
            return ret;
        }
        AllProjectsResult.ResultsBean results = allProjectsResult.getResults();
        if (results == null) {
            return ret;
        }
        List<AllProjectsResult.ResultsBean.DetailBean> detail = results.getDetail();
        if (detail == null || detail.isEmpty()) {
            return ret;
        }
        ret.addAll(detail);
        return ret;
    }

    private void paddingAllProjectLists() {
        try {
            List<AllProjectsResult.ResultsBean.DetailBean> detailBeans = syncAllProjectLists();
            mAllProjectsTmpData.clear();
            if (detailBeans != null) {
                mAllProjectsTmpData.addAll(detailBeans);
            }
        } catch (SessionInvalidException
                | InvalidRMClientException
                | RmsRestAPIException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskBegin() {

    }

    @Override
    public void onTaskProgress(int progress) {

    }

    @Override
    public void onTaskFinish() {
        int current = mFinishCount.addAndGet(1);
        if (current == mTotal) {
            if (inUserLoginMode()) {
                HeartbeatPolicyGenerator.configureOne(HeartbeatPolicyGenerator.TYPE_PROJECT,
                        new ProjectHeartbeatPolicy(IHeartBeatPolicy.TYPE_USER_RECOVER, 120));
                HeartbeatPolicyGenerator.configureOne(HeartbeatPolicyGenerator.TYPE_COMMON,
                        new CommonPolicy(IHeartBeatPolicy.TYPE_USER_RECOVER, 120));
            }
            if (mListener != null) {
                mListener.onTaskFinish();
            }
            mFinishCount.set(0);
            mTotal = 0;
        } else {
            if (mListener != null) {
                int progress = (int) ((current * 1.0 / mTotal) * 100);
                mListener.onTaskProgress(progress);
            }
        }
        if (mConcurrent.get()) {
            startCrawl(1);
        }
    }

    @Override
    public void onTaskFailed(Exception e) {

    }

    private void startCrawl(int count) {
        if (mTaskQueue.size() == 0) {
            return;
        }
        Log.d("HeartBeat", "startCrawl() called with: mTaskQueue size = [" + mTaskQueue.size() + "]");
        Log.d("HeartBeat", "startCrawl() called with: count = [" + count + "]");
        int max = count;
        while (max-- != 0) {
            IProject one = mTaskQueue.poll();
            if (one == null) {
                return;
            }
            if (mConcurrent.get()) {
                run(one);
            } else {
                one.onHeartBeat(this);
            }
        }
    }

    private void run(final IProject p) {
        if (p == null) {
            return;
        }
        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI)
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        p.onHeartBeat(ProjectRepo.this);
                    }
                });
    }

    private void runWithLowerPriority(final IProject p) {
        if (p == null) {
            return;
        }
        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.REGULAR_BACK_GROUND)
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        paddingAllProjectLists();
                        p.onHeartBeat(null);
                    }
                });
    }

    private void runWithLowerPrioritySyncClassification(final IProject p) {
        if (p == null) {
            return;
        }
        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.REGULAR_BACK_GROUND)
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        p.updatePartial();
                    }
                });
    }

    private boolean inUserLoginMode() {
        return HeartbeatPolicyGenerator.getOne(HeartbeatPolicyGenerator.TYPE_COMMON).getType() ==
                IHeartBeatPolicy.TYPE_NEW_USER_LOGIN;
    }

    @Override
    public void onReleaseResource() {
        if (mListener != null) {
            mListener = null;
        }
    }

    private long logWithTime(String msg) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        long timeInMillis = c.getTimeInMillis();
        mLog.i(String.format("%s [%s]", msg, c.getTime().toString()));
        return timeInMillis;
    }

    private void logWithCostTime(String msg, long time) {
        mLog.i(String.format(Locale.getDefault(), "%s cost time [%d]", msg, time));
    }
}
