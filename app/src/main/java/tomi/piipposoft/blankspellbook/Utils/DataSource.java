package tomi.piipposoft.blankspellbook.Utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import tomi.piipposoft.blankspellbook.Database.DailySpellList;
import tomi.piipposoft.blankspellbook.Database.SpellList;
import tomi.piipposoft.blankspellbook.Drawer.DrawerPresenter;
import tomi.piipposoft.blankspellbook.MainActivity.MainActivityPresenter;
import tomi.piipposoft.blankspellbook.PowerDetails.PowerDetailsPresenter;
import tomi.piipposoft.blankspellbook.PowerList.PowerListPresenter;

/**
 * Created by Domu on 17-Apr-16.
 */
public class DataSource {

    // TODO: 6.6.2017 sort these in some sensible way

    public static final String DB_POWER_LISTS_REFERENCE = "spell_lists";
    public static final String DB_DAILY_POWER_LIST_NAME = "daily_power_lists";
    public static final String DB_DAILY_POWER_LIST_CHILD_SPELLS = "spells";
    public static final String DB_DAILY_POWER_LIST_CHILD_NAME = "name";
    public static final String DB_SPELL_GROUPS_TREE_NAME = "spell_groups";
    public static final String DB_DAILY_SPELL_GROUPS_TREE_NAME = "daily_spell_groups";

    //for spell_groups table
    public static final String DB_SPELL_LIST_TREE_NAME = "spell_lists";
    public static final String DB_SPELL_LIST_CHILD_NAME = "name";
    public static final String DB_SPELL_LIST_CHILD_SPELLS = "spells";

    //for spells table
    public static final String DB_SPELL_TREE_NAME = "spells";
    public static final String DB_SPELLS_CHILD_GROUP_NAME = "groupName";
    public static final String DB_SPELLS_CHILD_DAILY_POWER_LISTS = "dailyPowerLists";
    private static final String DB_SPELLS_CHILD_POWER_LIST = "powerListId";

    public static final int DRAWERPRESENTER = 1;
    public static final int MAINACTIVITYPRESENTER = 2;
    public static final int POWERDETAILSPRESENTER = 3;

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
                        (HashMap<String, Boolean>) dataSnapshot.child(DB_SPELL_LIST_CHILD_SPELLS).getValue();

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


    /**
     * Add a new ChildEventListener to a spell list in DB
     * @param id ID of the spell list which to listen to
     * @return the child event listener
     */
    public static ChildEventListener addPowerListPowerListener(String id){
        DatabaseReference spellListReference =
                firebaseDatabase
                        .getReference(DB_SPELL_LIST_TREE_NAME)
                        .child(id)
                        .child(DB_SPELL_LIST_CHILD_SPELLS);

        Log.d(TAG, "adding path " + DB_SPELL_LIST_TREE_NAME + "/" + id + "/" + DB_SPELL_LIST_CHILD_SPELLS);


        return spellListReference.addChildEventListener(new ChildEventListener() {
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
    public static void saveSpellAndAddListener(Spell spell, String powerListId) {

        DatabaseReference spellReference = firebaseDatabase.getReference().child(DB_SPELL_TREE_NAME);
        String powerId = spellReference.push().getKey();

        Map<String, Object> childUpdates = getSaveSpellChildUpdates(spell, powerListId, powerId);

        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);

        //add a listener to the newly added spell
        spellReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Spell savedSpell = dataSnapshot.getValue(Spell.class);
                PowerDetailsPresenter.handleFetchedSpell(savedSpell, dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error at fetching newly saved spell " + databaseError.toString());
            }
        });
    }

