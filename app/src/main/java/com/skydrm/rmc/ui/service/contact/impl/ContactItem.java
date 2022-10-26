package com.skydrm.rmc.ui.service.contact.impl;

public class ContactItem {
    private String title;
    private Contact contact;

    public ContactItem(String title, Contact contact) {
        this.title = title;
        this.contact = contact;
    }

    public String getTitle() {
        return title;
    }

    public Contact getContact() {
        return contact;
    }
}
