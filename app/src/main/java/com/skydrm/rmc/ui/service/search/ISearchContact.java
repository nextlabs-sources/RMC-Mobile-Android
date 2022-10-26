package com.skydrm.rmc.ui.service.search;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.ui.common.NxlFileItem;
import com.skydrm.rmc.ui.service.favorite.model.bean.FavoriteItem;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.IVaultFileLog;
import com.skydrm.rmc.ui.project.feature.member.MemberItem;

import java.util.List;

public interface ISearchContact {
    interface IView {
        void onInitialize(boolean active);

        void showEmptyView(boolean active);

        void showNoSearchResultView(boolean active);

        void updateNxlItem(List<NxlFileItem> data);

        void updateFavItem(List<FavoriteItem> data);

        void updateRepoItem(List<NXFileItem> data);

        void updateMemberItem(List<MemberItem> data);

        void updateLogItem(List<IVaultFileLog> data);

        void updateProjectItem(List<IProject> data);
    }

    interface IPresenter extends IDestroyable {
        void initialize(String action);

        void searchByName(String filter);

        void deleteProjectFile(INxlFile file);

        void deleteMyVaultFile(INxlFile file, String vaultType);

        void deleteMyDriveFile(INxFile file);

        void updateProjectMemberItem(IMember target);
    }
}
