/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.wipe.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

/**
 * Makes sure the user really wants to exit the app by hitting the back key.
 */
public class ConfirmExitFromBackKey extends DialogFragment implements DialogInterface.OnClickListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Do you want to exit the app?");
        builder.setPositiveButton("Yes", this);
        builder.setNegativeButton("No", this);
        // Create the AlertDialog object and return it
        return builder.create();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which == DialogInterface.BUTTON_POSITIVE) {
            getActivity().finish();
        } else {
            // do nothing.
        }

    }

}
