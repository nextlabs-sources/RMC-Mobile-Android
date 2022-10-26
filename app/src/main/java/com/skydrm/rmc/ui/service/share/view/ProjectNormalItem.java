package com.skydrm.rmc.ui.service.share.view;

import com.skydrm.rmc.datalayer.repo.project.IProject;

public class ProjectNormalItem {
    private IProject project;
    private boolean selected;

    ProjectNormalItem(IProject p, boolean selected) {
        this.project = p;
        this.selected = selected;
    }

    public IProject getProject() {
        return project;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
