package tomi.piipposoft.blankspellbook.MainActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.dailypowerlist.DailyPowerListActivity;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetDailyPowerListNameDialog;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetPowerListNameDialog;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.PowerList.PowerListActivity;

/**
 *
 *
 */

public class MainActivity extends AppCompatActivity
        implements MainActivityContract.View,
        DrawerHelper.DrawerListener,
        SetPowerListNameDialog.NoticeDialogListener,
        SetDailyPowerListNameDialog.NoticeDialogListener,
        DrawerContract.ViewActivity{

    private final String DATABASE_PERSISTANCE_SET_KEY = "databasePersistanceSet";
    private final String TAG = "MainActivity";

    private Button spellBookButton, dailySpellsButton;
    private FloatingActionButton fab;
    private Activity thisActivity;
    private ActionBarDrawerToggle mDrawerToggle;
    private MainActivityContract.UserActionListener mActionlistener;
    private DrawerContract.UserActionListener mDrawerActionListener;


    private DrawerHelper mDrawerHelper;

    private boolean databasePersistanceSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        thisActivity = this;

        if(savedInstanceState != null) {
            databasePersistanceSet = savedInstanceState.getBoolean(DATABASE_PERSISTANCE_SET_KEY);
        }

        spellBookButton = (Button) findViewById(R.id.button_Spellbook);
        dailySpellsButton = (Button) findViewById(R.id.button_dailySpells);

        spellBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(thisActivity, PowerListActivity.class);
                startActivity(i);
            }
        });

        dailySpellsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(thisActivity, DailyPowerListActivity.class);
                startActivity(i);
            }

        });

        //set the support library's toolbar as application toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        switch(id){
            case R.id.action_settings:
                return true;

            case R.id.action_about:
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(DATABASE_PERSISTANCE_SET_KEY, databasePersistanceSet);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume(){
        super.onResume();

        if(!databasePersistanceSet){
            DataSource.setDatabasePersistance();
            databasePersistanceSet = true;
        }

        mDrawerHelper = DrawerHelper.getInstance(this, (Toolbar) findViewById(R.id.my_toolbar));
        mActionlistener = new MainActivityPresenter(DataSource.getDatasource(this), this, mDrawerHelper);

        if(mDrawerActionListener == null) {
            mDrawerActionListener = (DrawerContract.UserActionListener) mActionlistener;
        }

        //add button to toolbar to open the drawer
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerHelper.getDrawerLayout(),
                R.string.open_drawer_info,
                R.string.close_drawer_info){

            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle("Blank spellbook");
            }

            public void onDrawerOpened(View view){
                super.onDrawerOpened(view);
                getSupportActionBar().setTitle("Power lists");
            }

        };

        // Make the drawer initialize itself
        mDrawerActionListener.powerListProfileSelected();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    //FROM MAIN ACTIVITY CONTRACT INTERFACE


    // FROM DRAWER CONTRACT VIEWACTIVITY INTERFACE

    @Override
    public void openPowerList(String powerListId, String powerListName) {
        Intent i = new Intent(this, PowerListActivity.class);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_ID, powerListId);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_NAME, powerListName);
//        DrawerHelper.getInstance(this, (Toolbar)findViewById(R.id.my_toolbar)).closeDrawer();
        mDrawerHelper.closeDrawer();
        startActivity(i);
    }

    @Override
    public void openDailyPowerList(Long dailyPowerListId) {
        // TODO: 17-Apr-16 handle opening a new daily power list activity
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

    // FROM POPUP FRAGMENT INTERFACES

    @Override
    public void onSetDailyPowerNameDialogPositiveClick(DialogFragment dialog, String dailyPowerListName) {
        mDrawerActionListener.addDailyPowerList(dailyPowerListName);

    }

    // Called when positive button on SetSpellBookNameDialog is clicked
    @Override
    public void onSetPowerListNameDialogPositiveClick(DialogFragment dialog, String powerListName) {
        mDrawerActionListener.addPowerList(powerListName);
    }
}
