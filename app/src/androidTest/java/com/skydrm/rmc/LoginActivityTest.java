package com.skydrm.rmc;

import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.activity.splash.WelcomeActivity;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.workspace.ListFileParam;
import com.skydrm.sdk.rms.rest.workspace.ListFileResult;
import com.skydrm.sdk.rms.user.IRmUser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webKeys;


@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {
    private static final String LOGIN_URL = "rms-rhel74.qapf1.qalab01.nextlabs.com:8443";
    private static final String USER_NAME = "john.tyler";
    private static final String PASSWORD = "john.tyler";

    @Rule
    public final ActivityTestRule<WelcomeActivity> mWRule =
            new ActivityTestRule<>(WelcomeActivity.class);

    @Test
    public void testLogin() {
        // WelcomeActivity click login button.
        onView(withId(R.id.login)).perform(click());

        // ServerTypeSelectActivity
        onView(withId(R.id.ll_company_account_group)).perform(click());

        //
        try {
            // If rms-url input editText exists, then input text and click next to login page.
            onView(withId(R.id.textInputLayout))
                    .check(matches(isDisplayed()));

            onView(withId(R.id.et_url_input))
                    .perform(typeText(LOGIN_URL))
                    .perform(closeSoftKeyboard());

            onView(withId(R.id.bt_next)).perform(click());

            performWebViewAction();

        } catch (Exception e) {
            onView(withId(R.id.bt_next)).perform(click());
            performWebViewAction();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                testWorkSpaceAPI();
            }
        }).run();

        //onView(withId(R.id.nav_view)).perform(click());
    }

    private void testWorkSpaceAPI() {
        SkyDRMApp app = SkyDRMApp.getInstance();
        IRmUser rmUser = null;
        try {
            rmUser = app.getSession().getRmUser();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        try {
            ListFileResult result = app.getSession().getRmsRestAPI().getWorkSpaceService(rmUser).listFile(ListFileParam.newOne("/"));

            assert result != null;
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        } catch (RmsRestAPIException e) {
            e.printStackTrace();
        }
    }

    private void performWebViewAction() {
        pauseTestFor(3000);
        onWebView(withId(R.id.login_webView)).forceJavascriptEnabled()
                .withElement(findElement(Locator.ID, "ldap-username"))
                .perform(webKeys(USER_NAME))
                .withElement(findElement(Locator.ID, "ldap-password"))
                .perform(webKeys(PASSWORD))
                .withElement(findElement(Locator.ID, "ldap-login-btn"))
                .perform(webClick());

        pauseTestFor(5000);

    }

    private void pauseTestFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
