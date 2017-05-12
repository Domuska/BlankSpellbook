package tomi.piipposoft.blankspellbook.Utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.PowerDetails.PowerDetailsPresenter;
import tomi.piipposoft.blankspellbook.PowerList.PowerListPresenter;

/**
 * Created by Domu on 17-Apr-16.
 */
public class DataSource {
    public static final String DB_SPELL_LIST_TREE_NAME = "spell_lists";
    public static final String DB_SPELL_TREE_NAME = "spells";
    public static final String DB_SPELL_LIST_SPELLS_CHILD = "spells";
    private static final String TAG = "DataSource";

//    SQLiteDatabase myDb = BlankSpellBookContract.DBHelper

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public static BlankSpellBookContract.DBHelper getDatasource(Activity activity){
        return new BlankSpellBookContract.DBHelper(activity.getApplicationContext());
    }

    //passing in the class reference can cause weird errors later
    //this method not used any more, addSpellListener does what it does
    public static void getSpellsWithSpellBookId(final PowerListPresenter presenter, Context context, String id){
        //get reference to spells/$spell_lists
        DatabaseReference spellListReference =
                firebaseDatabase.getReference(DB_SPELL_LIST_TREE_NAME).child(id);

        spellListReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //get a list (map) of all spell IDs that this spell book contains
                HashMap<String, Boolean> spellsMap =
                        (HashMap<String, Boolean>) dataSnapshot.child(DB_SPELL_LIST_SPELLS_CHILD).getValue();

                for (Object o : spellsMap.entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    //the first object in the map is the ID of the spell, cast to string and store
                    Log.d(TAG, "spell id: " + pair.getKey());
                    getSpellFromDatabase((String) pair.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error, " + databaseError.toException().toString());
            }
        });
    }


    public static void addSpellListListener(String id){
        DatabaseReference spellListReference =
                firebaseDatabase.getReference(DB_SPELL_LIST_TREE_NAME).child(id).child(DB_SPELL_LIST_SPELLS_CHILD);


        spellListReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String newSpellId = dataSnapshot.getKey();
                Log.d(TAG, "a new spell was added with ID : " + newSpellId);
                getSpellFromDatabase(newSpellId);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //do stuff
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String removedSpellId = dataSnapshot.getKey();
                Log.d(TAG, "a spell was removed with ID: " + removedSpellId);

                //get the spell's info from database
                DatabaseReference spellReference =
                        firebaseDatabase.getReference(DB_SPELL_TREE_NAME).child(removedSpellId);

                spellReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //get the Spell object from the snapshot, add snapshot's Key as spell's ID
                        Spell removedSpell = dataSnapshot.getValue(Spell.class).setSpellId(dataSnapshot.getKey());
                        PowerListPresenter.handleSpellDeletion(removedSpell);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "Error occurred: " + databaseError.toString());
                    }
                });
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error ocurred: " + databaseError.toString());
            }
        });
    }

    public static void getSpellWithId(String spellId) {
        DatabaseReference spellReference =
                firebaseDatabase.getReference(DB_SPELL_TREE_NAME).child(spellId);

        spellReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Spell spell = dataSnapshot.getValue(Spell.class);
                PowerDetailsPresenter.handleFetchedSpell(spell, dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "error in getSpellWithId " + databaseError.toString());
            }
        });
    }

    /**
     * Take a Spell object and save it to database
     * @param spell An initialized spell object
     * @param powerListId Power list to which this spell is to be added, can be null
     */
    public static void saveSpell(Spell spell, String powerListId) {
        DatabaseReference spellReference = firebaseDatabase.getReference().child(DB_SPELL_TREE_NAME);
        String spellId = spellReference.push().getKey();

        //for saving $spell_id - true to spell_lists/$spellId/spells/$spellId
        Map<String, Object> childUpdates = new HashMap<>();

        //if spellList id not null, add it to the tables that are to be updated as well
        if(powerListId != null && !powerListId.isEmpty()){
            //add new child to spell_lists/$spell_id/spells/
            childUpdates.put(DB_SPELL_LIST_TREE_NAME
                    + "/"
                    + powerListId
                    + "/"
                    + DB_SPELL_LIST_SPELLS_CHILD
                    + "/"
                    + spellId, true);
        }

        //use Jackson Objectmapper to create map of the Spell object
        Map<String, Object> spellValues = new ObjectMapper().convertValue(spell, Map.class);
        //store the new spell to spells/$spell_id/, ID gotten with push() above
        childUpdates.put(DB_SPELL_TREE_NAME
                + "/"
                + spellId
                + "/", spellValues);
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);

        //add a listener to the newly added spell
        spellReference.child(spellId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Spell savedSpell = dataSnapshot.getValue(Spell.class);
                PowerDetailsPresenter.handleFetchedSpell(savedSpell, dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Erro at fetching newly saved spell " + databaseError.toString());
            }
        });

    }

    public static void updateSpell(Spell spell, String spellId){
        DatabaseReference spellReference = firebaseDatabase.getReference().child(DB_SPELL_TREE_NAME).child(spellId);
        spellReference.setValue(spell);

        spellReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Spell modifiedSpell = dataSnapshot.getValue(Spell.class);
                PowerDetailsPresenter.handleFetchedSpell(modifiedSpell, dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error when fetching modified spell " + databaseError.toString());
            }
        });

    }

    /**
     * Get spells from the database with the supplied list of IDs, calls PowerListPresenter's
     * handleSpellFromDatabase when query completed. Each spell is queried for individually.
     * @param spellId array of FireBase spells entry IDs
     */
    private static void getSpellFromDatabase(final String spellId) {

        DatabaseReference spellsReference =
                firebaseDatabase.getReference(DB_SPELL_TREE_NAME).child(spellId);
        spellsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Spell spell = dataSnapshot.getValue(Spell.class);
                if(spell != null) {
                    Log.d(TAG, "onDataChange, spell name: " + dataSnapshot.getValue(Spell.class).getName());
                    PowerListPresenter.handleSpellFromDatabase(
                            //Add snapshot's key as spell's ID, use the returned Spell as parameter
                            spell.setSpellId(dataSnapshot.getKey())
                    );
                }
                else
                    Log.d(TAG, "Error retrieving spell, spell with ID " + spellId + " not in DB");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "error when retrieving spells: " + databaseError.toString());
            }
        });
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
                .setHitDamageOrEffect("1d10+CHA")
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
