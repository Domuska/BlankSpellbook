package tomi.piipposoft.blankspellbook.dialog_fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import tomi.piipposoft.blankspellbook.R;

/**
 * Created by Domu on 09-Apr-16.
 *
 * http://developer.android.com/guide/topics/ui/dialogs.html
 */
public class SetPowerListNameDialog extends DialogFragment {


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener{
        void onSetPowerListNameDialogPositiveClick(DialogFragment dialog, String powerListName);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.set_spellbook_name_dialog, null);
        final EditText dialogEditText = (EditText) view.findViewById(R.id.edittext_spellbookname);

        builder.setView(view)
                .setTitle(R.string.alertdialog_createspellbook)
                .setPositiveButton(R.string.createspellbook_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onSetPowerListNameDialogPositiveClick(SetPowerListNameDialog.this, dialogEditText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.createspellbook_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SetPowerListNameDialog.this.getDialog().cancel();
                    }
                });


        return builder.create();

    }
}
