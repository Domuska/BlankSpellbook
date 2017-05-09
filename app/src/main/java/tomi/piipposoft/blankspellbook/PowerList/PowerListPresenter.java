package tomi.piipposoft.blankspellbook.PowerList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Utils.Spell;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.Drawer.DrawerPresenter;

/**
 * Created by Domu on 17-Apr-16.
 *
 * This could be made smarter, check DrawerPresenter's requirements. Now dbhelper is passed as
 * parameter to DrawerPresenter for some reason - could maybe be removed and just context
 * passed in or somesuch.
 */
public class PowerListPresenter extends DrawerPresenter implements
    PowerListContract.UserActionListener,
        DrawerContract.UserActionListener{

    private static final String TAG = "PowerListPresenter";
    private static PowerListContract.View mPowerListActivity;
    private final DrawerContract.ViewActivity mDrawerActivityView;

    //the old constructor, using SQL database
    public PowerListPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull PowerListContract.View powerListActivity,
            @NonNull DrawerHelper drawerHelper){
        // TODO: 8.5.2017 remove the sql database requirement when FireBase implementation complete
        super(dbHelper, drawerHelper);
        mPowerListActivity = powerListActivity;
        mDrawerActivityView = (DrawerContract.ViewActivity)mPowerListActivity;
    }

    public static void handleSpellFromDatabase(Spell spell){
        Log.d(TAG, "New spell! name: " + spell.getName());
        mPowerListActivity.addSpellToAdapter(spell);
    }

    // FROM POWERLISTCONTRACT

    @Override
    public void openPowerDetails(String itemId, boolean addingNewPower) {
        if(!addingNewPower)
            mPowerListActivity.showPowerDetailsUI(itemId);
        else
            mPowerListActivity.showNewPowerUI();
    }

    @Override
    public void getSpellList(Context context, String powerListId) {
        DataSource.addSpellListListener(powerListId);
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