    /**
     *
     * @param spell The spell that is to be updated
     * @param spellId The ID of the spell,
     * @param groupNameUpdated If the name of the spell group was updated or not
     * @param powerListId The spell list which this particular spell belongs to
     */
    public static void updateSpell(@NonNull Spell spell, @NonNull String spellId,
                                   @NonNull boolean groupNameUpdated, @Nullable String oldGroupName,
                                   @Nullable String powerListId){
        DatabaseReference spellReference = firebaseDatabase.getReference().child(DB_SPELL_TREE_NAME).child(spellId);

        Map<String, Object> childUpdates = new HashMap<>();

        //update the old spell
        childUpdates.put(DB_SPELL_TREE_NAME
                + "/"
                + spellId, spell);

        //check if the power belongs to a group, if it does we need to update the spell_groups table too
        if(!"".equals(powerListId)) {
            //update the spell_groups/id/groupName/
            if (groupNameUpdated) {
                //remove the spell from the old group
                childUpdates.put(DB_SPELL_GROUPS_TREE_NAME
                        + "/"
                        + powerListId
                        + "/"
                        + oldGroupName
                        + "/"
                        + spellId, null);
                //add the spell to the new group if new name is not null
                if (spell.getGroupName() != null) {
                    childUpdates.put(DB_SPELL_GROUPS_TREE_NAME
                            + "/"
                            + powerListId
                            + "/"
                            + spell.getGroupName()
                            + "/"
                            + spellId, true);
                }
            }
        }

        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);

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
     * Used for removing the reference to a spell from spell_lists table,
     * only removes the reference not the spell itself
     * @param spellIds IDs of the powers to be removed from list
     * @param powerListId ID of the power list where the power is to be removed
     */
    public static void removePowersFromList(String[] spellIds, final String powerListId) {

        Log.d(TAG, "removePowersFromList, power list id: " + powerListId);
        for (final String spellId : spellIds) {
            final Map<String, Object> childUpdates = new HashMap<>();

            //first remove the spell from the power list
            childUpdates.put(DB_SPELL_LIST_TREE_NAME
                    + "/"
                    + powerListId
                    + "/"
                    + DB_SPELL_LIST_CHILD_SPELLS
                    + "/"
                    + spellId, null);

            //remove the power list reference from this power's field
            childUpdates.put(
                    DB_SPELL_TREE_NAME
                    + "/"
                    + spellId
                    + "/"
                    + DB_SPELLS_CHILD_POWER_LIST, null);

            //get the group name of this spell
            firebaseDatabase.getReference()
                    .child(DB_SPELL_TREE_NAME)
                    .child(spellId)
                    .child(DB_SPELLS_CHILD_GROUP_NAME)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //delete the reference to the spell in spell_groups table in this list
                            String groupName = dataSnapshot.getValue(String.class);
                            childUpdates.put(DB_SPELL_GROUPS_TREE_NAME
                                    + "/"
                                    + powerListId
                                    + "/"
                                    + groupName
                                    + "/"
                                    + spellId, null);
                            //trigger the updates in DB
                            FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    /**
     * Get spells from the database with the supplied ID, calls PowerListPresenter's
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

    public static void setDatabasePersistance() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    /**
     * Attach a listener to the spell_lists portion of database to get informed of child changes
     * @param presenterCalling The code of the listener, use either DataSource.DRAWERPRESENTER or DataSource.MAINACTIVITYPRESENTER
     * @return the listner that was attached, so this listener can be detached later
     */
    public static ChildEventListener attachPowerListListener(final int presenterCalling){
        //first create the childEventListener
        ChildEventListener spellListChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String spellListName = dataSnapshot.child("name").getValue(String.class);
                Log.d(TAG, "attachPowerListListener: spell list name: " + spellListName);
                Log.d(TAG, "attachPowerListListener: list ID: " + dataSnapshot.getKey());
                //add a new power list item to the drawer
                if(presenterCalling == DRAWERPRESENTER)
                    DrawerPresenter.handlePowerList(spellListName, dataSnapshot.getKey());
                else if(presenterCalling == MAINACTIVITYPRESENTER) {
                    //MainActivityPresenter.handleNewListItem(spellListName, dataSnapshot.getKey());
                    //now add a listener to the spell_groups so we get the group names too
                    addSpellGroupListener(spellListName, dataSnapshot.getKey());
                }
                else {
                    throw new RuntimeException(
                            "Unhandled parameter at "
                                    + DataSource.class.getName()
                                    + ".attachPowerListListener"
                                    + "use either DataSource.DRAWERPRESENTER or DataSource.MAINACTIVITYPRESENTER");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //do stuff
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "item removed: " + dataSnapshot.child(DB_SPELL_LIST_CHILD_NAME) + " " + dataSnapshot.getKey());
                String spellListName = dataSnapshot.child(DB_SPELL_LIST_CHILD_NAME).getValue(String.class);

                if(presenterCalling == DRAWERPRESENTER)
                    DrawerPresenter.handleRemovedItem(spellListName, dataSnapshot.getKey());
                else if(presenterCalling == MAINACTIVITYPRESENTER)
                    MainActivityPresenter.handleRemovedPowerList(spellListName, dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.toString());
            }
        };

        //attach the listener to "spell_lists/" and return it
        firebaseDatabase.getReference(DB_POWER_LISTS_REFERENCE).addChildEventListener(spellListChildListener);
        return spellListChildListener;
    }

