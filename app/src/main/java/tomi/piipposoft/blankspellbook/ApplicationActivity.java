package tomi.piipposoft.blankspellbook;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.PowerDetails.PowerDetailsActivity;
import tomi.piipposoft.blankspellbook.PowerList.PowerListActivity;
import tomi.piipposoft.blankspellbook.Drawer.SetDailyPowerListNameDialog;
import tomi.piipposoft.blankspellbook.Drawer.SetPowerListNameDialog;

/**
 * Created by OMISTAJA on 2.6.2017.
 */

public class ApplicationActivity extends AppCompatActivity
        implements DrawerContract.ViewActivity,
        DrawerHelper.DrawerListener,
        SetPowerListNameDialog.NoticeDialogListener,
        SetDailyPowerListNameDialog.NoticeDialogListener{

    protected DrawerHelper drawerHelper;
    protected DrawerContract.UserActionListener drawerActionListener;

    private final String TAG = "ApplicationActivity";

    //internal implementation for opening daily power list activity, this can be called from subclasses too
    protected void openDailyPowerListActivity(String id, String name){
        Log.d(TAG, "openDailyPowerList: We should be opening daily power lists activity now");
    }

    //internal implementation for opening power list activity, this can be called from subclasses too
    protected void openPowerListActivity(String id, String name){
        Intent i = new Intent(this, PowerListActivity.class);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_ID, id);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_NAME, name);
        drawerHelper.closeDrawer();
        startActivity(i);
    }

    protected void openPowerDetailsActivity(String powerId, String powerListId, String powerListName){
        Intent i = new Intent(this, PowerDetailsActivity.class);
        Log.d(TAG, "setting spell ID as extra: " + powerId);
        i.putExtra(PowerDetailsActivity.EXTRA_POWER_DETAIL_ID, powerId);
        i.putExtra(PowerDetailsActivity.EXTRA_POWER_LIST_ID,
                powerListId);
        i.putExtra(PowerDetailsActivity.EXTRA_POWER_LIST_NAME, powerListName);
        startActivity(i);
    }

    // FROM DRAWER CONTRACT VIEWACTIVITY INTERFACE

    @Override
    public void openPowerList(String powerListId, String powerListName) {
        //just call the internal implementation of this method
        this.openPowerListActivity(powerListId, powerListName);
    }

    @Override
    public void openDailyPowerList(String id, String name) {
        //just call the internal implementation of this method
        this.openDailyPowerListActivity(id, name);
    }


    // FROM DrawerHelper.DrawerListener contract

    /**
     * Called by DrawerHelper telling that daily power lists have been selected
     */
    @Override
    public void dailyPowerListProfileSelected() {
        drawerActionListener.dailyPowerListProfileSelected();
    }

    /**
     * Called by DrawerHelper telling that power lists have been selected
     */
    @Override
    public void powerListProfileSelected() {
        drawerActionListener.powerListProfileSelected();
    }


    /**
     * Called by DrawerHelper telling that a power list has been clicked
     * Handles forwarding the information to the presenter (most likely DrawerPresenter)
     * @param clickedItem the IDrawerItem that has been clicked
     */
    @Override
    public void powerListClicked(IDrawerItem clickedItem) {
        drawerActionListener.powerListItemClicked(
                (String)clickedItem.getTag(),
                ((PrimaryDrawerItem)clickedItem).getName().toString());
    }

    /**
     * Called by DrawerHelper telling that a daily power list has been clicked
     * Handles forwarding the information to the presenter (most likely DrawerPresenter)
     * @param clickedItem the item that has been clicked
     */
    @Override
    public void dailyPowerListClicked(IDrawerItem clickedItem) {
        drawerActionListener.dailyPowerListItemClicked(
                (String)clickedItem.getTag(),
                ((PrimaryDrawerItem)clickedItem).getName().toString());
    }

    // FROM DRAWER POPUP FRAGMENT INTERFACES

    @Override
    public void onSetDailyPowerNameDialogPositiveClick(DialogFragment dialog, String dailyPowerListName) {
        drawerActionListener.addDailyPowerList(dailyPowerListName);

    }

    // Called when positive button on SetSpellBookNameDialog is clicked
    @Override
    public void onSetPowerListNameDialogPositiveClick(DialogFragment dialog, String powerListName) {
        drawerActionListener.addPowerList(powerListName);
    }

}
