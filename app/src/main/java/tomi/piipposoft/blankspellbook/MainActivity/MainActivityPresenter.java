package tomi.piipposoft.blankspellbook.MainActivity;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.Log;


import com.google.firebase.database.ChildEventListener;
import com.mikepenz.materialdrawer.Drawer;

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

    private ChildEventListener powerListListener;


    public MainActivityPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull MainActivityContract.View mainActivityView,
            @NonNull DrawerHelper drawerHelper){
        super(dbHelper, drawerHelper);
        mMainActivityView = mainActivityView;
        mDrawerActivityView = (DrawerContract.ViewActivity)mMainActivityView;
    }

    // TODO: 27.5.2017 could make this class have static getter. In it, if instance is null create new one
    // TODO: 27.5.2017 if the isntance not null, add the activity to the instance and return old one
    // TODO: 27.5.2017 this way we would not have to re-initialize DB listeners again and might have data cached already

    // FROM MAINACTIVITYCONTRACT

    @Override
    public void resumeActivity() {
        DataSource.attachDrawerPowerListListener(DataSource.MAINACTIVITYPRESENTER);
    }

    public static void handleNewPowerList(String name, String id){
        Log.d(TAG, "should be handling power list now....:" + name);
        mMainActivityView.addPowerListData(name, id);
    }

    public static void handleNewDailyPowerList(String name, String id){
        Log.d(TAG, "should be handling power list now....:" + name);
        mMainActivityView.addDailyPowerListData(name, id);
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
        /*if(DrawerPresenter.powerListChildListener == null) {
            DataSource.attachDrawerPowerListListener();
        }*/
    }

    @Override
    public void dailyPowerListProfileSelected() {
        this.showDailyPowerLists();
    }


}
