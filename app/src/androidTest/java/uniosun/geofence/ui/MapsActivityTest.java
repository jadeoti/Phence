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
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MapsActivityTest {

    @Rule
    public ActivityTestRule<MapsActivity> mActivityTestRule = new ActivityTestRule<>(MapsActivity.class);

    @Test
    public void mapsActivityTest() {
        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab), isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.field_title), isDisplayed()));
        appCompatEditText.perform(click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.field_title), isDisplayed()));
        appCompatEditText2.perform(replaceText("Hello"));

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.field_latitude), isDisplayed()));
        appCompatEditText3.perform(replaceText("6.644010"));

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.field_longitude), isDisplayed()));
        appCompatEditText4.perform(replaceText("3.360248"));

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.field_radius), isDisplayed()));
        appCompatEditText5.perform(replaceText("100"));

        //pressBack();

        ViewInteraction floatingActionButton2 = onView(
                allOf(withId(R.id.fab_submit_post), isDisplayed()));
        floatingActionButton2.perform(click());

    }
}
