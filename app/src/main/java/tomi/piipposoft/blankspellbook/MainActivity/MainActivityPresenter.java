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

/**
 * Created by Domu on 17-Apr-16.
 */
public class MainActivityPresenter extends DrawerPresenter
        implements DrawerContract.UserActionListener,
        MainActivityContract.UserActionListener,
        MainActivityContract.FragmentListActionListener {

    private static MainActivityContract.View mMainActivityView;

    private static final String TAG = "MainActivityPresenter";

    private static MainActivityPresenter thisInstance;
    private static ChildEventListener powerListListener;
    private static ChildEventListener dailyPowerListListener;
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
        //attach listeners to spells, power lists and daily power lists
        if(powerListListener == null)
            powerListListener = DataSource.attachPowerListListener(DataSource.MAINACTIVITYPRESENTER);
        else {
            Log.d(TAG, "resumeActivity: powerListListener is not null");
            DataSource.getPowerLists(DataSource.MAINACTIVITYPRESENTER);
        }
        //attach listener for daily power lists

        if(dailyPowerListListener == null)
            dailyPowerListListener = DataSource.attachDailyPowerListListener(DataSource.MAINACTIVITYPRESENTER);
        else{
            Log.d(TAG, "resumeActivity: dailyPowerListListener is not null");
            DataSource.getDailyPowerLists(DataSource.MAINACTIVITYPRESENTER);
        }
    }

    @Override
    public void pauseActivity() {
        //remove the listeners
        Log.d(TAG, "in pauseActivity");
        DataSource.removePowerListListener(powerListListener);
        DataSource.removeDailyPowerListListener(dailyPowerListListener);
    }

    @Override
    public void userSwitchedTo(int selectedList) {
        currentlySelectedList = selectedList;
    }

    public static void handleNewPowerList(String name, String id, ArrayList<String> groupNames){
        Log.d(TAG, "handleNewListItem: " + name);
        mMainActivityView.addPowerListData(name, id, groupNames);
    }

    public static void handleNewDailyPowerList(String name, String id, ArrayList<String> groupNames){
        Log.d(TAG, "handleNewDailyPowerList: " + name);
        mMainActivityView.addDailyPowerListData(name, id, groupNames);
    }

    public static void handleRemovedDailyPowerList(String dailyPowerListName, String id) {
        mMainActivityView.removeDailyPowerListData(dailyPowerListName, id);
    }

    public static void handleRemovedPowerList(String powerListName, String id) {
        mMainActivityView.removePowerListData(powerListName, id);
    }


    //FROM POWERLISTCONTRACT


    @Override
    public void onPowerListClicked(String listName, String listId) {
        if(currentlySelectedList == POWER_LISTS_SELECTED)
            mMainActivityView.startPowerListActivity(listName, listId);
        else
            mMainActivityView.startDailyPowerListActivity(listName, listId);
    }
}
