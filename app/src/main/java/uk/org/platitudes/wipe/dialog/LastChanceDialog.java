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

import uk.org.platitudes.wipe.main.DeleteFilesFragment;
import uk.org.platitudes.wipe.main.MainTabActivity;

/**
 * Invoked by DeleteFilesFragment when user clicks the "Wipe files" button.
 */
public class LastChanceDialog extends DialogFragment implements DialogInterface.OnClickListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Last chance, wiped files will be lost forever. Are you sure you want to proceed?");
        builder.setPositiveButton("Yes", this);
        builder.setNegativeButton("No", this);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which == DialogInterface.BUTTON_POSITIVE) {
            DeleteFilesFragment dff = MainTabActivity.sTheMainActivity.mSectionsPagerAdapter.deleteFilesFragment;
            dff.startFileDeletion();
        } else {
            Context context = MainTabActivity.sTheMainActivity.getApplicationContext();
            Toast toast = Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}