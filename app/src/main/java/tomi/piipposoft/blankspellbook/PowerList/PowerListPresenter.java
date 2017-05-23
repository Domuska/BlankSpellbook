package tomi.piipposoft.blankspellbook.PowerList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.PowerDetails.PowerDetailsActivity;
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
    private ChildEventListener spellListListener;
    private String powerListId;

    public PowerListPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull PowerListContract.View powerListActivity,
            @NonNull DrawerHelper drawerHelper,
            @NonNull String powerListId){
        // TODO: 8.5.2017 remove the sql database requirement when FireBase implementation complete
        super(dbHelper, drawerHelper);
        mPowerListActivity = powerListActivity;
        mDrawerActivityView = (DrawerContract.ViewActivity)mPowerListActivity;
        this.powerListId = powerListId;
    }

    public static void handleSpellFromDatabase(Spell spell){
        Log.d(TAG, "New spell! name: " + spell.getName());
        mPowerListActivity.addSpellToList(spell);
    }

    public static void handleSpellDeletion(Spell spell){
        mPowerListActivity.removeSpellFromList(spell);
    }

    // FROM POWERLISTCONTRACT

    @Override
    public void openPowerDetails(String itemId) {
        if(itemId.equals(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS))
            mPowerListActivity.showNewPowerUI();
        else
            mPowerListActivity.showPowerDetailsUI(itemId);
    }

    @Override
    public void getSpellList(Context context, String powerListId) {
        spellListListener = DataSource.addPowerListPowerListener(powerListId);
    }

    @Override
    public void activityPausing() {
        DataSource.removePowerListPowerListener(spellListListener, powerListId);
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
        showPowerLists();
        /*if(DrawerPresenter.powerListChildListener == null){
            DataSource.attachPowerListDrawerListener();
        }*/
    }

    @Override
    public void dailyPowerListProfileSelected() {
        this.showDailyPowerLists();
    }
}
