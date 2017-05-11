package it.polito.mad.team19.mad_expenses;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;


public class DeleteMemberDialog extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
   * implement this interface in order to receive event callbacks.
   * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogDeleteMemberClick(DialogFragment dialog);
        public void onDialogLeaveAndDeleteClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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

        Boolean isLastUser = new Boolean(getArguments().getString("usersLeft").compareTo("1") == 0);
        String currentUser = getArguments().getString("currentUid");
        String userToDelete = getArguments().getString("selectedUid");


        //in array_option verrà impostata la stringa da visualizzare in base alla situazione
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (!isLastUser) {
            int array_option;
            if (currentUser.compareTo(userToDelete) == 0){
                array_option = R.array.my_option_array;
            } else
                array_option = R.array.member_option_array;
            builder.setItems(array_option, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // The 'which' argument contains the index position
                    // of the selected item
                    //
                    switch (which) {
                        case 0:
                            mListener.onDialogDeleteMemberClick(DeleteMemberDialog.this);
                            break;

                    }
                }
            });
        } else {
            int array_option = R.array.last_member_option_array;
            builder.setItems(array_option, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // The 'which' argument contains the index position
                    // of the selected item
                    //
                    switch (which) {
                        case 0:
                            mListener.onDialogLeaveAndDeleteClick(DeleteMemberDialog.this);
                            break;

                    }
                }
            });

        }

        // Create the AlertDialog object and return it
        return builder.create();
    }

}
