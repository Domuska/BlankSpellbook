package tomi.piipposoft.blankspellbook.MainActivity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;


import com.google.firebase.database.ChildEventListener;

import java.util.ArrayList;
import java.util.Map;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.Drawer.DrawerPresenter;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public class MainActivityPresenter extends DrawerPresenter
        implements DrawerContract.UserActionListener,
        MainActivityContract.UserActionListener,
        MainActivityContract.FragmentUserActionListener,
        MainActivityContract.FilterFragmentUserActionListener,
        MainActivityContract.PagerAdapterListener{

    private static MainActivityContract.View mMainActivityView;

    private static final String TAG = "MainActivityPresenter";

    private static MainActivityPresenter thisInstance;
    private static ChildEventListener powerListListener;
    private static ChildEventListener dailyPowerListListener;
    private static ChildEventListener powersListener;
    private int currentlySelectedList;

    //for caching the data - prevent the list elements flashing in and out when returning to activity
    private static ArrayMap<String, String> powerLists = new ArrayMap<>();
    private static ArrayMap<String, ArrayList<String>> powersNamesInPowerLists = new ArrayMap<>();
    private static ArrayMap<String, String> dailyPowerLists = new ArrayMap<>();

    //for storing the powers to handle filtering
    private static ArrayList<Spell> displayedPowers = new ArrayList<>();
    private static ArrayMap<String, ArrayList<Spell>> powerGroupNames = new ArrayMap<>();
    private static ArrayMap<String, ArrayList<Spell>> powerListNames = new ArrayMap<>();


    public static final int DAILY_POWER_LISTS_SELECTED = 0;
    public static final int POWER_LISTS_SELECTED = 1;
    public static final int SPELLS_SELECTED = 2;


    private MainActivityPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull MainActivityContract.View mainActivityView,
            @NonNull DrawerHelper drawerHelper){
        super(dbHelper, drawerHelper, (DrawerContract.ViewActivity) mainActivityView);
        mMainActivityView = mainActivityView;
    }


    static MainActivityPresenter getInstance(@NonNull BlankSpellBookContract.DBHelper dbHelper,
                                             @NonNull DrawerHelper drawerHelper,
                                             @NonNull MainActivityContract.View mainActivityView){
        if(thisInstance == null) {
            Log.d(TAG, "no existing instance, creating new");
            thisInstance = new MainActivityPresenter(dbHelper, mainActivityView, drawerHelper);
        }
        else {
            Log.d(TAG, "thisInstance not null, setting mainActivityView");
            //Instance already exists, just save references to activity and drawer views
            mMainActivityView = mainActivityView;
            mDrawerView = drawerHelper;
        }
        return thisInstance;
    }

    // FROM MAINACTIVITYCONTRACT

    @Override
    public void resumeActivity() {
        //listeners used to be added here, now handled after fragments have been created in PagerAdapter
    }

    @Override
    public void pauseActivity() {
        //remove the listeners
        Log.d(TAG, "in pauseActivity");
        if(powerListListener != null) {
            DataSource.removePowerListListener(powerListListener);
            powerListListener = null;
        }
        if(dailyPowerListListener != null) {
            DataSource.removeDailyPowerListListener(dailyPowerListListener);
            dailyPowerListListener = null;
        }
        if(powersListener != null) {
            DataSource.removePowersListener(powersListener);
            powersListener = null;
        }
    }

    @Override
    public void userSwitchedTo(int selectedList) {
        currentlySelectedList = selectedList;
    }

    @Override
    public ArrayList<String> getGroupNamesForFilter() {
        ArrayList<String> list = new ArrayList<>();
        for(int i = 0; i < 50; i++){
            list.add(i + "");
        }
        return list;
    }

    @Override
    public ArrayList<String> getClassNamesForFilter() {
        ArrayList<String> list = new ArrayList<>();
        for(int i = 50; i < 100; i++){
            list.add(i + "");
        }
        return list;
    }

    /**
     * handle a new power list from DB, give it to View
     * @param name name of the power list
     * @param id ID of the power list
     */
    public static void handleNewPowerList(String name, String id){
        Log.d(TAG, "handleNewPowerList: name: " + name + " id: " + id);
        //if(powerLists.put(id, name) == null)
        mMainActivityView.addPowerListData(name, id);
    }

    public static void handleRemovedPowerList(String powerListName, String id) {
        powerLists.remove(id);
        mMainActivityView.removePowerListData(powerListName, id);
    }

    /**
     * handle a new power list from DB, pass that on to the View
     * @param name name of the power list
     * @param id ID of the power list
     */
    public static void handleNewDailyPowerList(String name, String id){
        Log.d(TAG, "handleNewDailyPowerList: name: " + name + " id: " + id);
        mMainActivityView.addDailyPowerListData(name, id);
    }

    /**
     * handle a new power name from power list, given to View that should display it with a power list
     * @param powerName Name of the power
     * @param powerListId ID of the power list where this power is
     * @param isPowerList flag whether this is a power list or a daily power list
     */
    public static void handleNewPowerNameForList(String powerName, String powerListId, boolean isPowerList) {
        Log.d(TAG, "handleNewPowerNameForList: powerName: " + powerName
                + " powerListId: " + powerListId + " isPowerList: " + isPowerList);
        if(isPowerList) {
            //add the power name to the cached list
            if(powersNamesInPowerLists.get(powerListId) == null){
                ArrayList<String> list = new ArrayList<>();
                list.add(powerName);
                powersNamesInPowerLists.put(powerListId, list);
            }
            else{
                powersNamesInPowerLists.get(powerListId).add(powerName);
            }
            //give View the data to show
            mMainActivityView.addPowerNameToPowerList(powerName, powerListId);
        }
        else {
            mMainActivityView.addPowerNameToDailyPowerList(powerName, powerListId);
        }
    }

    public static void handleRemovedDailyPowerList(String dailyPowerListName, String id) {
        mMainActivityView.removeDailyPowerListData(dailyPowerListName, id);
    }

    /**
     * Handle a new power from the database
     * @param power an object of Spell class representing the power
     * @param powerListName name of the power list this power belongs to, can be null
     */
    public static void handleNewPower(@NonNull Spell power, @Nullable String powerListName) {
        //Log.d(TAG, "got new power in handleNewPower: " + power.getName() + " with power list name: " + powerListName);

        //add power to a list of powers for filtering later
        displayedPowers.add(power);

        if(powerListName != null) {
            //add the name of power list power is in to the map for filtering later
            if (powerListNames.containsKey(powerListName))
                powerListNames.get(powerListNames).add(power);
            else {
                ArrayList<Spell> list = new ArrayList<>();
                list.add(power);
                powerListNames.put(powerListName, list);
            }
        }

        //add the power's group name to the map for filtering later
        String groupName = power.getGroupName();
        if (groupName != null && !groupName.equals("")) {
            if (powerGroupNames.containsKey(groupName))
                powerGroupNames.get(power.getGroupName()).add(power);
            else {
                ArrayList<Spell> list = new ArrayList<>();
                list.add(power);
                powerGroupNames.put(groupName, list);
            }
        }
        mMainActivityView.addNewPowerToList(power, powerListName);
    }

    public static void handlePowerRemoved(Spell power) {
        mMainActivityView.removePowerFromList(power);
    }


    //FROM MainActivityContract.FragmentUserActionListener


    @Override
    public void onPowerListClicked(String listName, String listId,
                                   View originView, int powerListColor) {
        if(currentlySelectedList == POWER_LISTS_SELECTED)
            mMainActivityView.startPowerListActivity(listName, listId, originView, powerListColor);
        else
            mMainActivityView.startDailyPowerListActivity(listName, listId);
    }

    @Override
    public void onPowerClicked(String powerId, String powerListId, View transitionOrigin) {
        mMainActivityView.startPowerDetailsActivity(powerId, powerListId, transitionOrigin);
    }

    //from MainActivityContract.FilterFragmentUserActionListener

    @Override
    public void filterGroupsAndPowersWithPowerListName(@NonNull String powerListName) {
        //see what groups are under a power list
        ArrayList<Spell> powers = powerListNames.get(powerListName);
        if(powers != null){
            /*ArrayList<Spell> filteredPowers = new ArrayList<>();
            for(Spell power : powers){
                groupNames.add(power.getGroupName());
            }*/
            mMainActivityView.showFilteredPowers(powers);
            mMainActivityView.showFilteredPowerGroups(powers);
            // TODO: 17.7.2017 test 

        }
        //tell View to show only spells that are under the power list
        //and to tell filterFragment to show groups that are under the power list
    }

    @Override
    public void filterPowerListsAndPowersWithGroupName(@NonNull String groupName) {
        //see what power lists have a group with groupName
        //tell View to show only spells that have group with groupName
        //and to tell filterFragment to show power lists that have group with groupName
        // TODO: 17.7.2017 do the same as above
    }


    //from MainActivityContract.PagerAdapterListener

    /**
     * Called by PagerAdapter when daily power list fragment has been created
     * Attaches a listener or fetches the data from DB and tells MainActivity to add data
     * to the fragment
     */
    @Override
    public void onDailyPowerListFragmentCreated() {
        //attach listener for daily power lists
        if(dailyPowerListListener == null)
            dailyPowerListListener = DataSource.attachDailyPowerListListener(DataSource.MAINACTIVITYPRESENTER);
        else{
            Log.d(TAG, "onDailyPowerListFragmentCreated: dailyPowerListListener is not null");
            DataSource.getDailyPowerLists(DataSource.MAINACTIVITYPRESENTER);
        }
    }


    /**
     * Called by PagerAdapter when powers fragment has been created
     * Attaches a listener or fetches the data from DB and tells MainActivity to add data
     * to the fragment
     */
    @Override
    public void onPowersFragmentCreated() {
        //give view the cached data if we have it
        for(Map.Entry<String, String> powerList : powerLists.entrySet()){
            //pass view the single power list
            mMainActivityView.addPowerListData(powerList.getValue(), powerList.getKey());
        }

        //listener for powers
        if(powersListener == null)
            powersListener = DataSource.attachPowerListener(DataSource.MAINACTIVITYPRESENTER);
        else{
            Log.d(TAG, "onPowersFragmentCreated: powersListener is not null");
            DataSource.getPowers(DataSource.MAINACTIVITYPRESENTER);
        }
    }

    /**
     * Called by PagerAdapter when power list fragment has been created
     * Attaches a listener or fetches the data from DB and tells MainActivity to add data
     * to the fragment
     */
    @Override
    public void onPowerListFragmentCreated() {
        //attach listeners to spells, power lists and daily power lists
        if(powerListListener == null)
            powerListListener = DataSource.attachPowerListListener(DataSource.MAINACTIVITYPRESENTER);
        else {
            Log.d(TAG, "onPowerListFragmentCreated: powerListListener is not null");
            DataSource.getPowerLists(DataSource.MAINACTIVITYPRESENTER);
        }
    }
}
