package tomi.piipposoft.blankspellbook.PowerList;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

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
    private static String powerListId;

    private String[] deletedPowerIds;

    private static PowerListPresenter thisInstance;

    //listener to the power list that is displayed
    private static ChildEventListener powerListListener;

    PowerListPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull PowerListContract.View powerListActivity,
            @NonNull DrawerHelper drawerHelper,
            @NonNull String listId){
        // TODO: 8.5.2017 remove the sql database requirement when FireBase implementation complete
        super(dbHelper, drawerHelper, (DrawerContract.ViewActivity) powerListActivity);
        mPowerListActivity = powerListActivity;
        powerListId = listId;
    }

    static PowerListPresenter getInstance(@NonNull BlankSpellBookContract.DBHelper dbHelper,
                                                 @NonNull PowerListContract.View powerListActivity,
                                                 @NonNull DrawerHelper drawerHelper,
                                                 @NonNull String listId){

        if(thisInstance == null)
            thisInstance = new PowerListPresenter(dbHelper, powerListActivity, drawerHelper, listId);
        else {
            //Instance already exists, just save references to activity and drawer views
            mPowerListActivity = powerListActivity;
            mDrawerView = drawerHelper;
            powerListId = listId;
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
    public void openPowerDetails(String itemId, String itemName, View transitioningView) {
        if(itemId.equals(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS)) {
            Log.d(TAG, "removing listener: " + powerListListener.toString());
            DataSource.removePowerListPowerListener(powerListListener, powerListId);
            mPowerListActivity.showNewPowerUI();
        }
        else {
            if(powerListListener != null) {
                Log.d(TAG, "removing listener: " + powerListListener.toString());
                DataSource.removePowerListPowerListener(powerListListener, powerListId);
            }
            mPowerListActivity.showPowerDetailsUI(itemId, itemName, powerListId, transitioningView);
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

        deletedPowerIds = new String[deletablePowers.size()];
        for (int i = 0; i < deletedPowerIds.length; i++) {
            Log.d(TAG, "Spell id: " + deletablePowers.get(i).getSpellId());
            deletedPowerIds[i] = deletablePowers.get(i).getSpellId();
        }
        DataSource.removePowersFromList(deletedPowerIds, powerListId);
        mPowerListActivity.showPowerDeletedSnackBar();
    }

    @Override
    public void userPushingUndo() {
        DataSource.addPowersToList(deletedPowerIds, powerListId);
    }
}