    //used for adding a listener to the spell_groups
    //spellListName and key are passed in so these can be given to MainActivityPresenter in the callback
    private static void addSpellGroupListener(final String spellListName, final String spellListId) {

        firebaseDatabase
                .getReference(DB_SPELL_GROUPS_TREE_NAME)
                .child(spellListId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> groupNames = new ArrayList<>();

                        for (DataSnapshot snapshot :
                                dataSnapshot.getChildren()) {
                            groupNames.add(snapshot.getKey());
                        }
                        MainActivityPresenter.handleNewPowerList(spellListName, spellListId, groupNames);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "error at addSpellGroupListener: " + databaseError.toString());
                    }
                });
    }

    /**
     * Used by attachDailyPowerListListener to get the names of the groups this daily power list
     * contains, will inform MainActivityPresenter of the result
     * @param name name of the power list
     * @param id ID of the power list
     */
    private static void addDailySpellGroupListener(final String name, final String id) {
        firebaseDatabase
                .getReference(DB_DAILY_SPELL_GROUPS_TREE_NAME)
                .child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> groupNames = new ArrayList<>();
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            groupNames.add(snapshot.getKey());
                            //Log.d(TAG, "group name in addDailySpellGroupListener: " + snapshot.getKey());
                        }
                        Log.d(TAG, "daily power group name in addDailySpellGroupListener: " + name);
                        MainActivityPresenter.handleNewDailyPowerList(name, id, groupNames);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "error at addDailySpellGroupListener: " + databaseError.toString());
                    }
                });
    }

    /**
     * Attach a listener to the daily_power_lists portion of database to get informed of child changes
     * @param presenterCalling The code of the listener, use either DataSource.DRAWERPRESENTER or DataSource.MAINACTIVITYPRESENTER
     * @return the listner that was attached, so this listener can be detached later
     */
    public static ChildEventListener attachDailyPowerListListener(final int presenterCalling) {
        ChildEventListener dailyPowerListChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String name = dataSnapshot.child(DB_DAILY_POWER_LIST_CHILD_NAME).getValue(String.class);
                //add a new daily power list item to the drawer
                if(presenterCalling == DRAWERPRESENTER)
                    DrawerPresenter.handleDailyPowerList(name, dataSnapshot.getKey());
                else if(presenterCalling == MAINACTIVITYPRESENTER) {
                    addDailySpellGroupListener(name, dataSnapshot.getKey());
                    //MainActivityPresenter.handleNewDailyPowerList(name, dataSnapshot.getKey());
                }

                else {
                    throw new RuntimeException(
                            "Unhandled parameter at "
                                    + DataSource.class.getName()
                                    + ".attachDailyPowerListListener"
                                    + "use either DataSource.DRAWERPRESENTER or DataSource.MAINACTIVITYPRESENTER");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //do stuff
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String spellListName = dataSnapshot.child("name").getValue(String.class);
                if(presenterCalling == DRAWERPRESENTER)
                    DrawerPresenter.handleRemovedItem(spellListName, dataSnapshot.getKey());
                else if(presenterCalling == MAINACTIVITYPRESENTER)
                    MainActivityPresenter.handleRemovedDailyPowerList(spellListName, dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.toString());
            }
        };

        firebaseDatabase.getReference(DB_DAILY_POWER_LIST_NAME)
                .addChildEventListener(dailyPowerListChildListener);
        return dailyPowerListChildListener;
    }



    public static void removePowerListListener(ChildEventListener spellListChildListener) {
        firebaseDatabase.getReference(DB_POWER_LISTS_REFERENCE).removeEventListener(spellListChildListener);
    }

    public static void removeDailyPowerListListener(ChildEventListener spellListChildListener) {
        firebaseDatabase.getReference(DB_DAILY_POWER_LIST_NAME).removeEventListener(spellListChildListener);
    }

    public static void removePowerListPowerListener(ChildEventListener listener, String powerListId){
        firebaseDatabase
                .getReference(DB_SPELL_LIST_TREE_NAME)
                .child(powerListId)
                .child(DB_SPELL_LIST_CHILD_SPELLS)
                .removeEventListener(listener);
    }

    public static void addNewDailyPowerList(String dailyPowerListName) {
        DailySpellList dailySpellList = new DailySpellList(dailyPowerListName);
        firebaseDatabase.getReference(DB_DAILY_POWER_LIST_NAME).push().setValue(dailySpellList);
    }

    public static void addNewPowerList(String powerListName) {
        SpellList spellList = new SpellList(powerListName);
        firebaseDatabase.getReference(DB_POWER_LISTS_REFERENCE).push().setValue(spellList);
    }

    /**
     * Get all of the power lists just once, don't attach a listener
     * @param presenterCalling code for which presenter is calling, use either DataSource.DRAWERPRESENTER, DataSource.POWERDETAILSPRESENTER or DataSource.MAINACTIVITYPRESENTER
     */
    public static void getPowerLists(final int presenterCalling){
        firebaseDatabase.getReference(DB_POWER_LISTS_REFERENCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "getPowerLists, presenter calling: " + presenterCalling);
                        //give power details presenter the data as two arrays
                        switch(presenterCalling){
                            case DataSource.POWERDETAILSPRESENTER:
                                Log.d(TAG, "getPowerLists, powerDetailsPresenter");
                                String[] names = new String[(int)dataSnapshot.getChildrenCount()];
                                String[] ids = new String[(int)dataSnapshot.getChildrenCount()];
                                int i = 0;
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    ids[i] = snapshot.getKey();
                                    names[i] = snapshot.child(DB_SPELL_LIST_CHILD_NAME).getValue(String.class);
                                    i++;
                                }
                                PowerDetailsPresenter.handleFetchedPowerLists(names, ids);
                                break;

                            case DataSource.DRAWERPRESENTER:
                                //give the children one by one to the drawer presenter
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    DrawerPresenter.handlePowerList(
                                            snapshot.child(DB_SPELL_LIST_CHILD_NAME).getValue(String.class),
                                            snapshot.getKey());
                                }
                                break;

                            case DataSource.MAINACTIVITYPRESENTER:
                                Log.d(TAG, "getPowerLists, mainActivityPresenter");
                                //give the children one by one to MainActivityPresenter
                                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                                    //call addSpellGroupListener so we also get the spell group names for MainActivityPresenter
                                    addSpellGroupListener(
                                            snapshot.child(DB_DAILY_POWER_LIST_CHILD_NAME).getValue(String.class),
                                            snapshot.getKey());
                                }
                                break;

                            default:
                                throw new RuntimeException(
                                        "Unhandled parameter at "
                                                + DataSource.class.getName()
                                                + ".getPowerLists"
                                                + "use either DataSource.DRAWERPRESENTER or DataSource.POWERDETAILSPRESENTER.");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "something went wrong fetching power lists" + databaseError.toString());
                    }
                });
    }

    public static void getDailyPowerLists(final int presenterCalling){
        firebaseDatabase.getReference(DB_DAILY_POWER_LIST_NAME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "# of children in getDailyPowerLists: " + dataSnapshot.getChildrenCount());

                        switch (presenterCalling) {
                            case DataSource.POWERDETAILSPRESENTER:
                                String[] names = new String[(int) dataSnapshot.getChildrenCount()];
                                String[] ids = new String[(int) dataSnapshot.getChildrenCount()];
                                int i = 0;
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    ids[i] = snapshot.getKey();
                                    names[i] = snapshot.child(DB_SPELL_LIST_CHILD_NAME).getValue(String.class);
                                    i++;
                                }
                                PowerDetailsPresenter.handleFetchedDailyPowerLists(names, ids);
                                break;

                            case DataSource.DRAWERPRESENTER:
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    DrawerPresenter.handleDailyPowerList(
                                            snapshot.child(DB_DAILY_POWER_LIST_CHILD_NAME).getValue(String.class),
                                            snapshot.getKey());
                                }
                                break;

                            case DataSource.MAINACTIVITYPRESENTER:
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    addDailySpellGroupListener(
                                            snapshot.child(DB_DAILY_POWER_LIST_CHILD_NAME).getValue(String.class),
                                            snapshot.getKey());
                                }
                                break;

                            default:
                                throw new RuntimeException(
                                        "Unhandled parameter at "
                                                + DataSource.class.getName()
                                                + ".getPowerLists,"
                                                + " use either DataSource.DRAWERPRESENTER"
                                                + " or DataSource.POWERDETAILSPRESENTER"
                                                + " or DataSource.MAINACTIVITYPRESENTER"
                                                + "Supplied parameter was: " + presenterCalling);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "something went wrong fetching daily power lists" + databaseError.toString());
                    }
                });
    }


    public static void addSpellToPowerLists(ArrayList<String> listIds, Spell power) {

        for(String listId : listIds) {
            Log.d(TAG, "in addSpellToPowerLists, spell id: " + power.getSpellId());
            //get the ID for the new spell
            DatabaseReference spellReference = firebaseDatabase.getReference().child(DB_SPELL_TREE_NAME);
            String powerId = spellReference.push().getKey();
            //add the ID of this list to the power's list of IDs
            power.setPowerListId(listId);
            //get the childUpdates needed for saving a new spell
            Map<String, Object> childUpdates = getSaveSpellChildUpdates(power, listId, powerId);

            FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
        }
    }

    public static void addSpellToDailyPowerLists(ArrayList<String> listIds,
                                                 String spellId, String groupName) {

        Map<String, Object> childUpdates = new HashMap<>();

        for(String id : listIds) {
            //add the daily power list ID to this spell's list of daily power lists
            childUpdates.put(
                    DB_SPELL_TREE_NAME
                    + "/"
                    + spellId
                    + "/"
                    + DB_SPELLS_CHILD_DAILY_POWER_LISTS
                    + "/"
                    + id, true);

            //add the spell to the daily power lists's spell list
            childUpdates.put(
                    DB_DAILY_POWER_LIST_NAME
                    + "/"
                    + id
                    + "/"
                    + DB_DAILY_POWER_LIST_CHILD_SPELLS
                    + "/"
                    + spellId, true);

            //add also the spell's group to the daily_spell_groups table
            if(!"".equals(groupName)) {
                childUpdates.put(
                        DB_DAILY_SPELL_GROUPS_TREE_NAME
                        + "/"
                        + id
                        + "/"
                        + groupName
                        + "/"
                        + spellId, true);
            }



            /*DatabaseReference ref = firebaseDatabase
                            .getReference(DB_DAILY_POWER_LIST_NAME)
                            .child(id)
                            .child(DB_DAILY_POWER_LIST_CHILD_SPELLS);
            ref.child(spellId).setValue(true);*/
            firebaseDatabase.getReference().updateChildren(childUpdates);
        }
    }

    /**
     * Get the power/spell groups inside a power list
     * @param powerListId ID of the power list
     */
    public static void getPowerGroupsWithListId(String powerListId) {
        firebaseDatabase
                .getReference(DB_SPELL_GROUPS_TREE_NAME)
                .child(powerListId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String[] groups = new String[(int)dataSnapshot.getChildrenCount()];
                                int i = 0;
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    groups[i] = snapshot.getKey();
                                    Log.d(TAG, "adding power group '" + groups[i] + "' to array");
                                    i++;
                                }
                                PowerDetailsPresenter.handleFetchedSpellGroups(groups);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "something went wrong at getPowerGroupsWithListId:"
                                            + databaseError.toString());
                            }
                        }
                );
    }

    public static ChildEventListener attachPowerListener(final int presenterCalling) {
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //if main activity presenter is the one adding the listener
                if(presenterCalling == MAINACTIVITYPRESENTER){
                    Spell power = dataSnapshot.getValue(Spell.class);
                    //if power has a list it belongs to list, get name of that list too
                    if(power.getPowerListId() != null && !"".equals(power.getPowerListId()))
                        getListPowerBelongsTo(power);
                    else
                        MainActivityPresenter.handleNewPower(power, null);

                }
                else{
                    Log.e(TAG, "unknown caller in attachPowerListener onChildAdded: " + presenterCalling);
                    throw new RuntimeException("unknown caller in attachPowerListener," +
                            "use DataSource.MAINACTIVITYPRESENTER");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(presenterCalling == MAINACTIVITYPRESENTER){
                    MainActivityPresenter.handlePowerRemoved(dataSnapshot.getValue(Spell.class));
                }
                else{
                    Log.e(TAG, "unknown caller in attachPowerListener onChildRemoved: " + presenterCalling);
                    throw new RuntimeException("unknown caller in attachPowerListener," +
                            "use DataSource.MAINACTIVITYPRESENTER");
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "something went wrong at attachPowerListener, canceling: " + databaseError.toString());
            }
        };

        //attach the listener and return it
        firebaseDatabase.getReference().child(DB_SPELL_TREE_NAME).addChildEventListener(listener);
        return listener;
    }

    /**
     * Method for getting all spells in the database without attaching a listener to the spells tree
     * @param presenterCalling DataSource.MAINACTIVITYPRESENTER to signify it is calling for the data
     */
    public static void getPowers(final int presenterCalling) {
        firebaseDatabase.getReference().child(DB_SPELL_TREE_NAME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(presenterCalling == MAINACTIVITYPRESENTER){
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Spell power = snapshot.getValue(Spell.class);
                                //if power is associated with a power list, get the list's name
                                if(power.getPowerListId() != null && !"".equals(power.getPowerListId()))
                                    getListPowerBelongsTo(power);
                                else
                                    MainActivityPresenter.handleNewPower(power, null);
                            }
                        }
                        else{
                            Log.e(TAG, "unknown caller in getPowers onDataChange: " + presenterCalling);
                            throw new RuntimeException("unknown caller in getPowers," +
                                    "use DataSource.MAINACTIVITYPRESENTER");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "something went wrong at getPowers, canceling: " + databaseError.toString());
                    }
                });
    }

    public static void removePowersListener(ChildEventListener powersListener) {
        firebaseDatabase.getReference().child(DB_SPELL_TREE_NAME).removeEventListener(powersListener);
    }

    /**
     * Used for getting the name of power list a power belongs to, gives the whole power then to MainActivityPresenter
     * @param power the power that should be passed to MainActivityPresenter
     */
    private static void getListPowerBelongsTo(final Spell power){
        Log.d(TAG, "getListPowerBelongsTo: power list id: " + power.getPowerListId());
        firebaseDatabase.getReference()
                .child(DB_SPELL_LIST_TREE_NAME)
                .child(power.getPowerListId())
                .child(DB_SPELL_LIST_CHILD_NAME)
                .addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        MainActivityPresenter.handleNewPower(power, dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "something wrong in getListPowerBelongsTo, cancelling. " + databaseError.toString());
                    }
                }
        );
    }


    /**
     * Method for getting the map of updates needed to add a new spell to DB
     * @param spell spell in question
     * @param powerListId ID of the power list this spell belongs to
     * @param spellId ID of the spell that is to be added
     * @return a map containing the updates
     */
    private static Map<String, Object> getSaveSpellChildUpdates(Spell spell, String powerListId, String spellId){

        Map<String, Object> childUpdates;

        if (!"".equals(spellId)) {
            //for saving $spell_id - true to spell_lists/$spellId/spells/$spellId
            childUpdates = new HashMap<>();

            //if spellList id not null, add it to the tables that are to be updated as well
            if(powerListId != null && !powerListId.isEmpty()){
                //add new child to spell_lists/$spell_id/spells/
                childUpdates.put(DB_SPELL_LIST_TREE_NAME
                        + "/"
                        + powerListId
                        + "/"
                        + DB_SPELL_LIST_CHILD_SPELLS
                        + "/"
                        + spellId, true);

                //add the spell_groups entry as well if spell is set to a group
                //note, spell can't have a group if it's not in a list (only lists have groups)
                if(!"".equals(spell.getGroupName())){
                    childUpdates.put(
                            DB_SPELL_GROUPS_TREE_NAME
                                    + "/"
                                    + powerListId
                                    + "/"
                                    + spell.getGroupName()
                                    + "/"
                                    + spellId, true);
                }
            }

            //add the spell entry to spells/ table
            //use Jackson Objectmapper to create map of the Spell object
            Map<String, Object> spellValues = new ObjectMapper().convertValue(spell, Map.class);
            //store the new spell to spells/$spell_id/, ID gotten with push() above
            childUpdates.put(DB_SPELL_TREE_NAME
                    + "/"
                    + spellId
                    + "/", spellValues);
            return childUpdates;
        } else {
            throw new RuntimeException("Error at getSaveSpellChildUpdates, spell should already have ID initialized");
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
