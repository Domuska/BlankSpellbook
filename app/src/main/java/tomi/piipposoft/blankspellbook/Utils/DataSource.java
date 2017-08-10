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
import com.google.firebase.database.Query;
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




    public static final String DB_SPELL_GROUPS_TREE_NAME = "spell_groups";
    public static final String DB_DAILY_SPELL_GROUPS_TREE_NAME = "daily_spell_groups";

    //for spell_groups table
    public static final String DB_SPELL_LIST_TREE_NAME = "spell_lists";
    public static final String DB_SPELL_LIST_CHILD_NAME = "name";
    public static final String DB_SPELL_LIST_CHILD_SPELLS = "spells";
    public static final String DB_SPELL_LIST_SINGLE_SPELL_NAME = "powerName";

    //for spells table
    public static final String DB_SPELL_TREE_NAME = "spells";
    public static final String DB_SPELLS_CHILD_GROUP_NAME = "groupName";
    public static final String DB_SPELLS_CHILD_DAILY_POWER_LISTS = "dailyPowerLists";
    private static final String DB_SPELLS_CHILD_POWER_LIST = "powerListId";
    public static final String DB_SPELLS_CHILD_NAME = "name";

    //for spell_lists table
    public static final String DB_POWER_LISTS_REFERENCE = "spell_lists";
    private static final String DB_POWER_LISTS_CHILD_NAME = "name";

    //daily_power_lists
    public static final String DB_DAILY_POWER_LIST_TREE_NAME = "daily_power_lists";
    public static final String DB_DAILY_POWER_LIST_CHILD_SPELLS = "spells";
    public static final String DB_DAILY_POWER_LIST_CHILD_NAME = "name";



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
     * @param powerListId ID of the spell list which to listen to
     * @return the child event listener
     */
    public static ChildEventListener attachPowerGroupListeners(final String powerListId){

        //we need to check if there are any children in the power list, if not show it in UI
        firebaseDatabase
                .getReference(DB_SPELL_GROUPS_TREE_NAME)
                .child(powerListId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount() < 1){
                            Log.d(TAG, "attachPowerGroupListeners: power list with ID has no groups");
                            PowerListPresenter.noPowersToShow();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        /*
        This method flows as follows:
        1) get power groups, ordered by name
        2) give these groups to presenter
        3) launch a new query to get all powers under a group, ordered by power name
        4) give these powers one by one to presenter
         */

        final Query powerGroupsQuery = firebaseDatabase
                .getReference(DB_SPELL_GROUPS_TREE_NAME)
                .child(powerListId)
                .orderByValue();
        Log.d(TAG, "attachPowerGroupListeners: adding query");

        return powerGroupsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //at this point we have $group_name as each snapshot
                String powerGroupName = dataSnapshot.getKey();
                Log.d(TAG, "attachPowerGroupListeners: got group: " + powerGroupName);

                //make sure we dont start to listen twice to a group
                if(!PowerListPresenter.listeningToGroup(powerGroupName)) {
                    PowerListPresenter.handlePowerGroup(powerGroupName);
                    //add a listener to the group to get all children
                    //Get the power ids from spell_groups/$powerlistId/$groupName/
                    //order by power name
                    Query powersIdsQuery = firebaseDatabase
                            .getReference(DB_SPELL_GROUPS_TREE_NAME)
                            .child(powerListId)
                            .child(powerGroupName)
                            .orderByChild(DB_SPELL_LIST_SINGLE_SPELL_NAME);

                    //pass the childeventlistener and group's name to presenter to deattach this later
                    PowerListPresenter.handlePowerGroupListener(
                            powersIdsQuery.addChildEventListener(new powerValueListener()),
                            powerGroupName);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Used for gettin a spell object from DB
     * NOTE: used only by PowerDetailsPresenter at the moment
     * @param spellId ID of the spell that should be fetched
     */
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
        if(powerListId != null && !"".equals(powerListId)) {
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
                            + spellId
                            + "/"
                            + spell.getName(), true);
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
    private static void handlePowerRemoval(String[] spellIds, final String powerListId) {

        Log.d(TAG, "removePowersFromList, power list id: " + powerListId);
        for (final String spellId : spellIds) {
            final Map<String, Object> childUpdates = new HashMap<>();

            //first remove the spell from the power list
            childUpdates.put(
                    DB_SPELL_LIST_TREE_NAME + "/"
                    + powerListId + "/"
                    + DB_SPELL_LIST_CHILD_SPELLS + "/"
                    + spellId, null);

            //remove the power list reference from this power's field
            childUpdates.put(
                    DB_SPELL_TREE_NAME + "/"
                    + spellId + "/"
                    + DB_SPELLS_CHILD_POWER_LIST, null);

            //get the group name of this spell to remove reference in spell_groups
            firebaseDatabase.getReference()
                    .child(DB_SPELL_TREE_NAME)
                    .child(spellId)
                    .child(DB_SPELLS_CHILD_GROUP_NAME)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //delete the reference to the spell in spell_groups table in this list
                            String groupName = dataSnapshot.getValue(String.class);
                            childUpdates.put(
                                    DB_SPELL_GROUPS_TREE_NAME + "/"
                                    + powerListId + "/"
                                    + groupName + "/"
                                    + spellId, null);
                            //trigger the updates in DB
                            FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "removePowersFromList: something went wrong when gettin power list name: "
                                        + databaseError.toString());
                        }
                    });
        }
    }

    public static void removePowersFromList(String[] spellIds, final String powerListId){
        handlePowerRemoval(spellIds, powerListId);
    }

    public static void removePowersFromList(String spellId, String powerListId){
        handlePowerRemoval(new String[]{spellId}, powerListId);
    }

    /**
     * Add powers to a list of powers
     * @param powerIds IDs of the powers that should be added
     * @param powerListId The ID of the list where powers should be added
     */
    public static void addPowersToList(String[] powerIds, final String powerListId) {
        //this method is basically reverse to handlePowerRemoval
        final HashMap<String, Object> childUpdates = new HashMap<>();
        Log.d(TAG, "addPowersToList: power list id: " + powerListId);
        for(final String id : powerIds){
            //add the to the power list
            childUpdates.put(DB_SPELL_LIST_TREE_NAME + "/"
                    + powerListId + "/"
                    + DB_SPELL_LIST_CHILD_SPELLS + "/"
                    + id, true);

            //add reference to the power about this list
            childUpdates.put(DB_SPELL_TREE_NAME + "/"
                    + id + "/"
                    + DB_SPELLS_CHILD_POWER_LIST, powerListId);

            //get power's group name and add reference to spell_groups
            firebaseDatabase.getReference()
                    .child(DB_SPELL_TREE_NAME)
                    .child(id)
                    .child(DB_SPELLS_CHILD_GROUP_NAME)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String groupName = dataSnapshot.getValue(String.class);
                            Log.d(TAG, "addPowersToList: group name: " + groupName);
                            //add the group name to spell_groups
                            if(groupName != null && !"".equals(groupName)) {
                                childUpdates.put(
                                        DB_SPELL_GROUPS_TREE_NAME + "/"
                                                + powerListId + "/"
                                                + groupName + "/"
                                                + id, true);
                            }
                            firebaseDatabase.getReference().updateChildren(childUpdates);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "addPowersToList: error at getting power group name "
                                    + databaseError.toString());
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
     * @return the listener that was attached, so this listener can be detached later
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
                    //give mainActivityPresenter info of the power group
                    String powerListId = dataSnapshot.getKey();
                    MainActivityPresenter.handleNewPowerList(spellListName, powerListId);
                    //get all the power names, those will be given to MainActivityPresenter as well
                    for(DataSnapshot snapshot : dataSnapshot.child(DB_SPELL_LIST_CHILD_SPELLS).getChildren()) {
                        getPowerNameForPresenter(snapshot.getKey(), powerListId, true);
                    }
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
        /*firebaseDatabase.getReference(DB_POWER_LISTS_REFERENCE)
                .addChildEventListener(spellListChildListener);*/
        Query query = firebaseDatabase.getReference()
                .child(DB_POWER_LISTS_REFERENCE)
                .orderByChild(DB_POWER_LISTS_CHILD_NAME);
        query.addChildEventListener(spellListChildListener);
        return spellListChildListener;
    }

    private static void getPowerNameForPresenter(final String powerId,
                                                 final String powerListId,
                                                 final boolean isPowerList){
        firebaseDatabase
                .getReference()
                .child(DB_SPELL_TREE_NAME)
                .child(powerId)
                .child(DB_SPELLS_CHILD_NAME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        MainActivityPresenter.handleNewPowerNameForList(
                                dataSnapshot.getValue(String.class),
                                powerListId,
                                isPowerList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "error occurred at getPowerNameForPresenter: " + databaseError.toString());
                    }
                });

    }

    /**
     * Get power group names from spell_groups, calls back to MainActivityPresenter with the data
     * @param spellListName Name of the power list
     * @param spellListId ID of the power list
     */
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
                        //MainActivityPresenter.handleNewPowerList(spellListName, spellListId, groupNames);
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
                        // TODO: 9.6.2017 tuu takas tänne
                        //MainActivityPresenter.handleNewDailyPowerList(name, id, groupNames);
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
                    //give presenter the daily power list
                    String dailyPowerListId = dataSnapshot.getKey();
                    MainActivityPresenter.handleNewDailyPowerList(name, dailyPowerListId);
                    //get the names of powers in this list
                    for(DataSnapshot spellChild :
                            dataSnapshot.child(DB_DAILY_POWER_LIST_CHILD_SPELLS).getChildren()) {
                        //get the power names in this list for presenter
                        getPowerNameForPresenter(
                                spellChild.getKey(), dailyPowerListId, false);
                    }
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

        Query query = firebaseDatabase
                .getReference(DB_DAILY_POWER_LIST_TREE_NAME)
                .orderByChild(DB_DAILY_POWER_LIST_CHILD_NAME);
        query.addChildEventListener(dailyPowerListChildListener);
        return dailyPowerListChildListener;
    }



    public static void removePowerListListener(ChildEventListener spellListChildListener) {
        firebaseDatabase.getReference(DB_POWER_LISTS_REFERENCE).removeEventListener(spellListChildListener);
    }

    public static void removeDailyPowerListListener(ChildEventListener spellListChildListener) {
        firebaseDatabase.getReference(DB_DAILY_POWER_LIST_TREE_NAME).removeEventListener(spellListChildListener);
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
        firebaseDatabase.getReference(DB_DAILY_POWER_LIST_TREE_NAME).push().setValue(dailySpellList);
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
                                    //give MainActivityPresenter the power list
                                    String powerListId = snapshot.getKey();
                                    MainActivityPresenter.handleNewPowerList(
                                            snapshot.child(DB_SPELL_LIST_CHILD_NAME).getValue(String.class),
                                            powerListId);
                                    //get the names of powers that are in this group
                                    for(DataSnapshot powerSnapshot :
                                            snapshot.child(DB_SPELL_LIST_CHILD_SPELLS).getChildren()){
                                        getPowerNameForPresenter(powerSnapshot.getKey(),
                                                powerListId, true);
                                    }
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
        firebaseDatabase.getReference(DB_DAILY_POWER_LIST_TREE_NAME)
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
                                //here we have all of the daily power lists
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    String dailyPowerListId = snapshot.getKey();
                                    //give presenter the new daily power list
                                    MainActivityPresenter.handleNewDailyPowerList(
                                            snapshot.child(DB_DAILY_POWER_LIST_CHILD_NAME).getValue(String.class),
                                            dailyPowerListId);
                                    //get the power names for the MainActivityPresenter
                                    for(DataSnapshot spellsChild :
                                            snapshot.child(DB_DAILY_POWER_LIST_CHILD_SPELLS).getChildren()){
                                        getPowerNameForPresenter(
                                                spellsChild.getKey(), dailyPowerListId, false);
                                    }
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


    /**
     * Used for creating a copy of the spell and adding that power to power lists
     * handles updating spell_groups table as well
     * @param listIds IDs of the lists where a copy should be saved
     * @param power power that should be copied
     * @return the IDs of the powers that were created
     */
    public static String[] copyPowerToPowerLists(ArrayList<String> listIds, Spell power) {

        String[] powerIds = new String[listIds.size()];

        int i = 0;
        for(String listId : listIds) {
            Log.d(TAG, "in copyPowerToPowerLists, spell id: " + power.getSpellId());
            //get the ID for the new spell
            DatabaseReference spellReference = firebaseDatabase.getReference().child(DB_SPELL_TREE_NAME);
            String powerId = spellReference.push().getKey();
            powerIds[i] = powerId;

            //add the ID of this list to the power's list of IDs
            power.setPowerListId(listId);
            //get the childUpdates needed for saving a new spell
            Map<String, Object> childUpdates = getSaveSpellChildUpdates(power, listId, powerId);

            FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
            i++;
        }
        return powerIds;
    }

    /**
     * Add a reference of a power to a daily power list and daily_power_groups
     * @param listIds IDs of the daily power lists where a copy should be saved
     * @param spellId ID of the power that should be added
     * @param groupName name of the power's group, supply this if power actually has a group
     */
    public static void addSpellToDailyPowerLists(@NonNull ArrayList<String> listIds,
                                                 @NonNull String spellId, @Nullable String groupName) {

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
                    DB_DAILY_POWER_LIST_TREE_NAME
                    + "/"
                    + id
                    + "/"
                    + DB_DAILY_POWER_LIST_CHILD_SPELLS
                    + "/"
                    + spellId, true);

            //add also the spell's group to the daily_spell_groups table
            if(groupName != null && !"".equals(groupName)) {
                childUpdates.put(
                        DB_DAILY_SPELL_GROUPS_TREE_NAME
                        + "/"
                        + id
                        + "/"
                        + groupName
                        + "/"
                        + spellId, true);
            }

            firebaseDatabase.getReference().updateChildren(childUpdates);
        }
    }


    /**
     * Remove a spell from list of daily powers, handles removing reference in
     * daily_power_lists/$id/spells and in daily_spell_groups/$id/$groupName
     * @param dailyPowerLists Ids of the daily power lists where reference to power should be removed
     * @param powerId ID of the power that should be removed from the lists
     * @param groupName name of the group the power belongs to
     */
    public static void removeSpellFromDailyPowerLists(ArrayList<String> dailyPowerLists, String powerId, String groupName) {
        HashMap<String, Object> childUpdates = new HashMap<>();

        if(dailyPowerLists != null) {
            for (String listId : dailyPowerLists) {
                //remove the reference from the $daily_power_lists
                childUpdates.put(
                        DB_DAILY_POWER_LIST_TREE_NAME + "/"
                                + listId + "/"
                                + DB_DAILY_POWER_LIST_CHILD_SPELLS + "/"
                                + powerId, null);

                //remove the reference from $daily_spell_groups
                if (groupName != null) {
                    childUpdates.put(
                            DB_DAILY_SPELL_GROUPS_TREE_NAME + "/"
                                    + listId + "/"
                                    + groupName + "/"
                                    + powerId, null);
                }
                //push the updates
                firebaseDatabase.getReference().updateChildren(childUpdates);
            }
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
                    //save spell id since it might be needed if starting spellDetailsActivity of this spell
                    power.setSpellId(dataSnapshot.getKey());
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
        //firebaseDatabase.getReference().child(DB_SPELL_TREE_NAME).addChildEventListener(listener);
        Query query = firebaseDatabase.getReference()
                .child(DB_SPELL_TREE_NAME)
                .orderByChild(DB_SPELLS_CHILD_NAME);
        query.addChildEventListener(listener);
        return listener;
    }

    /**
     * Method for getting all spells in the database without attaching a listener to the spells tree
     * @param presenterCalling DataSource.MAINACTIVITYPRESENTER to signify it is calling for the data
     */
    public static void getPowers(final int presenterCalling) {
        //create a new listener for getting all spells
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(presenterCalling == MAINACTIVITYPRESENTER){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Spell power = snapshot.getValue(Spell.class);
                        //save spell id since it might be needed if starting spellDetailsActivity of this spell
                        power.setSpellId(snapshot.getKey());
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
        };

        //add a query to order the elements by spell's name
        Query query = firebaseDatabase.getReference()
                .child(DB_SPELL_TREE_NAME)
                .orderByChild(DB_SPELLS_CHILD_NAME);
        //launch the query
        query.addListenerForSingleValueEvent(listener);

    }

    public static void removePowersListener(ChildEventListener powersListener) {
        firebaseDatabase.getReference().child(DB_SPELL_TREE_NAME).removeEventListener(powersListener);
    }

    /**
     * Remove a power from the database
     * removes all references to the power as well from spell_groups and spell_lists
     * @param powerId ID of the power that is to be removed
     */
    public static void deletePower(final String powerId) {
        firebaseDatabase.getReference()
                .child(DB_SPELL_TREE_NAME)
                .child(powerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, Object> childUpdates = new HashMap<>();
                        //remove references from $daily_power_list
                        for(DataSnapshot snapshot :
                                dataSnapshot.child(DB_SPELLS_CHILD_DAILY_POWER_LISTS).getChildren()){
                            childUpdates.put(
                                    DB_DAILY_POWER_LIST_TREE_NAME + "/"
                                    + snapshot.getKey() + "/"
                                    + DB_DAILY_POWER_LIST_CHILD_SPELLS + "/"
                                    + powerId, null);
                        }

                        String groupName = dataSnapshot.child(DB_SPELLS_CHILD_GROUP_NAME).getValue(String.class);

                        for(DataSnapshot dailyPowerList :
                                dataSnapshot.child(DB_SPELLS_CHILD_DAILY_POWER_LISTS).getChildren()){
                            String dailyPowerListId = dailyPowerList.getValue(String.class);

                            //remove references from $daily_power_lists
                            childUpdates.put(
                                    DB_DAILY_POWER_LIST_TREE_NAME + "/"
                                    + dailyPowerListId + "/"
                                    + DB_DAILY_POWER_LIST_CHILD_SPELLS + "/"
                                    + powerId, null);

                            //remove reference from daily_spell_groups if power has group name
                            if(groupName != null && !"".equals(groupName)){
                                childUpdates.put(
                                        DB_DAILY_SPELL_GROUPS_TREE_NAME + "/"
                                        + dailyPowerListId + "/"
                                        + groupName + "/"
                                        + powerId, null);
                            }
                        }

                        //remove references from $spell_lists
                        String powerListId = dataSnapshot.child(DB_SPELLS_CHILD_POWER_LIST).getValue(String.class);
                        if(powerListId != null && !powerListId.equals("")){
                            childUpdates.put(
                                    DB_SPELL_LIST_TREE_NAME + "/"
                                    + powerListId + "/"
                                    + DB_SPELL_LIST_CHILD_SPELLS + "/"
                                    + powerId, null);

                            //remove references from $spell_groups
                            //note, there is no entry in spell_groups if power is not in a list of powers
                            if(groupName != null) {
                                childUpdates.put(
                                        DB_SPELL_GROUPS_TREE_NAME + "/"
                                                + powerListId + "/"
                                                + groupName + "/"
                                                + powerId ,null);
                            }
                        }

                        //then remove the entry from spells
                        childUpdates.put(
                                DB_SPELL_TREE_NAME + "/"
                                + powerId, null);

                        firebaseDatabase.getReference().updateChildren(childUpdates);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "deletePower: error occurred: " + databaseError.toString());
                    }
                });
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
                //note, spell's group is only added to spell_groups when spell is added to a list
                //of spells, there is no need to have spells that aren't in a list in this table
                if(!"".equals(spell.getGroupName())){
                    childUpdates.put(
                            DB_SPELL_GROUPS_TREE_NAME
                                    + "/"
                                    + powerListId
                                    + "/"
                                    + spell.getGroupName()
                                    + "/"
                                    + spellId
                                    + "/"
                                    + spell.getName(), true);
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

    /**
     * Remove a listener in spell_groups/$powerListId/$powerGroupName
     * @param listener the listener to be removed
     * @param powerGroupName Name of the power group where listener should be removed from
     * @param powerListId Name of the power list where power group is
     */
    public static void removePowerGroupListener(ChildEventListener listener,
                                                String powerGroupName, String powerListId) {
        firebaseDatabase
                .getReference(DB_SPELL_GROUPS_TREE_NAME)
                .child(powerListId)
                .child(powerGroupName)
                .removeEventListener(listener);

    }


    /**
     * ChildEventListener for spell_groups/$id/$group_name,
     * when it notices new children under there it launches single value event
     * listener to fetch the actual power and passes it to PowerListPresenter
     */
    private static class powerValueListener implements ChildEventListener{
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String powerId = dataSnapshot.getKey();
            Log.d(TAG, "powerValueListener: power ID " + powerId);
            //launch the query to get spell from spells with the ID
            firebaseDatabase
                    .getReference(DB_SPELL_TREE_NAME)
                    .child(powerId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //give the single spell to presenter
                            Spell spell = dataSnapshot.getValue(Spell.class);
                            spell.setSpellId(dataSnapshot.getKey());
                            PowerListPresenter.handleSpellFromDatabase(spell);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "error at powerValueListener addListenerForSingleValueEvent: "
                                    + databaseError.toString());
                        }
                    });
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            // TODO: 16.6.2017 this should maybe be implemented
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "powerValueListener: onChildRemoved dataSnapshot key " + dataSnapshot.getKey());
            Log.d(TAG, "powerValueListener: onChildRemoved dataSnapshot value " + dataSnapshot.getValue());
            //get the whole power from DB
            firebaseDatabase
                    .getReference(DB_SPELL_TREE_NAME)
                    .child(dataSnapshot.getKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            PowerListPresenter.handleSpellDeletion(dataSnapshot.getValue(Spell.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "error at powerValueListener: " + databaseError.toString());
        }
    }
}
