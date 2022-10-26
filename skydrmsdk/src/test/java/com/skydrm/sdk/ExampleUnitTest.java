package com.skydrm.sdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void string_test() throws Exception{
        String rmsURL ="https://rmtest.nextlabs.solutions/rms";
        // rectify
        if (rmsURL.endsWith("/rms")) {
            rmsURL = rmsURL.substring(0, rmsURL.length() - 4);
        }

        assertNotNull(rmsURL);




        String filename = "attachment; filename*=UTF-8''aaatest-2017-06-08-03-07-44.jpg.nxl";
        try{
            String KEY_WORD="UTF-8''";
            filename=filename.substring(filename.lastIndexOf("'")+1);
        }catch (Exception e){
            filename="unkonwn";
            e.printStackTrace();
        }

        filename="";

    }

}