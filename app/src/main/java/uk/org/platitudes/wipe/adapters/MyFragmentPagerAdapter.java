/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.wipe.adapters;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;
import java.util.Locale;

import uk.org.platitudes.wipe.main.DeleteFilesFragment;
import uk.org.platitudes.wipe.main.SelectFilesFragment;

/**
 * Returns instances of SelectFilesFrgament or DeleteFilesFragment to the FragmentManager.
 */
public class MyFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

    public SelectFilesFragment selectFilesFragment;
    public DeleteFilesFragment deleteFilesFragment;
    Activity mActivity;

    public MyFragmentPagerAdapter(FragmentManager fm, Activity a) {
        super(fm);
        List<Fragment> fragmentList = fm.getFragments();
        if (fragmentList == null) {
            // This is the first time the app has been created. Create our own fragments.
            selectFilesFragment = new SelectFilesFragment();
            deleteFilesFragment = new DeleteFilesFragment();
        } else {
            // Fragments already recreated by android on restart
            for (Fragment f : fragmentList) {
                if (f instanceof SelectFilesFragment) {
                    selectFilesFragment = (SelectFilesFragment) f;
                } else {
                    deleteFilesFragment = (DeleteFilesFragment) f;
                }
            }
        }
        mActivity = a;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        // position starts at zero.
        Fragment result = selectFilesFragment;
        if (position == 1) result = deleteFilesFragment;
        return result;
//            return PlaceholderFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0: return "Select files";
            case 1: return "Delete list";
        }
        return null;
    }

}
