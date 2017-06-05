package tomi.piipposoft.blankspellbook;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.PowerList.PowerListActivity;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetDailyPowerListNameDialog;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetPowerListNameDialog;

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

    // FROM DRAWER CONTRACT VIEWACTIVITY INTERFACE

    @Override
    public void openPowerList(String powerListId, String powerListName) {
        Intent i = new Intent(this, PowerListActivity.class);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_ID, powerListId);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_NAME, powerListName);
        drawerHelper.closeDrawer();
        startActivity(i);
    }

    @Override
    public void openDailyPowerList(Long dailyPowerListId) {
        // TODO: 17-Apr-16 handle opening a new daily power list activity
    }


    // FROM DRAWER CONTRACT VIEW INTERFACE
    @Override
    public void dailyPowerListProfileSelected() {
        drawerActionListener.dailyPowerListProfileSelected();
    }

    @Override
    public void powerListProfileSelected() {
        drawerActionListener.powerListProfileSelected();
    }


    @Override
    public void powerListClicked(IDrawerItem clickedItem) {
        PrimaryDrawerItem item = (PrimaryDrawerItem)clickedItem;
        drawerActionListener.powerListItemClicked(
                (String)item.getTag(),
                item.getName().toString());
    }

    @Override
    public void dailyPowerListClicked(IDrawerItem clickedItem) {
        drawerActionListener.dailyPowerListItemClicked(clickedItem.getIdentifier());
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
