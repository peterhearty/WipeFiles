/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.wipe.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import uk.org.platitudes.wipe.main.MainTabActivity;

/**
 * Forces users to accept responsibility for using this app.
 */
public class WarningDialog extends DialogFragment implements DialogInterface.OnClickListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("WARNING - this app makes irreversible changes to your device. Under no circumstances will the authors be responsible for any loss or damage. You use this app entirely at your own risk.");
        builder.setPositiveButton("Accept", this);
        builder.setNegativeButton("Decline", this);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which == DialogInterface.BUTTON_POSITIVE) {
            // do nothing
        } else {
            MainTabActivity.sTheMainActivity.finish();
        }


    }
}
