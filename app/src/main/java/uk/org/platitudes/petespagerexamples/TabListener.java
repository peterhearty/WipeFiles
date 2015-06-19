package uk.org.platitudes.petespagerexamples;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;

/**
 * Created by pete on 30/04/15.
 */
public class TabListener implements ActionBar.TabListener  {

    ViewPager mViewPager;
    FragmentPagerAdapter mPager;

    public TabListener(ViewPager vp, FragmentPagerAdapter fpa) {
        mViewPager = vp;
        mPager = fpa;
    }

    private static int counter = 1;

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        int p = tab.getPosition();
        mViewPager.setCurrentItem(p);

        // PH - update the text.
        Fragment f = mPager.getItem(p);
        if (f != null) {
            View v = f.getView();
            if (v != null) {    // tab can get selected when view hasn't been constructed yet
                v.invalidate();
            }
            // TODO - move the button check and other common stuff to a common superclass.
            if (f instanceof SelectFilesFragment) {
                SelectFilesFragment sff = (SelectFilesFragment) f;
                if (sff.mControlButtonHandler != null)
                    sff.mControlButtonHandler.checkButtonStatus();
            }
            if (f instanceof DeleteFilesFragment) {
                DeleteFilesFragment dff = (DeleteFilesFragment) f;
                if (dff.mControlButtonHandler != null)
                    dff.mControlButtonHandler.checkButtonStatus();
            }
        }

    }


    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


}
