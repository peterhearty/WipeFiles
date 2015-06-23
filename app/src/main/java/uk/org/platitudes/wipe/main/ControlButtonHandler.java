/**
 * This source code is not owned by anybody. You can can do what you like with it.
 *
 * @author  Peter Hearty
 * @date    April 2015
 */
package uk.org.platitudes.wipe.main;

import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageButton;

import uk.org.platitudes.petespagerexamples.R;
import uk.org.platitudes.wipe.main.MainTabActivity;

/**
 * The DeleteFilesFragment and the SelectFilesFragment both have a small arrow button
 * to show or hide the ActionBar. This class holds the button and handles clicks on it.
 *
 * Layout: activity_main.xml
 *
 */
public class ControlButtonHandler implements View.OnClickListener{

    /**
     * The actual button.
     */
    private ImageButton mImageButton;

    public ControlButtonHandler (View parent) {
        View v = parent.findViewById(R.id.controls_button);
        if (v != null) {
            mImageButton = (ImageButton) v;
            v.setOnClickListener(this);
            ActionBar ab = MainTabActivity.sTheMainActivity.mActionBar;
            if (!ab.isShowing()) {
                // Action bar is hidden, change default icon to down.
                mImageButton.setImageResource(R.drawable.arrow_down);
            }

        }
    }

    @Override
    public void onClick(View v) {
        // Called when the "Action bar" button is clicked
        ActionBar ab = MainTabActivity.sTheMainActivity.mActionBar;
        if (ab.isShowing()) {
            ab.hide();
            mImageButton.setImageResource(R.drawable.arrow_down);
        } else {
            ab.show();
            mImageButton.setImageResource(R.drawable.arrow_up);
        }
        // N.B. We can't use checkButtonStatus () to change the icon as isShowing() may not report correctly yet.
    }

    /**
     * Gets called by TabListener when a tab is selected to make sure the
     * arrow on the button points in the right direction.
     */
    public void checkButtonStatus () {
        ActionBar ab = MainTabActivity.sTheMainActivity.mActionBar;
        if (ab.isShowing()) {
            mImageButton.setImageResource(R.drawable.arrow_up);
        } else {
            mImageButton.setImageResource(R.drawable.arrow_down);
        }
    }

    /**
     * Both SelectFilesFragment and DeleteFilesFragment implement this.
     * It allows the TabListener to access the button state on both fragments.
     */
    public interface GetControlButtonHandler {
        public ControlButtonHandler getControlButtonHandler();
    }
}

