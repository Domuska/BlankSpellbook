package tomi.piipposoft.blankspellbook.MainActivity;

import android.support.annotation.NonNull;
import android.util.Log;


import com.google.firebase.database.ChildEventListener;

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
    private final DrawerContract.ViewActivity mDrawerActivityView;

    private static final String TAG = "MainActivityPresenter";

    private static MainActivityPresenter thisInstance;
    private static ChildEventListener powerListListener;

    private MainActivityPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull MainActivityContract.View mainActivityView,
            @NonNull DrawerHelper drawerHelper){
        super(dbHelper, drawerHelper);
        mMainActivityView = mainActivityView;
        mDrawerActivityView = (DrawerContract.ViewActivity)mMainActivityView;
    }


    static MainActivityPresenter getInstance(@NonNull BlankSpellBookContract.DBHelper dbHelper,
                                             @NonNull DrawerHelper drawerHelper,
                                             @NonNull MainActivityContract.View mainActivityView){
        if(thisInstance == null) {
            Log.d(TAG, "no existing instance, creating new");
            thisInstance = new MainActivityPresenter(dbHelper, mainActivityView, drawerHelper);
        }
        return thisInstance;
    }

    // FROM MAINACTIVITYCONTRACT

    @Override
    public void resumeActivity() {
        //attach the listener so they can be added to the fragments, list or whatever
        powerListListener = DataSource.attachPowerListListener(DataSource.MAINACTIVITYPRESENTER);
        // TODO: 29.5.2017 if listener is already set, get the values so we get the data again if phone is rotated
    }

    @Override
    public void pauseActivity() {
        //remove the listeners to prevent leaks
        Log.d(TAG, "in pauseActivity");
        DataSource.removePowerListListener(powerListListener);
    }

    public static void handleNewPowerList(String name, String id){
        mMainActivityView.addPowerListData(name, id);
    }

    public static void handleNewDailyPowerList(String name, String id){
        mMainActivityView.addDailyPowerListData(name, id);
    }

    public static void handleRemovedDailyPowerList(String dailyPowerListName, String id) {
        mMainActivityView.removeDailyPowerListData(dailyPowerListName, id);
    }

    public static void handleRemovedPowerList(String powerListName, String id) {
        mMainActivityView.removePowerListData(powerListName, id);
    }


    //From Drawer contract
    @Override
    public void addPowerList(@NonNull String powerListName){
        this.addNewPowerList(powerListName);
    }

    @Override
    public void addDailyPowerList(@NonNull String dailyPowerListName) {
        this.addNewDailyPowerList(dailyPowerListName);
    }

    @Override
    public void drawerOpened() {
    }

    @Override
    public void powerListItemClicked(@NonNull String itemId, String name) {
        mDrawerActivityView.openPowerList(itemId, name);
    }

    @Override
    public void dailyPowerListItemClicked(@NonNull long itemId) {
        mDrawerActivityView.openDailyPowerList(itemId);
    }

    @Override
    public void powerListProfileSelected() {
        showPowerLists();
    }

    @Override
    public void dailyPowerListProfileSelected() {
        this.showDailyPowerLists();
    }

}
