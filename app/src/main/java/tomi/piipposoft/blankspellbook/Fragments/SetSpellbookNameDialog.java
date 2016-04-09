package tomi.piipposoft.blankspellbook.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import tomi.piipposoft.blankspellbook.Database.PowerContract;
import tomi.piipposoft.blankspellbook.R;

/**
 * Created by Domu on 09-Apr-16.
 *
 * http://developer.android.com/guide/topics/ui/dialogs.html
 */
public class SetSpellbookNameDialog extends DialogFragment {



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //handle adding a new spell book

        PowerContract.PowerHelper powerDbHelper = new PowerContract.PowerHelper(getActivity().getApplicationContext());
        SQLiteDatabase myDb = powerDbHelper.getWritableDatabase();


        builder.setView(inflater.inflate(R.layout.set_spellbook_name_dialog, null))
                .setTitle(R.string.alertdialog_createspellbook)
                .setPositiveButton(R.string.createspellbook_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do stuff when create is clicked...
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
