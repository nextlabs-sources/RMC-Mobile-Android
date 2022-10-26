package com.skydrm.rmc.utils.commonUtils;

import com.skydrm.sdk.rms.rest.user.User;

public class ExpiryChecker {
    private long sStdCurMills = System.currentTimeMillis();

    public boolean isValidate(User.IExpiry expiry) {
        return expiry != null && buildExpiry(expiry).isValidate();
    }

    public boolean isValidate(long startDate, long endDate) {
        if (startDate == 0) {
            //never
            return endDate == 0 || sStdCurMills <= endDate;
        } else {
            return sStdCurMills <= endDate;
        }
    }

    private IExpiry buildExpiry(User.IExpiry expiry) {
        int option = expiry.getOption();
        if (option == 0) {
            return new Never();
        } else if (option == 1) {
            return new Relative((User.IRelative) expiry);
        } else if (option == 2) {
            return new Absolute((User.IAbsolute) expiry);
        } else {
            return new Range((User.IRange) expiry);
        }
    }

    interface IExpiry {
        boolean isValidate();
    }

    class Never implements IExpiry {

        @Override
        public boolean isValidate() {
            return true;
        }
    }

    class Relative implements IExpiry {
        User.IRelative mRelative;

        Relative(User.IRelative relative) {
            this.mRelative = relative;
        }

        @Override
        public boolean isValidate() {
            return true;
        }
    }

    class Absolute implements IExpiry {
        User.IAbsolute mAbsolute;

        Absolute(User.IAbsolute absolute) {
            this.mAbsolute = absolute;
        }

        @Override
        public boolean isValidate() {
            return sStdCurMills <= mAbsolute.endDate();
        }
    }

    class Range implements IExpiry {
        User.IRange mRange;

        Range(User.IRange range) {
            this.mRange = range;
        }

        @Override
        public boolean isValidate() {
            return sStdCurMills <= mRange.endDate();
        }
    }
}
