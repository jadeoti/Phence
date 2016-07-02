package uniosun.geofence.ui;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import uniosun.geofence.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SignInActivityTest2 {

    @Rule
    public ActivityTestRule<SignInActivity> mActivityTestRule = new ActivityTestRule<>(SignInActivity.class);

    @Test
    public void signInActivityTest2() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.field_email),
                        withParent(withId(R.id.layout_email_password)),
                        isDisplayed()));
        appCompatEditText.perform(click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.field_email),
                        withParent(withId(R.id.layout_email_password)),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("ade@d.com"));

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.field_password),
                        withParent(withId(R.id.layout_email_password)),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("UnderGod11"));

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_sign_up), withText("Sign Up"),
                        withParent(withId(R.id.layout_buttons)),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction appCompatCheckedTextView = onView(
                allOf(withId(R.id.design_menu_item_text), withText("New"), isDisplayed()));
        appCompatCheckedTextView.perform(click());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.field_title), isDisplayed()));
        appCompatEditText4.perform(click());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.field_title), isDisplayed()));
        appCompatEditText5.perform(replaceText("Falomo"));

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.field_latitude), isDisplayed()));
        appCompatEditText6.perform(click());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.field_latitude), isDisplayed()));
        appCompatEditText7.perform(replaceText("6.444509"));

        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.field_longitude), isDisplayed()));
        appCompatEditText8.perform(replaceText("3.426760"));

        ViewInteraction appCompatEditText9 = onView(
                allOf(withId(R.id.field_radius), isDisplayed()));
        appCompatEditText9.perform(replaceText("100"));

        appCompatEditText9.perform(pressImeActionButton());

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab_submit_post), isDisplayed()));
        floatingActionButton.perform(click());

        pressBack();

    }
}
