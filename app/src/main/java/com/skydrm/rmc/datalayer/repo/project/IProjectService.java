package com.skydrm.rmc.datalayer.repo.project;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.util.List;

public interface IProjectService {
    List<IProject> listProject(int type);

    List<IProject> syncProject(int type)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;

    IProject createProject(String name, String description,
                          List<String> emails, String invitationMsg)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException;
}
