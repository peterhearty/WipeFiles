/**
 * This source code is not owned by anybody. You can can do what you like with it.
 *
 * This is the main entry point for the wipe app. If you want to understand how this
 * app works then this is the place to start. It was created using one of the Android
 * Studio wizards.
 *
 * @author  Peter Hearty
 * @date    2015
 */
package uk.org.platitudes.petespagerexamples;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;


public class MainTabActivity extends ActionBarActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    public MyFragmentPagerAdapter   mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager                mViewPager;
    public ActionBar                mActionBar;
    public TabListener          mTabListener;
    static MainTabActivity sTheMainActivity;
    public int textSize;
    public int logTextSize;
    public ArrayList<String> deleteLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // savedInstanceState is null when app first created.
        // It's non-null if the app is being restored
        // When non-null, the line below creates new PetesFragments. What else does it create?
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sTheMainActivity = this;

        if (savedInstanceState != null) {
            // data from previous incarnation
            deleteLog = savedInstanceState.getStringArrayList("deleteLog");
        } else {
            deleteLog = new ArrayList<>();
        }


        // Set up the action bar. ActionBar has the app title, menus etc.
        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        // Create the adapter that will return a fragment for each of the
        // primary sections of the activity.
        FragmentManager fm = getSupportFragmentManager();
        mSectionsPagerAdapter = new MyFragmentPagerAdapter(fm, this);

        // Set up the ViewPager with the sections adapter.
        // http://developer.android.com/reference/android/support/v4/view/ViewPager.html
        // Layout manager that allows the user to flip left and right through pages of data.
        // You supply an implementation of a PagerAdapter to generate the pages that the view shows.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        View content = findViewById(android.R.id.content);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mActionBar.setSelectedNavigationItem(position);
            }
        });

        mTabListener = new TabListener(mViewPager, mSectionsPagerAdapter);

        // For each of the sections in the app, add a tab to the action bar.x
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            CharSequence pageTitle = mSectionsPagerAdapter.getPageTitle(i);
            ActionBar.Tab newTab = mActionBar.newTab();
            newTab.setText(pageTitle);
            newTab.setTabListener(mTabListener);
            mActionBar.addTab(newTab);
        }

        // Get settings
        setTextSize();
        hideOrShowTabs();
    }

    private void setTextSize () {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String textSizeString = sharedPref.getString("text_size_key", "25");
        textSize = Integer.valueOf(textSizeString);

        textSizeString = sharedPref.getString("log_text_size_key", "15");
        logTextSize = Integer.valueOf(textSizeString);
    }

    private void hideOrShowTabs () {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean hideTabs = sharedPref.getBoolean("hide_tabs_checkbox", false);
        if (hideTabs.booleanValue())
            mActionBar.hide();
        else
            mActionBar.show();

    }

    public void redrawBothLists (String newtextSize) {
        textSize = Integer.valueOf(newtextSize);
        mSectionsPagerAdapter.selectFilesFragment.resetAdapter();
        mSectionsPagerAdapter.deleteFilesFragment.resetAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_petes_tab_example, menu);

        // Totally out of place, but a convenient opportunity to set the app title to the
        // start directory.
        SelectFilesFragment frag = (SelectFilesFragment) mSectionsPagerAdapter.getItem(0);
        frag.setAppTitle();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_exit) {
            finish();
            return true;
        }

        if (id == R.id.action_show_log) {
            Intent intent = new Intent(this, DeletionLogActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Used to save dynamic data, e.g. when the screen is turned round.
        // Different from onPause, which gets called when a process is being put in the background
        // and might not come back.
        // "In general onSaveInstanceState(Bundle) is used to save per-instance state in the
        // activity and this method is used to store global persistent data (in content providers, files, etc.)"
        outState.putStringArrayList("deleteLog", deleteLog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        // onSaveInstanceState (Bundle outState, PersistableBundle outPersistentState) is a separate call with an extra parameter.
        // It is invoked when an app is marked with the attribute persistableMode set to persistAcrossReboots. Such apps get
        // created via  onCreate(Bundle, PersistableBundle).
    }

}