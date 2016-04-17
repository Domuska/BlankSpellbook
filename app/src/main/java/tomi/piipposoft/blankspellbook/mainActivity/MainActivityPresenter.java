package tomi.piipposoft.blankspellbook.mainActivity;

import android.support.annotation.NonNull;


import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.drawer.DrawerPresenter;

/**
 * Created by Domu on 17-Apr-16.
 */
public class MainActivityPresenter extends DrawerPresenter
        implements DrawerContract.UserActionListener,
        MainActivityContract.UserActionListener {

    private final MainActivityContract.View mMainActivityView;
    private final DrawerContract.ViewActivity mDrawerActivityView;


    public MainActivityPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull MainActivityContract.View mainActivityView,
            @NonNull DrawerHelper drawerHelper){
        super(dbHelper, drawerHelper);
        mMainActivityView = mainActivityView;
        mDrawerActivityView = (DrawerContract.ViewActivity)mMainActivityView;
    }

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
    public void listPowerListItemClicked(@NonNull long itemId, String name) {

//        mMainActivityView.openPowerList(itemId, name);
        mDrawerActivityView.openPowerList(itemId, name);
    }

    @Override
    public void listDailyPowerListItemClicked(@NonNull long itemId) {
//        mMainActivityView.openDailyPowerList(itemId);
        mDrawerActivityView.openDailyPowerList(itemId);
    }

    @Override
    public void powerListProfileSelected() {
        this.showPowerLists();
    }

    @Override
    public void dailyPowerListProfileSelected() {
        this.showDailyPowerLists();
    }


}
