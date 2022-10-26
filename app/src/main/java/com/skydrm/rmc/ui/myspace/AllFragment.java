package com.skydrm.rmc.ui.myspace;

import com.skydrm.rmc.engine.eventBusMsg.RepositorySelectEvent;
import com.skydrm.rmc.engine.eventBusMsg.RepositorySelectMyDrive;
import com.skydrm.rmc.engine.eventBusMsg.RepositoryUpdateEvent;
import com.skydrm.rmc.ui.fragment.AllPresenter;
import com.skydrm.rmc.ui.myspace.base.MySpaceFileBaseFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class AllFragment extends MySpaceFileBaseFragment {

    public static AllFragment newInstance() {
        return new AllFragment();
    }

    @Override
    protected void initViewAndEvents() {
        super.initViewAndEvents();
        mPresenter = new AllPresenter(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveRepositoryUpdateEvent(RepositoryUpdateEvent event) {
        if (mPresenter != null) {
            mPresenter.refreshRepo();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveRepositoryUpdateEvent(RepositorySelectMyDrive event) {
        if (mPresenter != null) {
            mPresenter.refreshRepo();
        }
    }

    /**
     * user change the bound service available to user.
     *
     * @param event {@link RepositorySelectEvent}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void selectRepository(RepositorySelectEvent event) {
        if (mPresenter != null) {
            mPresenter.refreshRepo();
        }
    }
}
