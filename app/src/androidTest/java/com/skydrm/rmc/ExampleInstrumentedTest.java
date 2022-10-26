package com.skydrm.rmc;

import android.support.test.runner.AndroidJUnit4;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.Factory;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.user.IRmUser;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

/**
 * Created by jrzhou on 8/3/2017.
 */

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static RestAPI api;
    private static Config config;
    private static IRmUser rmUser;


    @BeforeClass
    public static void setUp() throws Exception {
        // use debugging  server
        config = new Config("https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms");
//        config = new Config(Factory.RM_SERVER_RELEASE);
        Factory.DEVICE_ID = "awifi02:00:00:00:00:00";
        Factory.DEVICE_TYPE = "806";
        Factory.DEVICE_NAME = "Android SDK built for x86";
        Factory.CLIENT_ID = "EA8112FC5A975E40C96E61EEEC80C339";
        // ignore SSL handshake for debugging server
        api = new RestAPI(config);
    }

    @Test
    public void test_login() throws Exception {

    }
}
