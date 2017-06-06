package tomi.piipposoft.blankspellbook.MainActivity;

import android.support.annotation.NonNull;
import android.util.Log;


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
        MainActivityContract.FragmentListActionListener,
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
        if(powerListListener != null)
            DataSource.removePowerListListener(powerListListener);
        if(dailyPowerListListener != null)
            DataSource.removeDailyPowerListListener(dailyPowerListListener);
        if(powersListener != null)
            DataSource.removePowersListener(powersListener);
    }

    @Override
    public void userSwitchedTo(int selectedList) {
        currentlySelectedList = selectedList;
    }

    public static void handleNewPowerList(String name, String id, ArrayList<String> groupNames){
        Log.d(TAG, "handleNewListItem: " + name);
        mMainActivityView.addPowerListData(name, id, groupNames);
    }

    public static void handleRemovedPowerList(String powerListName, String id) {
        mMainActivityView.removePowerListData(powerListName, id);
    }

    public static void handleNewDailyPowerList(String name, String id, ArrayList<String> groupNames){
        Log.d(TAG, "handleNewDailyPowerList: " + name);
        mMainActivityView.addDailyPowerListData(name, id, groupNames);
    }

    public static void handleRemovedDailyPowerList(String dailyPowerListName, String id) {
        mMainActivityView.removeDailyPowerListData(dailyPowerListName, id);
    }

    public static void handleNewPower(Spell power) {
        Log.d(TAG, "got new powerin handleNewPower: " + power.getName()
                + " group: " + power.getGroupName());
        mMainActivityView.addNewPowerToList(power);
    }

    public static void handlePowerRemoved(Spell power) {
        mMainActivityView.removePowerFromList(power);
    }


    //FROM MainActivityContract.FragmentListActionListener


    @Override
    public void onPowerListClicked(String listName, String listId) {
        if(currentlySelectedList == POWER_LISTS_SELECTED)
            mMainActivityView.startPowerListActivity(listName, listId);
        else
            mMainActivityView.startDailyPowerListActivity(listName, listId);
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
            Log.d(TAG, "resumeActivity: dailyPowerListListener is not null");
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
            Log.d(TAG, "resumeActivity: powersListener is not null");
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
            Log.d(TAG, "resumeActivity: powerListListener is not null");
            DataSource.getPowerLists(DataSource.MAINACTIVITYPRESENTER);
        }
    }
}
