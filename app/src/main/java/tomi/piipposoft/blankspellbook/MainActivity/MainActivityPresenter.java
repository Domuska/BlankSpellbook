package tomi.piipposoft.blankspellbook.MainActivity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;


import com.google.firebase.database.ChildEventListener;

import java.util.ArrayList;

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
        MainActivityContract.PagerAdapterListener{

    private static MainActivityContract.View mMainActivityView;

    private static final String TAG = "MainActivityPresenter";

    private static MainActivityPresenter thisInstance;
    private static ChildEventListener powerListListener;
    private static ChildEventListener dailyPowerListListener;
    private static ChildEventListener powersListener;
    private int currentlySelectedList;

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

    /**
     * handle a new power list from DB, give it to View
     * @param name name of the power list
     * @param id ID of the power list
     */
    public static void handleNewPowerList(String name, String id){
        Log.d(TAG, "handleNewPowerList: name: " + name + " id: " + id);
        mMainActivityView.addPowerListData(name, id);
    }

    public static void handleRemovedPowerList(String powerListName, String id) {
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
        if(isPowerList)
            mMainActivityView.addPowerNameToPowerList(powerName, powerListId);
        else
            mMainActivityView.addPowerNameToDailyPowerList(powerName, powerListId);
    }

    public static void handleRemovedDailyPowerList(String dailyPowerListName, String id) {
        mMainActivityView.removeDailyPowerListData(dailyPowerListName, id);
    }

    public static void handleNewPower(@NonNull Spell power, @Nullable String powerListName) {
        Log.d(TAG, "got new power in handleNewPower: " + power.getName()
                + " with power list name: " + powerListName);
        mMainActivityView.addNewPowerToList(power, powerListName);
    }

    public static void handlePowerRemoved(Spell power) {
        mMainActivityView.removePowerFromList(power);
    }


    //FROM MainActivityContract.FragmentUserActionListener


    @Override
    public void onPowerListClicked(String listName, String listId, View originView) {
        if(currentlySelectedList == POWER_LISTS_SELECTED)
            mMainActivityView.startPowerListActivity(listName, listId, originView);
        else
            mMainActivityView.startDailyPowerListActivity(listName, listId);
    }

    @Override
    public void onPowerClicked(String powerId, String powerListId) {
        mMainActivityView.startPowerDetailsActivity(powerId, powerListId);
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
