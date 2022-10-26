package com.skydrm.rmc.ui.service.share.view;

import android.content.Context;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IProject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProjectGroupItem {
    private String title;
    private List<ProjectNormalItem> projects;

    private ProjectGroupItem(String title, List<ProjectNormalItem> projects) {
        this.title = title;
        this.projects = projects;
    }

    public String getTitle() {
        return title;
    }

    public List<ProjectNormalItem> getChildren() {
        return projects;
    }

    static ProjectGroupItem newByCreateByMeData(Context ctx, List<IProject> data, List<String> defaultSelected) {
        if (ctx == null || data == null) {
            return null;
        }
        String builder = ctx.getString(R.string.Projects_create_by) + " " +
                ctx.getString(R.string.me) +
                ctx.getString(R.string.Projects_create_by_me_count);
        String title = String.format(Locale.getDefault(), builder, data.size());
        List<ProjectNormalItem> child = new ArrayList<>();
        for (IProject p : data) {
            child.add(new ProjectNormalItem(p, defaultSelected != null
                    && defaultSelected.contains(String.valueOf(p.getId()))));
        }
        return new ProjectGroupItem(title, child);
    }

    static ProjectGroupItem newByInvitedByOtherData(Context ctx, List<IProject> data, List<String> defaultSelected) {
        if (ctx == null || data == null) {
            return null;
        }
        String builder = ctx.getString(R.string.Projects_invited_by) + " " +
                ctx.getString(R.string.others) +
                ctx.getString(R.string.Projects_invited_by_other_count);
        String title = String.format(Locale.getDefault(), builder, data.size());
        List<ProjectNormalItem> child = new ArrayList<>();
        for (IProject p : data) {
            child.add(new ProjectNormalItem(p, defaultSelected != null
                    && defaultSelected.contains(String.valueOf(p.getId()))));
        }
        return new ProjectGroupItem(title, child);
    }
}
