package com.skydrm.rmc.domain;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

public class UserAccount implements Serializable {
    private static final long serialVersionUID = -2358912029037065688L;
    private String accountType;
    private List<Item> accountItems;

    public UserAccount(String accountType, List<Item> accountItems) {
        this.accountType = accountType;
        this.accountItems = accountItems;
    }

    public String getAccountType() {
        return accountType;
    }

    public List<Item> getAccountItems() {
        return accountItems;
    }

    public static class Item implements Serializable {
        private static final long serialVersionUID = 928274266434599904L;
        private boolean selected;
        private String account;

        public Item(boolean selected, String account) {
            this.selected = selected;
            this.account = account;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public boolean isSelected() {
            return selected;
        }

        public String getAccount() {
            return account;
        }
    }

    public boolean contains(String url) {
        if (accountItems == null || accountItems.size() == 0) {
            return false;
        }
        for (Item item : accountItems) {
            if (TextUtils.equals(item.getAccount(), url)) {
                return true;
            }
        }
        return false;
    }
}
