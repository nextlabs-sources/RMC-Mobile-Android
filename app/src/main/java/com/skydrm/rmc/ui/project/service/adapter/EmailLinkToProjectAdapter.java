package com.skydrm.rmc.ui.project.service.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.Project;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.activity.splash.SplashActivity;
import com.skydrm.rmc.ui.project.ProjectActivity;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.project.GetProjectMetadataResult;

/**
 * Created by jrzhou on 6/15/2017.
 */

@Deprecated
public class EmailLinkToProjectAdapter {
    private String projectId;
    private boolean isCreatedByMe = false;
    private Context context;

    public EmailLinkToProjectAdapter(Context context, String projectId) {
        this.context = context;
        this.projectId = projectId;
    }

    public void toProjectActivity() {
        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                    GetProjectMetadataResult result = session.getRmsRestAPI()
                            .getProjectService(session.getRmUser())
                            .getProjectMetadata(Integer.parseInt(projectId));
                    Project p = new Project();
                    Intent i = new Intent(context, ProjectActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constant.PROJECT_DETAIL, p);
                    bundle.putInt(Constant.PROJECT_INDEX, 1);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtras(bundle);
                    context.startActivity(i);
                    ((SplashActivity) context).finish();
                } catch (RmsRestAPIException e) {
                    e.printStackTrace();
                    ((SplashActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String hintMessage = "";
                                try {
                                    hintMessage = context.getString(R.string.You_are_not_authorized_to_access_this_project);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(context, hintMessage, Toast.LENGTH_SHORT).show();
                                ((SplashActivity) context).finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (SessionInvalidException e) {
                    e.printStackTrace();
                    ((SplashActivity) context).finish();
                } catch (InvalidRMClientException e) {
                    e.printStackTrace();
                    ((SplashActivity) context).finish();
                }
            }
        });
    }
}
