package uk.org.platitudes.wipe.preferences;

import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;

import uk.org.platitudes.wipe.main.MainTabActivity;

/**
 * Created by pete on 25/05/15.
 */
public class PrefChangeListener implements Preference.OnPreferenceChangeListener {

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            if (index >= 0) {
                preference.setSummary(listPreference.getEntries()[index]);
            } else {
                preference.setSummary(null);
            }

            if (listPreference.getKey().equals(SettingsActivity.TEXT_SIZE_KEY)) {
                // The text size has to change so invalidate all data and redraw screen.
                MainTabActivity.sTheMainActivity.redrawBothLists(stringValue);
            }

            if (listPreference.getKey().equals(SettingsActivity.LOG_SIZE_KEY)) {
                MainTabActivity.sTheMainActivity.mLogTextSize = Integer.valueOf(stringValue);
            }

        } else if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkbox = (CheckBoxPreference) preference;
            Boolean booleanValue = (Boolean) value;
            if (checkbox.getKey().equals(SettingsActivity.HIDE_TABS_KEY)) {
                // Don't apply this straight away, only at startup.
//                    if (booleanValue.booleanValue())
//                        MainTabActivity.sTheMainActivity.mActionBar.hide();
//                    else
//                        MainTabActivity.sTheMainActivity.mActionBar.show();
            }


        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

}
