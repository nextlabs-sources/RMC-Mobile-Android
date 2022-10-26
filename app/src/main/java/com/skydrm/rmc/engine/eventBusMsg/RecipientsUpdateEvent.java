package com.skydrm.rmc.engine.eventBusMsg;

import java.util.List;

/**
 * Created by hhu on 5/12/2017.
 */

public class RecipientsUpdateEvent {
    private List<String> newAddedEmails;
    private List<String> removedEmails;
    private boolean removed;

    public RecipientsUpdateEvent(List<String> newAddEmails, List<String> removedEmails, boolean removed) {
        this.newAddedEmails = newAddEmails;
        this.removedEmails = removedEmails;
        this.removed = removed;
    }

    public List<String> getRemovedEmails() {
        return removedEmails;
    }

    public boolean isRemoved() {
        return removed;
    }

    public List<String> getNewAddedEmails() {
        return newAddedEmails;
    }
}
