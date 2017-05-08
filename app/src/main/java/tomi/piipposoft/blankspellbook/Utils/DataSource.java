package tomi.piipposoft.blankspellbook.Utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.PowerList.PowerListPresenter;

/**
 * Created by Domu on 17-Apr-16.
 */
public class DataSource {
    public static final String DB_SPELL_LIST_TREE_NAME = "spell_lists";
    public static final String DB_SPELL_TREE_NAME = "spells";
    private static final String TAG = "DataSource";

//    SQLiteDatabase myDb = BlankSpellBookContract.DBHelper

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public static BlankSpellBookContract.DBHelper getDatasource(Activity activity){
        return new BlankSpellBookContract.DBHelper(activity.getApplicationContext());
    }

    //passing in the class reference can cause weird errors later
    // TODO: 8.5.2017 figure out better way to handle the class reference
    public static void getSpellsWithSpellBookId(final PowerListPresenter presenter, Context context, String id){
        //get reference to spells/$spell_lists
        DatabaseReference spellListReference =
                firebaseDatabase.getReference(DB_SPELL_LIST_TREE_NAME).child(id);


        spellListReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> spellIds = new ArrayList<>();

                HashMap<String, Boolean> spellsMap = (HashMap<String, Boolean>) dataSnapshot.child("spells").getValue();

                for (Object o : spellsMap.entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    //the first object in the map is the ID of the spell, cast to string and store
                    spellIds.add((String) pair.getKey());
                    Log.d(TAG, "spell id: " + pair.getKey());
                }

                Log.d(TAG, "spell ids table size: " + spellIds.size());
                if(spellIds.size() > 0){
                    getSpellsFromDatabase(spellIds, presenter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error, " + databaseError.toException().toString());
            }
        });
    }

    /**
     *
     * @param spellIds array of FireBase spells entry IDs
     */
    private static void getSpellsFromDatabase(List<String> spellIds, final PowerListPresenter presenter) {

        for(String id : spellIds) {
            DatabaseReference spellsReference =
                    firebaseDatabase.getReference(DB_SPELL_TREE_NAME).child(id);
            spellsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    presenter.handleSpellFromDatabase(
                            dataSnapshot.getValue(Spell.class)
                    );
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "error when retrieving spells: " + databaseError.toString());
                }
            });
        }
    }


    public static ArrayList<Spell> getSpellsWithSpellBookId2(Context context, long id){
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
                /*s.setSpellId(cursor.getLong(
                        cursor.getColumnIndexOrThrow(BlankSpellBookContract.PowerEntry.COLUMN_NAME_POWER_ID)
                ));*/

                spells.add(s);
                cursor.moveToNext();
            } while (!(cursor.isLast()));
        }

        return spells;
//        return getDummyData();
    }



    private static ArrayList<Spell> getDummyData(){
        Spell spell = new Spell();

        spell
                .setName("Abi zalim's horrid wilting")
                .setHitDamage("1d10+CHA")
                .setAttackRoll("level + INT")
                .setGroupName("level 8");
                //.setSpellId("5");


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
