package tomi.piipposoft.blankspellbook.powerlist;

import android.support.annotation.NonNull;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.drawer.DrawerPresenter;

/**
 * Created by Domu on 17-Apr-16.
 */
public class PowerListPresenter extends DrawerPresenter implements
    PowerListContract.UserActionListener,
        DrawerContract.UserActionListener{

    private final PowerListContract.View mPowerListActivity;
    private final DrawerContract.ViewActivity mDrawerActivityView;

    public PowerListPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull PowerListContract.View powerListActivity,
            @NonNull DrawerHelper drawerHelper){
        super(dbHelper, drawerHelper);
        mPowerListActivity = powerListActivity;
        mDrawerActivityView = (DrawerContract.ViewActivity)mPowerListActivity;
    }

    // FROM POWERLISTCONTRACT

    @Override
    public void openPowerDetails(long itemId) {
        mPowerListActivity.showPowerDetailUI(itemId);
    }

    // FROM DRAWER CONTRACT INTERFACE

    @Override
    public void addPowerList(@NonNull String powerListName) {
        this.addNewPowerList(powerListName);
    }

    @Override
    public void addDailyPowerList(@NonNull String dailyPowerListName) {
        this.addNewDailyPowerList(dailyPowerListName);
    }

    @Override
    public void drawerOpened() {
        // do stuff...
    }

    @Override
    public void listPowerListItemClicked(long itemId, String name) {
        mDrawerActivityView.openPowerList(itemId, name);

    }

    @Override
    public void listDailyPowerListItemClicked(long itemId) {
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
