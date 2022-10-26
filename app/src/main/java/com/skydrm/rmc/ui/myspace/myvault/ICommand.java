package com.skydrm.rmc.ui.myspace.myvault;

/**
 * Created by hhu on 4/28/2018.
 */


public interface ICommand {
    interface ICommandExecuteCallback<Result extends IResult, Message extends IError> {
        void onInvoked(Result result);

        void onFailed(Message message);
    }

    interface IResult {

    }

    interface IError {

    }
}
