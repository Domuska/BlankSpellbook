package tomi.piipposoft.blankspellbook.PowerList;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.ChildEventListener;

import java.util.ArrayList;

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

    //listener to the power list that is displayed
    private static ChildEventListener powerListListener;

    private static ArrayMap<String, ArrayList<Spell>> powerGroups = new ArrayMap<>();
    private static ArrayMap<String, ArrayList<Spell>> previousPowerGroups;
    //private static ArrayList<ChildEventListener> powerGroupListeners = new ArrayList<>();
    private static ArrayMap<ChildEventListener, String> powerGroupListeners = new ArrayMap<>();

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
        Log.d(TAG, "handleSpellFromDatabase: name: " + spell.getName() + " group: " + spell.getGroupName());
        //mPowerListActivity.addSpellToList(spell);
        //add the power to the list of powers in the powerGroups map
        powerGroups.get(spell.getGroupName()).add(spell);
        //pass the power to view to display it as it wishes
        mPowerListActivity.addSpellToList(spell);
    }

    public static void handleSpellDeletion(Spell spell){
        mPowerListActivity.removeSpellFromList(spell);
    }

    public static void handlePowerGroup(String powerGroupName) {
        //add the power group name to the map if it's not there yet
        Log.d(TAG, "handlePowerGroup: got a new group " + powerGroupName);
        if(!powerGroups.containsKey(powerGroupName))
            powerGroups.put(powerGroupName, new ArrayList<Spell>());
    }

    public static void handlePowerGroupListener(ChildEventListener childEventListener, String groupName) {
        Log.d(TAG, "handlePowerGroupListener: added new listener: " + childEventListener);
        powerGroupListeners.put(childEventListener, groupName);
    }

    /**
     * Check if presenter is already listening to a certain power group
     * in spell_groups/$powerlistId/$powerGroup
     * @param powerGroupName name of the powerGroup
     * @return true if already listens to the group
     */
    public static boolean listeningToGroup(String powerGroupName) {
        return powerGroups.containsKey(powerGroupName);
    }

    // FROM POWERLISTCONTRACT

    @Override
    public void openPowerDetails(@NonNull String itemId, @Nullable String itemName,
                                 @Nullable View transitioningView) {

        //first remove the listeners
        if(powerListListener != null) {
            Log.d(TAG, "openPowerDetails: removing listener: " + powerListListener.toString());
            DataSource.removePowerListPowerListener(powerListListener, powerListId);
        }
        Log.d(TAG, "openPowerDetails: powerGroupListeners size: " + powerGroupListeners.size());
        for(int i = 0; i < powerGroupListeners.size(); i++) {
            Log.d(TAG, "openPowerDetails: removing group listener at index " + i);
            ChildEventListener listener = powerGroupListeners.keyAt(i);
            Log.d(TAG, "openPowerDetails: listener removed is " + listener);
            DataSource.removePowerGroupListener(
                    listener, powerGroupListeners.get(listener), powerListId);
        }
        powerGroupListeners.clear();
        previousPowerGroups = powerGroups;
        powerGroups.clear();

        //decide if we open new power input screen or just show existing power details
        if(itemId.equals(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS)) {
            Log.d(TAG, "openPowerDetails: removing listener: " + powerListListener.toString());
            mPowerListActivity.showNewPowerUI();
        }
        else {
            mPowerListActivity.showPowerDetailsUI(itemId, itemName, powerListId, transitioningView);
        }
    }

    @Override
    public void getSpellList(String powerListId) {
        //remove the old listener
        if(powerListListener != null) {
            Log.d(TAG, "getSpellList: removing old power list listener");
            DataSource.removePowerListPowerListener(powerListListener, powerListId);
        }
        powerListListener = DataSource.attachPowerGroupListeners(powerListId);
        Log.d(TAG, "getSpellList: added new listener: " + powerListListener.toString());
    }

    @Override
    public void resumeActivity() {

    }

    @Override
    public void pauseActivity() {
        Log.d(TAG, "pauseActivity: activity pausing, removing listener");
        DataSource.removePowerListPowerListener(powerListListener, powerListId);
        for(int i = 0; i < powerGroupListeners.size(); i++) {
            ChildEventListener listener = powerGroupListeners.keyAt(i);
            Log.d(TAG, "pauseActivity: group listener removed is " + listener);
            DataSource.removePowerGroupListener(
                    listener, powerGroupListeners.get(listener), powerListId);
        }
        powerGroupListeners.clear();
        powerListListener = null;
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
