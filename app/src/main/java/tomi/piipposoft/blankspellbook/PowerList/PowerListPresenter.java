package tomi.piipposoft.blankspellbook.PowerList;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.ChildEventListener;

import java.util.ArrayList;
import java.util.Map;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.PowerDetails.PowerDetailsActivity;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Utils.Spell;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.Drawer.DrawerPresenter;

/**
 * Created by Domu on 17-Apr-16.
 *
 * This could be made smarter, check DrawerPresenter's requirements. Now dbhelper is passed as
 * parameter to DrawerPresenter for some reason - could maybe be removed and just context
 * passed in or somesuch.
 */
public class PowerListPresenter extends DrawerPresenter implements
    PowerListContract.UserActionListener,
        DrawerContract.UserActionListener{

    private static final String TAG = "PowerListPresenter";
    private static PowerListContract.View mPowerListActivity;
    private static String powerListId;

    private String[] deletedPowerIds;

    private static PowerListPresenter thisInstance;

    //listener to the spell_groups in DB
    private static ChildEventListener groupsListener;
    //map of listeners to individual spells under spell_groups
    private static ArrayMap<String, ChildEventListener> powerGroupListeners = new ArrayMap<>();
    //power group name - list of powers under it
    private static ArrayMap<String, ArrayList<Spell>> powerGroups = new ArrayMap<>();
    private static String previousPowerListId;



    private PowerListPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull PowerListContract.View powerListActivity,
            @NonNull DrawerHelper drawerHelper,
            @NonNull String listId){
        // TODO: 8.5.2017 remove the sql database requirement when FireBase implementation complete
        super(dbHelper, drawerHelper, (DrawerContract.ViewActivity) powerListActivity);
        mPowerListActivity = powerListActivity;
        powerListId = listId;
    }

    static PowerListPresenter getInstance(@NonNull BlankSpellBookContract.DBHelper dbHelper,
                                                 @NonNull PowerListContract.View powerListActivity,
                                                 @NonNull DrawerHelper drawerHelper,
                                                 @NonNull String listId){

        if(thisInstance == null)
            thisInstance = new PowerListPresenter(dbHelper, powerListActivity, drawerHelper, listId);
        else {
            //Instance already exists, just save references to activity and drawer views
            mPowerListActivity = powerListActivity;
            mDrawerView = drawerHelper;
            powerListId = listId;
        }
        return thisInstance;
    }

    public static void handleSpellFromDatabase(Spell spell){
        Log.d(TAG, "handleSpellFromDatabase: name: " + spell.getName()
                + " group: " + spell.getGroupName());

        //we simply check if power is already in this group, if it is, discard it
        //note, here is actually a bug, if power's group name has changed,
        //the same power will be under two different groups (the old and new group)
        //since the old power is not removed at anywhere, however this should
        //not happen too often and it will not cause big enough problems to be worth it to fix for now


        /*for(Spell s : powerGroups.get(spell.getGroupName())){
            Log.d(TAG, "handleSpellFromDatabase: power group: " + s.getGroupName()
                    + " power name: " + s.getName());
        }*/

        if(!(powerGroups.get(spell.getGroupName()).contains(spell))) {
            //add the power to the list of powers in the powerGroups map
            powerGroups.get(spell.getGroupName()).add(spell);
            //pass the power to view to display it as it wishes
            mPowerListActivity.addSpellToList(spell);
        }
    }

    public static void handleSpellDeletion(Spell spell){
        Log.d(TAG, "handleSpellDeletion: removing power " + spell.getName());

        ArrayList<Spell> powerGroup = powerGroups.get(spell.getGroupName());
        powerGroup.remove(spell);

        //check if power was last one in its' group
        if(powerGroup.size() < 1){
            powerGroups.remove(spell.getGroupName());
            //remove the listener to this group

            ChildEventListener listener = powerGroupListeners.get(spell.getGroupName());
            DataSource.removePowerGroupListener(listener, spell.getGroupName(), powerListId);
            mPowerListActivity.removeSpellFromList(spell, true);
        }
        else
            mPowerListActivity.removeSpellFromList(spell, false);
    }

    public static void handlePowerGroup(String powerGroupName) {
        //add the power group name to the map if it's not there yet
        Log.d(TAG, "handlePowerGroup: got a new group " + powerGroupName);
        if(!powerGroups.containsKey(powerGroupName))
            powerGroups.put(powerGroupName, new ArrayList<Spell>());
    }

    public static void handlePowerGroupListener(ChildEventListener childEventListener, String groupName) {
        Log.d(TAG, "handlePowerGroupListener: added new listener: " + childEventListener);
        powerGroupListeners.put(groupName, childEventListener);
    }

    /**
     * Check if presenter is already listening to a certain power group
     * in spell_groups/$powerlistId/$powerGroup
     * @param powerGroupName name of the powerGroup
     * @return true if already listens to the group
     */
    public static boolean listeningToGroup(String powerGroupName) {
        return powerGroupListeners.containsKey(powerGroupName);
    }

    // FROM POWERLISTCONTRACT

    @Override
    public void openPowerDetails(@NonNull String itemId, @Nullable String itemName,
                                 @Nullable View transitioningView) {

        //first remove the listeners
        if(groupsListener != null) {
            Log.d(TAG, "openPowerDetails: removing listener: " + groupsListener.toString());
            DataSource.removePowerListPowerListener(groupsListener, powerListId);
        }
        Log.d(TAG, "openPowerDetails: powerGroupListeners size: " + powerGroupListeners.size());
        for(int i = 0; i < powerGroupListeners.size(); i++) {
            Log.d(TAG, "openPowerDetails: removing group listener at index " + i);
            String groupName = powerGroupListeners.keyAt(i);
            ChildEventListener listener = powerGroupListeners.get(groupName);
            Log.d(TAG, "openPowerDetails: listener removed is " + listener);
            DataSource.removePowerGroupListener(
                    listener, groupName, powerListId);
        }
        powerGroupListeners.clear();
        //save current data so it can be displayed if we return to this activity
        previousPowerListId = powerListId;

        //decide if we open new power input screen or just show existing power details
        if(itemId.equals(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS)) {
            Log.d(TAG, "openPowerDetails: removing listener: " + groupsListener.toString());
            mPowerListActivity.showNewPowerUI();
        }
        else {
            mPowerListActivity.showPowerDetailsUI(itemId, itemName, powerListId, transitioningView);
        }
    }

    @Override
    public void getPowersForDisplay(String powerListId) {
        //remove the old listener
        if(groupsListener != null) {
            Log.d(TAG, "getPowersForDisplay: removing old power list listener");
            DataSource.removePowerListPowerListener(groupsListener, powerListId);
        }

        //check if we are returning to previously shown list of powers or entering new one
        if(!powerListId.equals(previousPowerListId)) {
            powerGroups.clear();
        }

        //pass View the cached data so it can display it immediately (unless it was cleared above),
        //previously unknown powers will be added later when listener is attached
        for(Map.Entry<String, ArrayList<Spell>> powerGroup : powerGroups.entrySet()){
            //here we have a single map entry (a list of powers) that is iterated
            for(Spell power : powerGroup.getValue()){
                mPowerListActivity.addSpellToList(power);
            }
        }

        groupsListener = DataSource.attachPowerGroupListeners(powerListId);
        Log.d(TAG, "getPowersForDisplay: added new listener: " + groupsListener.toString());
    }

    @Override
    public void resumeActivity() {
        //attach listeners again
    }

    @Override
    public void pauseActivity() {
        Log.d(TAG, "pauseActivity: activity pausing, removing listener");
        DataSource.removePowerListPowerListener(groupsListener, powerListId);
        for(int i = 0; i < powerGroupListeners.size(); i++) {
            String groupName = powerGroupListeners.keyAt(i);
            ChildEventListener listener = powerGroupListeners.get(groupName);
            Log.d(TAG, "pauseActivity: listener removed is " + listener);
            DataSource.removePowerGroupListener(
                    listener, groupName, powerListId);
        }
        powerGroupListeners.clear();
        groupsListener = null;
    }

    @Override
    public void userDeletingPowersFromList(ArrayList<Spell> deletablePowers) {
        Log.d(TAG, "spells that should be selected for deletion:");

        deletedPowerIds = new String[deletablePowers.size()];
        for (int i = 0; i < deletedPowerIds.length; i++) {
            Log.d(TAG, "Spell id: " + deletablePowers.get(i).getSpellId());
            deletedPowerIds[i] = deletablePowers.get(i).getSpellId();
        }
        DataSource.removePowersFromList(deletedPowerIds, powerListId);
        mPowerListActivity.showPowerDeletedSnackBar();
    }

    @Override
    public void userPushingUndo() {
        DataSource.addPowersToList(deletedPowerIds, powerListId);
    }

}
