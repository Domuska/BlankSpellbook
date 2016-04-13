package tomi.piipposoft.blankspellbook.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Database.PowerListContract;
import tomi.piipposoft.blankspellbook.R;

/**
 * Created by Domu on 09-Apr-16.
 *
 * http://developer.android.com/guide/topics/ui/dialogs.html
 */
public class SetSpellbookNameDialog extends DialogFragment {


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener{
        void onDialogPositiveClick(DialogFragment dialog);
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
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //handle adding a new spell book

        final BlankSpellBookContract.PowerHelper powerDbHelper = new BlankSpellBookContract.PowerHelper(getActivity().getApplicationContext());
        final SQLiteDatabase myDb = powerDbHelper.getWritableDatabase();

        View view = inflater.inflate(R.layout.set_spellbook_name_dialog, null);
        final EditText dialogEditText = (EditText) view.findViewById(R.id.edittext_spellbookname);

        builder.setView(view)
                .setTitle(R.string.alertdialog_createspellbook)
                .setPositiveButton(R.string.createspellbook_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ContentValues values = new ContentValues();
                        values.put(BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME,
                                    dialogEditText.getText().toString());

                        myDb.insert(
                                BlankSpellBookContract.PowerListEntry.TABLE_NAME,
                                null,
                                values
                        );

                        mListener.onDialogPositiveClick(SetSpellbookNameDialog.this);

                    }
                })
                .setNegativeButton(R.string.createspellbook_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SetSpellbookNameDialog.this.getDialog().cancel();
                    }
                });


        return builder.create();

    }
}
