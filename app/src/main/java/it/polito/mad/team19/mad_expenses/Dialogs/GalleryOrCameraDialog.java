package it.polito.mad.team19.mad_expenses.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by Jured on 21/04/17.
 */

public class GalleryOrCameraDialog extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogCameraClick(DialogFragment dialog);
        public void onDialogGalleryClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = new Activity();

        if(context instanceof Activity)
            activity = (Activity) context;
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_camera_or_gallery)
                .setItems(R.array.image_source_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                //
                                switch (which) {
                                    case 0:
                                        mListener.onDialogGalleryClick(GalleryOrCameraDialog.this);
                                        break;
                                    case 1:
                                        mListener.onDialogCameraClick(GalleryOrCameraDialog.this);
                                        break;
                                }
                            }
                });



        // Create the AlertDialog object and return it
        return builder.create();
    }
}
