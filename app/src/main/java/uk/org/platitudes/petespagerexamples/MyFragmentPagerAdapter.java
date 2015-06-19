package uk.org.platitudes.petespagerexamples;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;
import java.util.Locale;

/**
 * Returns instances of SelectFilesFrgament or DeleteFilesFragment to the FragmentManager.
 *
 * Created by pete on 30/04/15.
 */
public class MyFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

    SelectFilesFragment selectFilesFragment;
    DeleteFilesFragment deleteFilesFragment;
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
            case 0: return mActivity.getString(R.string.title_section1);
            case 1: return mActivity.getString(R.string.title_section2);
        }
        return null;
    }

}
