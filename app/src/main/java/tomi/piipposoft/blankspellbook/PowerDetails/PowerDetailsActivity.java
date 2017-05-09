package tomi.piipposoft.blankspellbook.PowerDetails;

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
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.PowerList.PowerListActivity;

public class PowerDetailsActivity extends AppCompatActivity
    implements DrawerHelper.DrawerListener,
        SetPowerListNameDialog.NoticeDialogListener,
        SetDailyPowerListNameDialog.NoticeDialogListener,
        DrawerContract.ViewActivity,
        PowerDetailsContract.View{

    public static final String EXTRA_POWER_DETAIL_ID = "powerDetailId";
    public static final String EXTRA_ADD_NEW_POWER_DETAILS = "";

    private final String TAG = "PowerDetailsActivity";
    private DrawerHelper mDrawerHelper;
    private DrawerContract.UserActionListener mDrawerActionListener;
    private PowerDetailsContract.UserActionListener mActionListener;

    private String powerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_details);

        powerId = getIntent().getStringExtra(EXTRA_POWER_DETAIL_ID);
        Log.i(TAG, "onCreate: ID extra gotten " + powerId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("Power details activity");
        setSupportActionBar(toolbar);
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
                (String)item.getTag(),
                item.getName().toString());
    }

    @Override
    public void dailyPowerListClicked(IDrawerItem clickedItem) {
        mDrawerActionListener.dailyPowerListItemClicked(clickedItem.getIdentifier());
    }


    // FROM DRAWER CONTRACT VIEW ACTIVITY INTERFACE
    @Override
    public void openPowerList(String powerListId, String name) {

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
