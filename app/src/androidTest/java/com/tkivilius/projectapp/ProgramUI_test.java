package com.tkivilius.projectapp;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class ProgramUI_test {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(
            MainActivity.class);

    /**
     * Tests a complete scenario from main screen to deletion of data
     * Course of actions:
     * Open drawer -> click Add
     * Enter title and description, scroll down to save, click
     * Go back to List, click Testing button
     * Compare the the text in new intent with what was inputted
     * Go back, open action bar overflow, click delete all data
     */
    @Test
    public void completeScenario() {
        // Open Drawer
        onView(withId(R.id.mainDrawer)).perform(DrawerActions.open());
        onView(withText("Add")).perform(click());

        // Input data
        onView(withId(R.id.inputTitle)).perform(typeText("Testing"));
        onView(withId(R.id.inputDescription)).perform(typeText("Testing description"), closeSoftKeyboard());
        onView(withId(R.id.buttonSave)).perform(scrollTo()).perform(click());

        // Go back to list
        onView(withId(R.id.mainDrawer)).perform(DrawerActions.open());
        onView(withText("List")).perform(click());

        // Click on new Created item
        onView(withText("Testing")).perform(click());
        onView(withId(R.id.detailTitle)).check(matches(withText("Testing")));
        onView(withId(R.id.detailDescription)).check(matches(withText("Testing description")));

        // Press back button
        Espresso.pressBack();

        // Open toolbar overflow option and Delete all data, Clean up after test
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText("Delete all data")).perform(click());
    }


}

