package com.skydrm.sdk.rms;

import com.skydrm.sdk.policy.Expiry;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Date;

/**
 * Created by oye on 11/2/2017.
 */

public class NxlTest {


    @Test
    public void Expiry_Never_test(){

        Expiry e = new Expiry.Builder()
                .never()
                .build();
        System.out.println("never: "+e.toString());
        // as defined , adhoc json is null
        Assert.assertNull(e.toAdHocExpiry());

    }

    @Test
    public void Expiry_Relative_test(){

        Expiry e = new Expiry.Builder()
                .relative()
                .setYear(1)
                .setMonth(1)
                .setWeek(1)
                .setDay(1)
                .build();
        System.out.println("relative: "+e.toString());
        Assert.assertNotNull(e.toAdHocExpiry());
        System.out.println(e.toAdHocExpiry());

    }

    @Test
    public void Expiry_Absolute_test(){
        Expiry e = new Expiry.Builder()
                .absolute()
                .setEndDate(2018,9,21)
                .build();
        System.out.println("absolute: "+e.toString());
        Assert.assertNotNull(e.toAdHocExpiry());
        System.out.println(e.toAdHocExpiry());
    }
    @Test
    public void Expiry_Range_test(){
        Expiry e = new Expiry.Builder()
                .range()
                .setStartDate(new Date().getTime())
                .setEndDate(2018,9,21)
                .build();
        System.out.println("range: "+e.toString());
        Assert.assertNotNull(e.toAdHocExpiry());
        System.out.println(e.toAdHocExpiry());
    }
}
