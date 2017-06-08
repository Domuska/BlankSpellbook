package tomi.piipposoft.blankspellbook.PowerDetails;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tomi.piipposoft.blankspellbook.R;

/**
 * Created by OMISTAJA on 8.6.2017.
 */

public class ConfirmDeletionDialog extends DialogFragment {

    ConfirmDeletionListener listener;

    interface ConfirmDeletionListener{
        void onPositiveClick();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (ConfirmDeletionDialog.ConfirmDeletionListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement ConfirmDeletionListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_confirm_power_deletion, null))
                .setTitle(R.string.deleteDialog_title)
                .setPositiveButton(R.string.action_delete_power, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onPositiveClick();
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ConfirmDeletionDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }
}
