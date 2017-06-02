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
        MainActivityContract.UserActionListener {

    private static MainActivityContract.View mMainActivityView;

    private static final String TAG = "MainActivityPresenter";

    private static MainActivityPresenter thisInstance;
    private static ChildEventListener powerListListener;

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
            mMainActivityView = mainActivityView;
        }
        return thisInstance;
    }

    // FROM MAINACTIVITYCONTRACT

    @Override
    public void resumeActivity() {
        //attach the listener to get callbacks of the data being changed
        if(powerListListener == null)
            powerListListener = DataSource.attachPowerListListener(DataSource.MAINACTIVITYPRESENTER);
        else {
            Log.d(TAG, "resumeActivity: powerListListener is not null");
            DataSource.getPowerLists(DataSource.MAINACTIVITYPRESENTER);
        }

    }

    @Override
    public void pauseActivity() {
        //remove the listeners to prevent leaks
        Log.d(TAG, "in pauseActivity");
        DataSource.removePowerListListener(powerListListener);
    }

    public static void handleNewPowerList(String name, String id, ArrayList<String> groupNames){
        Log.d(TAG, "handleNewPowerList: " + name);
        mMainActivityView.addPowerListData(name, id, groupNames);
    }

    public static void handleNewDailyPowerList(String name, String id){
        //mMainActivityView.addDailyPowerListData(name, id);
    }

    public static void handleRemovedDailyPowerList(String dailyPowerListName, String id) {
        mMainActivityView.removeDailyPowerListData(dailyPowerListName, id);
    }

    public static void handleRemovedPowerList(String powerListName, String id) {
        mMainActivityView.removePowerListData(powerListName, id);
    }
}
