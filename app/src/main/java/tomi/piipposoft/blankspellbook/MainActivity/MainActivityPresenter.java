package tomi.piipposoft.blankspellbook.MainActivity;

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
import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public class MainActivityPresenter extends DrawerPresenter
        implements DrawerContract.UserActionListener,
        MainActivityContract.UserActionListener,
        MainActivityContract.FragmentUserActionListener,
        MainActivityContract.FilterFragmentUserActionListener,
        MainActivityContract.ListeningStateInterface,
        MainActivityContract.preferencesInterface{

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
    private static ArrayMap<String, String> dailyPowerLists = new ArrayMap<>();
    private static ArrayMap<String, ArrayList<String>> powerNamesForPowerLists = new ArrayMap<>();

    //for storing the powers to handle filtering
    private static ArrayList<Spell> allPowers = new ArrayList<>();
    private static ArrayList<Spell> displayedPowers;

    //for storing which powers are under which power list or power group
    private static ArrayMap<String, ArrayList<Spell>> powerGroupNamesMap = new ArrayMap<>();
    private static ArrayMap<String, ArrayList<Spell>> powerListNamesMap = new ArrayMap<>();

    //selected group names and power list names, needed when removing a filter to re-calculate shown powers
    private ArrayList<String> groupsSpellFilters = new ArrayList<>();
    private ArrayList<String> powerListsSpellFilters = new ArrayList<>();

    //for storing the currently displayed power list names and group names
    private ArrayList<String> displayedGroupNames;
    private ArrayList<String> displayedPowerListNames;

    //define the allowed values for the spell filtering methods
    @IntDef({FILTER_BY_GROUP_NAME, FILTER_BY_POWER_LIST_NAME})
    @interface FilterType{}
    static final int FILTER_BY_GROUP_NAME = 1;
    static final int FILTER_BY_POWER_LIST_NAME = 2;

    //boolean for whether we take cross-section or join when filtering powers in powersFragment
    private boolean filterByCrossSection;

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
        //re-populate the powers list with data we should have
        allPowers.size();
        if(displayedPowers != null){
            for(Spell power : displayedPowers)
                mMainActivityView.addNewPowerToList(power, power.getPowerListName());
        }
        else {
            for(Spell power : allPowers){
                //Log.d(TAG, "adding power " + power.getName() + " to view from allPowers");
                mMainActivityView.addNewPowerToList(power, power.getPowerListName());
            }
        }
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
        //powerGroupNamesMap = new ArrayMap<>();
        //powerListNamesMap = new ArrayMap<>();
        //allPowers = new ArrayList<>();
        //displayedPowers = null;
    }

    @Override
    public void userSwitchedTo(int selectedList) {
        currentlySelectedList = selectedList;
    }

    @Override
    public ArrayList<String> getGroupNamesForFilter() {
        if(displayedGroupNames == null) {
            displayedGroupNames = new ArrayList<>();
            for (Map.Entry<String, ArrayList<Spell>> groupName : powerGroupNamesMap.entrySet()) {
                displayedGroupNames.add(groupName.getKey());
                Log.d(TAG, "getGroupNamesForFilter: group name " + groupName.getKey());
            }
        }
        return displayedGroupNames;
    }

    @Override
    public ArrayList<String> getSelectedGroupsForFilter() {
        return groupsSpellFilters;
    }

    @Override
    public ArrayList<String> getPowerListNamesForFilter() {
        if(displayedPowerListNames == null) {
            displayedPowerListNames = new ArrayList<>();
            for(Map.Entry<String, ArrayList<Spell>> groupName : powerListNamesMap.entrySet()){
                displayedPowerListNames.add(groupName.getKey());
            }
        }
        return displayedPowerListNames;
    }

    @Override
    public ArrayList<String> getSelectedPowerListsForFilter() {
        return powerListsSpellFilters;
    }

    /**
     * handle a new power list from DB, give it to View
     * @param name name of the power list
     * @param id ID of the power list
     */
    public static void handleNewPowerList(String name, String id){
        Log.d(TAG, "handleNewPowerList: name: " + name + " id: " + id);
        //add the power list if it's not already known, might be if we are returning to activity
        //and re-attaching listener
        if(!powerLists.containsKey(id)) {
            Log.d(TAG, "handleNewPowerList: power list not yet in list");
            powerLists.put(id, name);
            mMainActivityView.addPowerListData(name, id);
        }
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
        if(!dailyPowerLists.containsKey(id)) {
            dailyPowerLists.put(id, name);
            mMainActivityView.addDailyPowerListData(name, id);
        }
    }

    /**
     * handle a new power name from power list, given to View that should display it with a power list
     * @param powerName Name of the power
     * @param powerListId ID of the power list where this power is
     * @param isPowerList flag whether this is a power list or a daily power list
     */
    public static void handleNewPowerNameForList(String powerName, String powerListId, boolean isPowerList) {
        /*Log.d(TAG, "handleNewPowerNameForList: powerName: " + powerName
                + " powerListId: " + powerListId + " isPowerList: " + isPowerList);*/
        if(isPowerList) {
            //add the power name to the cached list
            if(!powerNamesForPowerLists.containsKey(powerListId)){
                ArrayList<String> list = new ArrayList<>();
                list.add(powerName);
                powerNamesForPowerLists.put(powerListId, list);
                mMainActivityView.addPowerNameToPowerList(powerName, powerListId);
            }
            else{
                //check if the name is already in the list, can happen when activity resumed
                //and we have cached data
                if(!powerNamesForPowerLists.get(powerListId).contains(powerName)) {
                    powerNamesForPowerLists.get(powerListId).add(powerName);
                    //give View the data to show
                    mMainActivityView.addPowerNameToPowerList(powerName, powerListId);
                }
            }

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

        //since Spell.equals is badly defined, go through the list like this to see if spell is already in it
        boolean spellAlreadyInList = false;
        for(Spell power1 : allPowers){
            if (power1.getSpellId().equals(power.getSpellId()))
                    spellAlreadyInList = true;
        }
        //Log.d(TAG, "handleNewPower is power already in allPowers? " + spellAlreadyInList);
        //add power to a list of powers for filtering later
        if(!spellAlreadyInList) {
            allPowers.add(power);

            if (powerListName != null) {
                //add the name of power list power is in to the map for filtering later
                if (powerListNamesMap.containsKey(powerListName))
                    powerListNamesMap.get(powerListName).add(power);
                else {
                    ArrayList<Spell> list = new ArrayList<>();
                    list.add(power);
                    powerListNamesMap.put(powerListName, list);
                }
            /*not sure if powerlistname should be set at all. Maybe re-work so
             we can just use the .equals method properly? This comes as a problem with the
             "poor" .equals method in in filterDisplayedPowersCrossSection & filterDisplayedPowersJoin.
             As it is, in those methods we need to do convoluted for-loops to get the power list name
             from the powerListNamesMap.
             Another solution might be that we re-work the map so we store spell-powerlistname instead
             of what we do now. This might even be a better idea.*/
                power.setPowerListName(powerListName);
                Log.d(TAG, "added power " + power.getName() + " with power list name " + powerListName);
            } else {
                power.setPowerListName("");
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
            //get the power list name... Three layers of for loops? Eh..
            /*for (Map.Entry<String, ArrayList<Spell>> entry : powerListNamesMap.entrySet()) {
                String listName = entry.getKey();
                for(Spell power2 : entry.getValue()){
                    if(entry.getValue().contains(power) && power.getPowerListId().equals(power2.getPowerListId()))
                        powersForMainActivity.put(power, listName);
                }
            }*/
        }

        /*ArrayMap<Spell, String> powersForMainActivity = new ArrayMap<>();
        for(Spell power : displayedPowers){
            powersForMainActivity.put(power, power.getPowerListName());
        }*/

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

        //if filters are empty, we should be displaying all powers and we need an empty list
        //to which we start adding new items to display.
        //Otherwise, we just add more items to be displayed in a list that already has something.
        if(displayedPowers == null ||
                (groupsSpellFilters).size() < 1 && powerListsSpellFilters.size() < 1)
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

        //since we have a bad Spell.equals method, have to make sure we don't add duplicates in this way
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

        //remove all powers from the list since we apply the filters again
        displayedPowers.clear();
        //filtering by cross-section will remove powers that should not be displayed,
        //while filtering by joining will add more powers to the list, so for the joining
        //function to work properly we will need an empty list to start with
        if(filterByCrossSection )
            displayedPowers.addAll(allPowers);

        //get the group and power list names again, they will be filtered later on again
        displayedGroupNames = null;
        displayedGroupNames = getGroupNamesForFilter();
        displayedPowerListNames = null;
        displayedPowerListNames = getPowerListNamesForFilter();

        if(groupsSpellFilters.size() > 0 || powerListsSpellFilters.size() > 0){
            //apply the filters again
            for (String filter : groupsSpellFilters) {
                filterPowerListsAndPowersWithGroupName(filter);
            }

            for (String filter : powerListsSpellFilters) {
                filterGroupsAndPowersWithPowerListName(filter);
            }
        }
        else{
            //if filtering by join, we need to add all the powers to the list (since they were not added earlier)
            if(!filterByCrossSection)
                displayedPowers.addAll(allPowers);
        }
        //show the power list names and group names in the fragment
        mMainActivityView.showFilteredPowerLists(displayedPowerListNames);
        mMainActivityView.showFilteredGroups(displayedGroupNames);

        //if both filters are empty, show all the powers in the list anyway
        if(groupsSpellFilters.size() < 1 && powerListsSpellFilters.size() < 1) {
            mMainActivityView.setPowerListData(displayedPowers);
        }


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
    public void onPowerClicked(String powerId, String powerListId,
                               View transitionOrigin, String powerListName) {
        mMainActivityView.startPowerDetailsActivity(powerId, powerListId, transitionOrigin, powerListName);
    }

    //from MainActivityContract.FilterFragmentUserActionListener

    @Override
    public void filterGroupsAndPowersWithPowerListName(@NonNull String powerListName) {
        //see what groups are under a power list
        ArrayList<Spell> filteredPowers = powerListNamesMap.get(powerListName);
        Log.d(TAG, "filterGroupsAndPowersWithPowerListName: size of powers list: " + filteredPowers.size());
        ArrayList<String> groupNames = new ArrayList<>();
        for(Spell power : filteredPowers){
            groupNames.add(power.getGroupName());
        }

        //check if the new group names are also in the list of currently displayed group names (take cross-section)
        for(Iterator<String> iterator = displayedGroupNames.iterator(); iterator.hasNext();){
            String groupName = iterator.next();
            if(!groupNames.contains(groupName))
                iterator.remove();
        }
        mMainActivityView.showFilteredGroups(displayedGroupNames);
        if(filterByCrossSection)
            filterDisplayedPowersCrossSection(powerListName, FILTER_BY_POWER_LIST_NAME);
        else
            filterDisplayedPowersJoin(powerListName, FILTER_BY_POWER_LIST_NAME);
    }

    @Override
    public void filterPowerListsAndPowersWithGroupName(@NonNull String groupName) {

        Log.d(TAG, "filterPowerListsAndPowersWithGroupName: groupName: " + groupName);
        ArrayList<Spell> filteredPowers = powerGroupNamesMap.get(groupName);
        Log.d(TAG, "filterPowerListsAndPowersWithGroupName: size of powers list: " + filteredPowers.size());

        //remove duplicates...? Check out later if this is necessary.
        // TODO: 24.7.2017 check out if this is necessary
        for(Iterator<String> iterator = displayedPowerListNames.iterator(); iterator.hasNext();){
            String powerListName = iterator.next();
            if(!displayedPowerListNames.contains(powerListName))
                iterator.remove();
        }

        if(filteredPowers != null) {
            //first remove from list powers that don't satisfy the current power list name filters

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

            //remove from displayedPowers the names that shouldn't be visible any more
            for(Iterator<String> iterator = displayedPowerListNames.iterator(); iterator.hasNext();){
                String powerListName = iterator.next();
                if(!powerListNames.contains(powerListName))
                    iterator.remove();
            }

            if(filterByCrossSection)
                filterDisplayedPowersCrossSection(groupName, FILTER_BY_GROUP_NAME);
            else
                filterDisplayedPowersJoin(groupName, FILTER_BY_GROUP_NAME);

            mMainActivityView.showFilteredPowerLists(displayedPowerListNames);
        }
    }


    //from MainActivityContract.ListeningStateInterface

    /**
     * Called by PagerAdapter when daily power list fragment has been created
     * Attaches a listener or fetches the data from DB and tells MainActivity to add data
     * to the fragment
     */
    @Override
    public void startListeningForDailyPowerLists() {

        for(Map.Entry<String, String> powerList : dailyPowerLists.entrySet()){
            mMainActivityView.addDailyPowerListData(powerList.getValue(), powerList.getKey());
        }
        //attach listener for daily power lists
        if(dailyPowerListListener == null)
            dailyPowerListListener = DataSource.attachDailyPowerListListener(DataSource.MAINACTIVITYPRESENTER);
        /*else{
            Log.d(TAG, "startListeningForDailyPowerLists: dailyPowerListListener is not null");
            DataSource.getDailyPowerLists(DataSource.MAINACTIVITYPRESENTER);
        }*/
    }


    /**
     * Called by PagerAdapter when powers fragment has been created
     * Attaches a listener or fetches the data from DB and tells MainActivity to add data
     * to the fragment
     */
    @Override
    public void startListeningForPowers() {
        //give view the cached data if we have it
        Log.d(TAG, "startListeningForPowers: powerLists size " + powerLists.size());
        for(Map.Entry<String, String> powerList : powerLists.entrySet()){
            //pass view the single power list
            String powerListId = powerList.getKey();
            mMainActivityView.addPowerListData(powerList.getValue(), powerListId);
            Log.d(TAG, "startListeningForPowers: power list " + powerListId + " added to mainActivity");

        }

        //listener for powers
        if(powersListener == null)
            powersListener = DataSource.attachPowerListener(DataSource.MAINACTIVITYPRESENTER);
        else{
            //is this even ever reached? Think this is called only once when the fragment is
            //initialized, and then powersListener is null.
            Log.d(TAG, "startListeningForPowers: powersListener is not null");
            DataSource.getPowers(DataSource.MAINACTIVITYPRESENTER);
        }
    }

    /**
     * Called by PagerAdapter when power list fragment has been created
     * Attaches a listener or fetches the data from DB and tells MainActivity to add data
     * to the fragment
     */
    @Override
    public void startListeningForPowerLists() {
        //pass cached power list names and ids to view
        for(Map.Entry<String, String> powerList : powerLists.entrySet()){
            String powerListId = powerList.getKey();
            //mMainActivityView.addPowerListData(powerList.getValue() + " CACHED", powerListId);
            mMainActivityView.addPowerListData(powerList.getValue(), powerListId);
            //pass the power name data to be displayed under power list cards or where-ever
            if(powerNamesForPowerLists.containsKey(powerListId)){
                Log.d(TAG, "startListeningForPowerLists: power list id " + powerListId + " in powerNamesForPowerLists");
                for(String powerName : powerNamesForPowerLists.get(powerListId)) {
                    //Log.d(TAG, "startListeningForPowerLists: adding power " + powerName + " to be shown in mainActivity");
                    mMainActivityView.addPowerNameToPowerList(powerName, powerListId);
                }
            }
        }
        //attach the listener
        if(powerListListener == null)
            powerListListener = DataSource.attachPowerListListener(DataSource.MAINACTIVITYPRESENTER);
    }


    //interface MainActivityContract.preferencesInterface

    @Override
    public void filterStyleChanged(boolean newValue) {
        filterByCrossSection = newValue;
    }
}
