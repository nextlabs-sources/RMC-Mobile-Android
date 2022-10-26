package com.skydrm.rmc.ui.base;

public abstract class BaseHandler<Params extends IBaseHandlerRequest, Exception extends BaseException> {
    protected BaseHandler mSuccessor;

    public void setSuccessor(BaseHandler successor) {
        this.mSuccessor = successor;
    }

    public abstract void handleRequest(Params p) throws Exception;
}
