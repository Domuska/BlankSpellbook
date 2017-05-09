package tomi.piipposoft.blankspellbook.PowerDetails;

import android.support.annotation.NonNull;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.Drawer.DrawerPresenter;

/**
 * Created by Domu on 17-Apr-16.
 */
public class PowerDetailsPresenter extends DrawerPresenter
        implements PowerDetailsContract.UserActionListener,
        DrawerContract.UserActionListener{

    private final PowerDetailsContract.View mPowerDetailsView;
    private final DrawerContract.ViewActivity mDrawerActivityView;

    public PowerDetailsPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull PowerDetailsContract.View powerDetailsView,
            @NonNull DrawerHelper drawerHelper){
        super(dbHelper, drawerHelper);
        mPowerDetailsView = powerDetailsView;
        mDrawerActivityView = (DrawerContract.ViewActivity) mPowerDetailsView;
    }


    // FROM PowerDetailsContract

    @Override
    public void showPowerDetails(String powerId) {
        if(powerId.equals(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS)){
            mPowerDetailsView.showEmptyForms();
        }
        else
            mPowerDetailsView.showFilledForms();
    }

    // FROM DRAWERCONTRACT USERACTIONLISTENER
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
    }

    @Override
    public void powerListItemClicked(String itemId, String name) {
        mDrawerActivityView.openPowerList(itemId, name);
    }

    @Override
    public void dailyPowerListItemClicked(long itemId) {
        mDrawerActivityView.openDailyPowerList(itemId);
    }

    @Override
    public void powerListProfileSelected() {
        this.showPowerLists();
        if(DrawerPresenter.spellListChildListener == null){
            this.attachSpellListDrawerListener();
        }
    }

    @Override
    public void dailyPowerListProfileSelected() {
        this.showDailyPowerLists();
        if(DrawerPresenter.dailySpellListChildListener == null){
            this.attachDailySpellListDrawerListener();
        }
    }
}
