package tomi.piipposoft.blankspellbook.Utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;

/**
 * Created by Domu on 17-Apr-16.
 */
public class DataSource {
    private static final String TAG = "DataSource";

//    SQLiteDatabase myDb = BlankSpellBookContract.DBHelper


    public static BlankSpellBookContract.DBHelper getDatasource(Activity activity){
        return new BlankSpellBookContract.DBHelper(activity.getApplicationContext());
    }

    public static ArrayList<Spell> getSpellsWithSpellBookId(Context context, long id){
        Log.d(TAG, "getSpellsWithSpellBookId called with ID " + id);

        //handle the dirty SQL things here
        BlankSpellBookContract.DBHelper myDbHelper = new BlankSpellBookContract.DBHelper(context);
        SQLiteDatabase db = myDbHelper.getReadableDatabase();

        //projection; which rows to select
        String[] projection = {
                BlankSpellBookContract.PowerEntry.COLUMN_NAME_POWER_NAME,
                BlankSpellBookContract.PowerEntry.COLUMN_NAME_GROUP
        };

        //selection arguments - use the ID of the spell book to get all spells in that book
        String selection = BlankSpellBookContract.PowerEntry.COLUMN_NAME_POWER_LIST_ID + " = ?";
        String selectionArgs[] = {Long.toString(id)};

        //do the query
        Cursor cursor = db.query(
                BlankSpellBookContract.PowerEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        ArrayList<Spell> spells = new ArrayList<>();
        cursor.moveToFirst();

        if(cursor.getCount() > 0) {
            //fill the spells arraylist with data from cursor
            do {
                Spell s = new Spell();
                s.setName(cursor.getString(
                        cursor.getColumnIndexOrThrow(BlankSpellBookContract.PowerEntry.COLUMN_NAME_POWER_NAME)));
                s.setGroupName(cursor.getString(
                        cursor.getColumnIndexOrThrow(BlankSpellBookContract.PowerEntry.COLUMN_NAME_GROUP)
                ));

                spells.add(s);
                cursor.moveToNext();
            } while (!(cursor.isLast()));
        }

        return spells;
    }



    private static ArrayList<Spell> getDummyData(){
        Spell spell = new Spell();

        spell
                .setName("Abi zalim's horrid wilting")
                .setHitDamage("1d10+CHA")
                .setAttackRoll("level + INT")
                .setGroupName("level 8");

        ArrayList<Spell> data = new ArrayList<>();
        data.add(spell);

        spell = new Spell();
        spell.setName("fireball")
                .setGroupName("level 3");
        data.add(spell);

        spell = new Spell();
        spell.setName("frost ray")
                .setGroupName("lvl 1");
        data.add(spell);

        for(int i = 0; i < 10; i++) {
            spell = new Spell();
            spell.setName("sepon säde").setGroupName("lvl 1");
            data.add(spell);
        }
        return data;
    }

}
