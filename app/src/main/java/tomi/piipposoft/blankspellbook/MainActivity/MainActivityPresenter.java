package tomi.piipposoft.blankspellbook.MainActivity;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;


import com.google.firebase.database.ChildEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.Drawer.DrawerPresenter;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Utils.SharedPreferencesHandler;
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

    static final int DAILY_POWER_LISTS_SELECTED = 0;
    static final int POWER_LISTS_SELECTED = 1;
    static final int SPELLS_SELECTED = 2;

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
    private static ArrayList<Spell> allPowers = new ArrayList<>();
    private static ArrayList<Spell> displayedPowers;
    private static ArrayMap<String, ArrayList<Spell>> powerGroupNamesMap = new ArrayMap<>();
    private static ArrayMap<String, ArrayList<Spell>> powerListNamesMap = new ArrayMap<>();
    private ArrayList<String> groupsSpellFilters = new ArrayList<>();
    private ArrayList<String> powerListsSpellFilters = new ArrayList<>();

    //define the allowed values for the spell filtering methods
    @IntDef({FILTER_BY_GROUP_NAME, FILTER_BY_POWER_LIST_NAME})
    public @interface FilterType{}
    public static final int FILTER_BY_GROUP_NAME = 1;
    public static final int FILTER_BY_POWER_LIST_NAME = 2;

    //boolean for whether we take cross-section or join when filtering powers in powersFragment
    private boolean filterByCrossSection;

    private MainActivityPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull MainActivityContract.View mainActivityView,
            @NonNull DrawerHelper drawerHelper){
        super(dbHelper, drawerHelper, (DrawerContract.ViewActivity) mainActivityView);
        mMainActivityView = mainActivityView;
        //get filter type user has set in settings
        filterByCrossSection =
                SharedPreferencesHandler.getFilterSPellByCrossSection((Activity)mainActivityView);
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
        powerGroupNamesMap = new ArrayMap<>();
        powerListNamesMap = new ArrayMap<>();
        allPowers = new ArrayList<>();
        displayedPowers = null;
    }

    @Override
    public void userSwitchedTo(int selectedList) {
        currentlySelectedList = selectedList;
    }

    @Override
    public ArrayList<String> getGroupNamesForFilter() {
        ArrayList<String> names = new ArrayList<>();
        for(Map.Entry<String, ArrayList<Spell>> groupName : powerGroupNamesMap.entrySet()){
            names.add(groupName.getKey());
            Log.d(TAG, "getGroupNamesForFilter: group name " + groupName.getKey());
        }
        return names;
    }

    @Override
    public ArrayList<String> getPowerListNamesForFilter() {
        /*ArrayList<String> list = new ArrayList<>();
        for(int i = 50; i < 100; i++){
            list.add(i + "");
        }
        return list;*/
        ArrayList<String> names = new ArrayList<>();
        for(Map.Entry<String, ArrayList<Spell>> groupName : powerListNamesMap.entrySet()){
            names.add(groupName.getKey());
        }
        return names;
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
        allPowers.add(power);

        if(powerListName != null) {
            //add the name of power list power is in to the map for filtering later
            if (powerListNamesMap.containsKey(powerListName))
                powerListNamesMap.get(powerListName).add(power);
            else {
                ArrayList<Spell> list = new ArrayList<>();
                list.add(power);
                powerListNamesMap.put(powerListName, list);
            }
            Log.d(TAG, "added power " + power.getName() + " with power list name " + powerListName);
        }

        //add the power's group name to the map for filtering later
        String groupName = power.getGroupName();
        if (groupName != null && !groupName.equals("")) {
            if (powerGroupNamesMap.containsKey(groupName))
                powerGroupNamesMap.get(groupName).add(power);
            else {
                ArrayList<Spell> list = new ArrayList<>();
                list.add(power);
                powerGroupNamesMap.put(groupName, list);
            }
            Log.d(TAG, "added power " + power.getName() + " with group name " + power.getGroupName());
        }

        mMainActivityView.addNewPowerToList(power, powerListName);

    }

    public static void handlePowerRemoved(Spell power) {
        mMainActivityView.removePowerFromList(power);
    }


    /**
     * Filtering the powers that should be displayed in View. This filter will remove the
     * powers that do not satisfy the criteria (take a cross-section). For joining the results
     * of the filters, see {@link #filterDisplayedPowersJoin(String, int)} method
     * @param filterText Name of the power list or group name that should be used as filter
     * @param filterCategory Whether the first argument is group name or power list name, see {@link FilterType}
     */
    private void filterDisplayedPowersCrossSection(
            @NonNull String filterText, @FilterType int filterCategory){
        if(displayedPowers == null && allPowers != null) {
            displayedPowers = new ArrayList<>();
            displayedPowers.addAll(allPowers);
        }

        //save the groupName filter so we can restore the filtered list if needed

        ArrayList<Spell> powersInList;
        if(filterCategory == FILTER_BY_GROUP_NAME) {
            powersInList = powerGroupNamesMap.get(filterText);
            // TODO: 20.7.2017 remove this if when we actually restore filter state when resuming activity
            if(!groupsSpellFilters.contains(filterText))
                groupsSpellFilters.add(filterText);
        }
        else if(filterCategory == FILTER_BY_POWER_LIST_NAME) {
            powersInList = powerListNamesMap.get(filterText);
            // TODO: 20.7.2017 remove this if when we actually restore filter state when resuming activity
            if(!powerListsSpellFilters.contains(filterText))
                powerListsSpellFilters.add(filterText);
        }
        else
            throw new RuntimeException("Use either FILTER_BY_GROUP_NAME or FILTER_BY_POWER_LIST_NAME as filterType");


        //since we have a bad .equals method in spells, we need to check by power list ID aswell
        String powerListId = powersInList.get(0).getPowerListId();
        if (powerListId == null)
            powerListId = "";
        //remove the powers that are not in the list fetched earlier by the group or power list name
        for(Iterator<Spell> iterator = displayedPowers.iterator(); iterator.hasNext();){
            Spell power = iterator.next();
            if(!powersInList.contains(power) ||
                    (filterCategory == FILTER_BY_POWER_LIST_NAME && !powerListId.equals(power.getPowerListId()))){
                iterator.remove();
            }
        }
        allPowers.size();
        //mMainActivityView.showFilteredPowers(displayedPowers);
        mMainActivityView.setPowerListData(displayedPowers);
    }

    /**
     * Filtering the powers that should be displayed in View. This filter will add
     * to displayedPowers the spells that satisfy the filter criteria. For taking cross section of the results
     * of the filters, see {@link #filterDisplayedPowersCrossSection(String, int)} method
     * @param filterText Group name or power list name used for filtering
     * @param filterCategory Whether the first argument is group name or power list name, see {@link FilterType}
     */
    private void filterDisplayedPowersJoin(
            @NonNull String filterText, @FilterType int filterCategory){

        if(displayedPowers == null)
            displayedPowers = new ArrayList<>();

        ArrayList<Spell> powersInList;
        if(filterCategory == FILTER_BY_GROUP_NAME) {
            powersInList = powerGroupNamesMap.get(filterText);
            // TODO: 20.7.2017 remove this if when we actually restore filter state when resuming activity
            if(!groupsSpellFilters.contains(filterText))
                groupsSpellFilters.add(filterText);
        }
        else if(filterCategory == FILTER_BY_POWER_LIST_NAME) {
            powersInList = powerListNamesMap.get(filterText);
            // TODO: 20.7.2017 remove this if when we actually restore filter state when resuming activity
            if(!powerListsSpellFilters.contains(filterText))
                powerListsSpellFilters.add(filterText);
        }
        else
            throw new RuntimeException("Use either FILTER_BY_GROUP_NAME or FILTER_BY_POWER_LIST_NAME as filterType");

        //since we have a bad Spell.equals method, have to make sure we don't add duplicates
        for(Iterator<Spell> iterator = powersInList.iterator(); iterator.hasNext();){
            Spell power = iterator.next();
            for(Spell displayedPower : displayedPowers){
                if(power.getSpellId().equals(displayedPower.getSpellId()))
                    iterator.remove();
            }
        }

        displayedPowers.addAll(powersInList);
        mMainActivityView.setPowerListData(displayedPowers);
    }


    @Override
    public void removeFilter(@NonNull String filterName, @FilterType int filterType){

        if(filterType == FILTER_BY_GROUP_NAME)
            groupsSpellFilters.remove(filterName);
        else if(filterType == FILTER_BY_POWER_LIST_NAME)
            powerListsSpellFilters.remove(filterName);
        else
            throw new RuntimeException("Use either FILTER_BY_GROUP_NAME or FILTER_BY_POWER_LIST_NAME as filterType");

        displayedPowers.clear();
        displayedPowers.addAll(allPowers);

        //re-calculate the powers that should be shown by applying all filters again. Might be not too optimal.
        if(filterByCrossSection) {
            for (String filter : groupsSpellFilters) {
                filterDisplayedPowersCrossSection(filter, FILTER_BY_GROUP_NAME);
            }
            for(String filter: powerListsSpellFilters){
                filterDisplayedPowersCrossSection(filter, FILTER_BY_POWER_LIST_NAME);
            }
        }
        else{
            for(String filter: groupsSpellFilters){
                filterDisplayedPowersJoin(filter, FILTER_BY_GROUP_NAME);
            }
            for(String filter: powerListsSpellFilters){
                filterDisplayedPowersJoin(filter, FILTER_BY_POWER_LIST_NAME);
            }
        }

        mMainActivityView.setPowerListData(displayedPowers);
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
        Log.d(TAG, "filterGroupsAndPowersWithPowerListName: powerListName: " + powerListName);
        ArrayList<Spell> filteredPowers = powerListNamesMap.get(powerListName);
        Log.d(TAG, "filterGroupsAndPowersWithPowerListName: size of powers list: " + filteredPowers.size());
        ArrayList<String> groupName = new ArrayList<>();
        for(Spell power : filteredPowers){
            groupName.add(power.getGroupName());
        }
        Log.d(TAG, "groups that should be shown: " + groupName);

        if(filteredPowers != null){
            /*ArrayList<Spell> filteredPowers = new ArrayList<>();
            for(Spell power : powers){
                groupNames.add(power.getGroupName());
            }*/
            //mMainActivityView.showFilteredPowers(filteredPowers);
            mMainActivityView.showFilteredPowerGroups(groupName);
            if(filterByCrossSection)
                filterDisplayedPowersCrossSection(powerListName, FILTER_BY_POWER_LIST_NAME);
            else
                filterDisplayedPowersJoin(powerListName, FILTER_BY_POWER_LIST_NAME);
        }
        //tell View to show only spells that are under the power list
        //and to tell filterFragment to show groups that are under the power list
    }

    @Override
    public void filterPowerListsAndPowersWithGroupName(@NonNull String groupName) {

        Log.d(TAG, "filterPowerListsAndPowersWithGroupName: groupName: " + groupName);
        ArrayList<Spell> filteredPowers = powerGroupNamesMap.get(groupName);
        Log.d(TAG, "filterPowerListsAndPowersWithGroupName: size of powers list: " + filteredPowers.size());

        // TODO: 17.7.2017 make a list of shown filtered powers that is further filtered here
        //so that we can apply both group name and power list name filters at same time

        if(filteredPowers != null) {
            //search in the power lists map the name of the power list this power belongs to
            ArrayList<String> powerListNames = new ArrayList<>();
            ArrayList<String> foundPowerListIds = new ArrayList<>();
            //iterate through every power for every entry in the map that contains lists... Eh.
            //Better than making a network call to get all the power list names and checking those?
            for (Spell power : filteredPowers) {
                //first check if we already have this power list id a list, no need to re-search the name
                Log.d(TAG, "power " + power.getName() + " belongs to group with ID " + power.getPowerListId());
                if (power.getPowerListId() != null && !foundPowerListIds.contains(power.getPowerListId())) {
                    Log.d(TAG, "power list ID not list of found IDs");
                    //if not, iterate the power list maps
                    for (Map.Entry<String, ArrayList<Spell>> entry : powerListNamesMap.entrySet()) {
                        String listName = entry.getKey();
                        //the list has this power and the name of the list is not already in the known names list
                        if (entry.getValue().contains(power) && !powerListNames.contains(listName)) {
                            Log.d(TAG, "power found in list with name " + entry.getKey());
                            powerListNames.add(listName);
                            foundPowerListIds.add(power.getPowerListId());
                        }
                    }
                }
            }
            Log.d(TAG, "power list names that should be shown: " + powerListNames);
            //mMainActivityView.showFilteredPowers(filteredPowers);
            if(filterByCrossSection)
                filterDisplayedPowersCrossSection(groupName, FILTER_BY_GROUP_NAME);
            else
                filterDisplayedPowersJoin(groupName, FILTER_BY_GROUP_NAME);

            mMainActivityView.showFilteredPowerLists(powerListNames);
        }
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
