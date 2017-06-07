package tomi.piipposoft.blankspellbook.PowerList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;

import java.util.ArrayList;

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
    private String powerListId;


    private static PowerListPresenter thisInstance;

    //listener to the power list that is displayed
    private static ChildEventListener powerListListener;

    public PowerListPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull PowerListContract.View powerListActivity,
            @NonNull DrawerHelper drawerHelper,
            @NonNull String powerListId){
        // TODO: 8.5.2017 remove the sql database requirement when FireBase implementation complete
        super(dbHelper, drawerHelper, (DrawerContract.ViewActivity) powerListActivity);
        mPowerListActivity = powerListActivity;
        this.powerListId = powerListId;
    }

    public static PowerListPresenter getInstance(@NonNull BlankSpellBookContract.DBHelper dbHelper,
                                                 @NonNull PowerListContract.View powerListActivity,
                                                 @NonNull DrawerHelper drawerHelper,
                                                 @NonNull String powerListId){

        if(thisInstance == null)
            thisInstance = new PowerListPresenter(dbHelper, powerListActivity, drawerHelper, powerListId);
        else {
            //Instance already exists, just save references to activity and drawer views
            mPowerListActivity = powerListActivity;
            mDrawerView = drawerHelper;
        }
        return thisInstance;
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
        if(itemId.equals(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS)) {
            Log.d(TAG, "removing listener: " + powerListListener.toString());
            DataSource.removePowerListPowerListener(powerListListener, powerListId);
            mPowerListActivity.showNewPowerUI();
        }
        else {
            Log.d(TAG, "removing listener: " + powerListListener.toString());
            DataSource.removePowerListPowerListener(powerListListener, powerListId);
            mPowerListActivity.showPowerDetailsUI(itemId, powerListId);
        }
    }

    @Override
    public void getSpellList(String powerListId) {
        //remove the old listener
        if(powerListListener != null) {
            Log.d(TAG, "listener is not null, removing");
            DataSource.removePowerListPowerListener(powerListListener, powerListId);
        }
        Log.d(TAG, "power list ID in getSpellList is: " + powerListId);
        powerListListener = DataSource.addPowerListPowerListener(powerListId);
        Log.d(TAG, "added new listener: " + powerListListener.toString());
    }

    @Override
    public void resumeActivity() {

    }

    @Override
    public void pauseActivity() {
        Log.d(TAG, "activity pausing, removing listener");
        DataSource.removePowerListPowerListener(powerListListener, powerListId);
        powerListListener = null;
    }

    @Override
    public void userDeletingPowersFromList(ArrayList<Spell> deletablePowers) {
        Log.d(TAG, "spells that should be selected for deletion:");

        String[] deletablePowerIds = new String[deletablePowers.size()];
        for (int i = 0; i < deletablePowerIds.length; i++) {
            Log.d(TAG, "Spell id: " + deletablePowers.get(i).getSpellId());
            deletablePowerIds[i] = deletablePowers.get(i).getSpellId();
        }
        DataSource.removePowersFromList(deletablePowerIds, powerListId);
    }
}
