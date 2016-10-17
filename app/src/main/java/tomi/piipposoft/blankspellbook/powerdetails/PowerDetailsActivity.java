package tomi.piipposoft.blankspellbook.powerdetails;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetDailyPowerListNameDialog;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetPowerListNameDialog;
import tomi.piipposoft.blankspellbook.drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.powerlist.PowerListActivity;

public class PowerDetailsActivity extends AppCompatActivity
    implements DrawerHelper.DrawerListener,
        SetPowerListNameDialog.NoticeDialogListener,
        SetDailyPowerListNameDialog.NoticeDialogListener,
        DrawerContract.ViewActivity,
        PowerDetailsContract.View{

    public static final String EXTRA_POWER_DETAIL_ID = "powerDetailId";
    public static final long ADD_NEW_POWER_DETAILS = -1;

    private final String TAG = "PowerDetailsActivity";
    private DrawerHelper mDrawerHelper;
    private DrawerContract.UserActionListener mDrawerActionListener;
    private PowerDetailsContract.UserActionListener mActionListener;

    private long powerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_details);

        powerId = getIntent().getLongExtra(EXTRA_POWER_DETAIL_ID, PowerDetailsActivity.ADD_NEW_POWER_DETAILS);
        Log.i(TAG, "onCreate: ID extra gotten " + powerId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("Power details activity");
        setSupportActionBar(toolbar);

        //NOTE, THIS AND THE THINGS BELOW THIS MIGHT NEED TO BE MOVED TO onResume TO PREVENT CRASHES
        // WHEN RETURNING TO ACTIVITY AND TRYING TO ADD POWER LIST

    }

    @Override
    protected void onResume() {
        super.onResume();

        mDrawerHelper = DrawerHelper.getInstance(this, (Toolbar)findViewById(R.id.my_toolbar));
        mActionListener = new PowerDetailsPresenter(
                DataSource.getDatasource(this),
                this,
                DrawerHelper.getInstance(this, (Toolbar)findViewById(R.id.my_toolbar))
        );

        mDrawerActionListener = (DrawerContract.UserActionListener)mActionListener;
        mDrawerActionListener.powerListProfileSelected();
        mActionListener.showPowerDetails(powerId);

    }

    // FROM PowerDetailsContract

    @Override
    public void showEmptyForms() {

    }

    @Override
    public void showFilledForms() {

    }


    // FROM DRAWER CONTRACT VIEW INTERFACE

    @Override
    public void dailyPowerListProfileSelected() {
        mDrawerActionListener.dailyPowerListProfileSelected();
    }

    @Override
    public void powerListProfileSelected() {
        mDrawerActionListener.powerListProfileSelected();
    }

    @Override
    public void powerListClicked(IDrawerItem clickedItem) {
        PrimaryDrawerItem item = (PrimaryDrawerItem)clickedItem;
        mDrawerActionListener.powerListItemClicked(
                item.getIdentifier(),
                item.getName().toString());
    }

    @Override
    public void dailyPowerListClicked(IDrawerItem clickedItem) {
        mDrawerActionListener.dailyPowerListItemClicked(clickedItem.getIdentifier());
    }


    // FROM DRAWER CONTRACT VIEW ACTIVITY INTERFACE
    @Override
    public void openPowerList(Long powerListId, String name) {

        Intent i = new Intent(this, PowerListActivity.class);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_ID, powerListId);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_NAME, name);
        mDrawerHelper.closeDrawer();
        startActivity(i);
    }

    @Override
    public void openDailyPowerList(Long dailyPowerListId) {
        // do stuff
    }

    // FROM POPUP FRAGMENT INTERFACES

    @Override
    public void onSetPowerListNameDialogPositiveClick(DialogFragment dialog, String powerListName) {
        mDrawerActionListener.addPowerList(powerListName);
    }

    @Override
    public void onSetDailyPowerNameDialogPositiveClick(DialogFragment dialog, String dailyPowerListName) {
        mDrawerActionListener.addDailyPowerList(dailyPowerListName);
    }
}
