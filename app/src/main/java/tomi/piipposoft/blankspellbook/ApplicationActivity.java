package tomi.piipposoft.blankspellbook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.PowerList.PowerListActivity;

/**
 * Created by OMISTAJA on 2.6.2017.
 */

public class ApplicationActivity extends AppCompatActivity
        implements DrawerContract.ViewActivity{

    protected DrawerHelper drawerHelper;

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

}
