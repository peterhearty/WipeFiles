package uk.org.platitudes.wipe.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import uk.org.platitudes.petespagerexamples.R;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

    public static final String TEXT_SIZE_KEY = "text_size_key";
    public static final String LOG_SIZE_KEY = "log_text_size_key";
    public static final String HIDE_TABS_KEY = "hide_tabs_checkbox";

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new PrefChangeListener();


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     *
     * THIS SHOWS ALL THE PREFERENCES AS A SINGLE SCREEN.
     */
    private void setupSimplePreferencesScreen() {
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.preferences);


        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("test_mode_sleep_time_key"));
        bindPreferenceSummaryToValue(findPreference(TEXT_SIZE_KEY));
        bindPreferenceSummaryToValue(findPreference(LOG_SIZE_KEY));
        bindPreferenceSummaryToValue(findPreference("zero_wipe"));
        bindPreferenceSummaryToValue(findPreference("number_passes"));
        bindPreferenceSummaryToValue(findPreference("block_size"));
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        Object value = null;
        String key = preference.getKey();
        if (preference instanceof CheckBoxPreference) {
            value = sp.getBoolean(key, false);
        } else {
            value = sp.getString(preference.getKey(), "");
        }

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,value);
    }

}
