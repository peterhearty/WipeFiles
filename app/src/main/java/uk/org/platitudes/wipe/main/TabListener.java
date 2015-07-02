/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.wipe.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;

import uk.org.platitudes.wipe.main.DeleteFilesFragment;
import uk.org.platitudes.wipe.main.SelectFilesFragment;

/**
 * Switches views when a tab is selected.
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
            // Make sure that the correct image is shown for the show/hide ActionBar button.
            if (f instanceof ControlButtonHandler.GetControlButtonHandler) {
                ControlButtonHandler.GetControlButtonHandler gcbh = (ControlButtonHandler.GetControlButtonHandler) f;
                ControlButtonHandler cbh = gcbh.getControlButtonHandler();
                if (cbh != null) cbh.checkButtonStatus();
            }
        }

    }


    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}


}
