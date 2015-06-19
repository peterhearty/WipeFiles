package uk.org.platitudes.petespagerexamples;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

/**
 * Invoked by DeleteFilesFragment when user clicks the "Delete" button.
 *
 * Created by pete on 17/05/15.
 */
public class LastChanceDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private Activity deleteFilesFragment;

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