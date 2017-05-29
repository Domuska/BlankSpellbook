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
    private final DrawerContract.ViewActivity mDrawerActivityView;
    //private ChildEventListener spellListListener;
    private String powerListId;

    //private static ArrayList<String> listenerList = new ArrayList<>();
    //the currently set listener, can only have one, dont make sense to have listeners
    //to activities that aren't on foreground
    private static ChildEventListener currentListener;

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
        if(itemId.equals(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS)) {
            Log.d(TAG, "removing listener: " + currentListener.toString());
            DataSource.removePowerListPowerListener(currentListener, powerListId);
            mPowerListActivity.showNewPowerUI();
        }
        else {
            Log.d(TAG, "removing listener: " + currentListener.toString());
            DataSource.removePowerListPowerListener(currentListener, powerListId);
            mPowerListActivity.showPowerDetailsUI(itemId);
        }
    }

    @Override
    public void getSpellList(Context context, String powerListId) {
        //remove the old listener
        if(currentListener != null) {
            Log.d(TAG, "listener is not null, removing");
            DataSource.removePowerListPowerListener(currentListener, powerListId);
        }
        currentListener = DataSource.addPowerListPowerListener(powerListId);
        Log.d(TAG, "added new listener: " + currentListener.toString());
    }

    @Override
    public void activityPausing() {
        DataSource.removePowerListPowerListener(currentListener, powerListId);
    }

    @Override
    public void userPressingDeleteButton(ArrayList<Spell> deletablePowers) {
        Log.d(TAG, "spells that should be selected:");

        String[] deletablePowerIds = new String[deletablePowers.size()];
        for (int i = 0; i < deletablePowerIds.length; i++) {
            Log.d(TAG, "Spell id: " + deletablePowers.get(i).getSpellId());
            deletablePowerIds[i] = deletablePowers.get(i).getSpellId();
        }
        DataSource.removePowersFromList(deletablePowerIds, powerListId);
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
            DataSource.attachPowerListListener();
        }*/
    }

    @Override
    public void dailyPowerListProfileSelected() {
        this.showDailyPowerLists();
    }
}
