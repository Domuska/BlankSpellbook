package tomi.piipposoft.blankspellbook.MainActivity;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.ApplicationActivity;
import tomi.piipposoft.blankspellbook.PowerDetails.PowerDetailsActivity;
import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.Utils.SharedPreferencesHandler;
import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 *
 *
 */

public class MainActivity extends ApplicationActivity
        implements MainActivityContract.View {

    private final String DATABASE_PERSISTANCE_SET_KEY = "databasePersistanceSet";
    private final String TAG = "MainActivity";
    private final String FRAGMENT_LAST_VISIBLE = "lastVisibleFragment";


    private Button spellBookButton, dailySpellsButton;
    private ActionBarDrawerToggle mDrawerToggle;
    private MainActivityContract.UserActionListener mActionlistener;

    PowersFragment powersFragment;
    RecyclerListFragment powerListFragment;
    private FragmentManager fragmentManager;
    private MainActivityPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private View secondaryToolbarTools;
    private TextView secondaryToolbarText;

    private boolean databasePersistanceSet = false;

    //default selection is the spell lists fragment
    private int currentlySelectedList = MainActivityPresenter.POWER_LISTS_SELECTED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!SharedPreferencesHandler.isDatabasePersistanceSet(this)){
            DataSource.setDatabasePersistance();
            SharedPreferencesHandler.setDatabasePersistance(true, this);
        }

        if(savedInstanceState != null) {
            databasePersistanceSet = savedInstanceState.getBoolean(DATABASE_PERSISTANCE_SET_KEY);

            //if we get a value then activity was destroyed and is resuming
            currentlySelectedList = savedInstanceState
                    .getInt(FRAGMENT_LAST_VISIBLE);
        }

        secondaryToolbarText = (TextView) findViewById(R.id.toolar_secondary_text);

        //set the support library's toolbar as application toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }


    @Override
    public void onResume(){
        super.onResume();

        //nav drawer
        this.drawerHelper = DrawerHelper.getInstance(this, (Toolbar) findViewById(R.id.my_toolbar));
        mActionlistener = MainActivityPresenter.getInstance(
                DataSource.getDatasource(this),
                drawerHelper,
                this);

        //create a new adapter and give it the actionListener to attach to the fragments
        pagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager(),
                (MainActivityContract.FragmentUserActionListener) mActionlistener,
                (MainActivityContract.PagerAdapterListener) mActionlistener);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        //set the number of screens that are away from currently focused screen
        //if this is smaller, the fragments are re-created and data needs to be re-fetched
        viewPager.setOffscreenPageLimit(2);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentlySelectedList = position;
                switch (position) {
                    case MainActivityPresenter.DAILY_POWER_LISTS_SELECTED:
                        secondaryToolbarText.setText(getString(R.string.toolbar_text_daily_power_lists));
                        if (secondaryToolbarTools != null)
                            secondaryToolbarTools.setVisibility(View.GONE);
                        //tell presenter which page was switched to
                        mActionlistener.userSwitchedTo(MainActivityPresenter.DAILY_POWER_LISTS_SELECTED);
                        break;

                    case MainActivityPresenter.POWER_LISTS_SELECTED:
                        secondaryToolbarText.setText(getString(R.string.toolbar_text_power_lists));
                        if (secondaryToolbarTools != null)
                            secondaryToolbarTools.setVisibility(View.GONE);
                        mActionlistener.userSwitchedTo(MainActivityPresenter.POWER_LISTS_SELECTED);
                        break;

                    case MainActivityPresenter.SPELLS_SELECTED:
                        secondaryToolbarText.setText(getText(R.string.toolbar_text_spells));
                        if (secondaryToolbarTools == null)
                            secondaryToolbarTools = ((ViewStub) findViewById(R.id.toolbar_viewStub)).inflate();
                        else
                            secondaryToolbarTools.setVisibility(View.VISIBLE);
                        mActionlistener.userSwitchedTo(MainActivityPresenter.SPELLS_SELECTED);
                        break;
                    default:
                        Log.e(TAG, "onPageSelected: something went terribly wrong, position: " + position);
                        Toast.makeText(getApplicationContext(),
                                "Something went really wrong, please inform the developer, position: " + position,
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if(this.drawerActionListener == null) {
            this.drawerActionListener = (DrawerContract.UserActionListener) mActionlistener;
        }

        //add button to toolbar to open the drawer
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerHelper.getDrawerLayout(),
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
        this.drawerActionListener.powerListProfileSelected();
        mActionlistener.resumeActivity();
        viewPager.setCurrentItem(currentlySelectedList);
        mActionlistener.userSwitchedTo(currentlySelectedList);
    }

    @Override
    protected void onPause() {
        mActionlistener.pauseActivity();
        //remove the data from the fragments since it's re-fetched on resume
        pagerAdapter.removePowerListsFromFragment();
        pagerAdapter.removeDailyPowerListsFromFragment();
        pagerAdapter.removeAllPowers();
        super.onPause();
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
        outState.putInt(FRAGMENT_LAST_VISIBLE, viewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //back button functionality
        if(keyCode == KeyEvent.KEYCODE_BACK ) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.mainactivity_back_button_popup_title)
                    .setMessage(R.string.mainactivity_back_button_popup_info)
                    .setPositiveButton(getString(R.string.action_yes)
                            , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.this.finish();
                                }
                            })
                    .setNegativeButton(getString(R.string.action_no), null)
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    //FROM MAIN ACTIVITY CONTRACT INTERFACE


    @Override
    public void addPowerListData(String name, String id) {
        pagerAdapter.addPowerListToFragment(name, id);
    }

    @Override
    public void addPowerNameToPowerList(String name, String powerListId) {
        pagerAdapter.addPowerNameToPowerList(name, powerListId);
    }

    @Override
    public void addDailyPowerListData(String name, String id) {
        pagerAdapter.addDailyPowerListToFragment(name, id);
    }

    @Override
    public void addPowerNameToDailyPowerList(String powerName, String dailyPowerListId) {
        pagerAdapter.addPowerNameToDailyPowerList(powerName, dailyPowerListId);
    }

    @Override
    public void removePowerListData(String powerListName, String id) {
        pagerAdapter.removePowerListItem(powerListName, id);
    }

    @Override
    public void removeDailyPowerListData(String dailyPowerListName, String id) {
        pagerAdapter.removeDailyPowerListItem(dailyPowerListName, id);
    }

    @Override
    public void addNewPowerToList(Spell power, String powerListName) {
        pagerAdapter.addPowerToFragment(power, powerListName);
    }

    @Override
    public void removePowerFromList(Spell power) {
        pagerAdapter.removePowerFromFragment(power);
    }

    @Override
    public void startPowerListActivity(String name, String id) {
        this.openPowerListActivity(id, name);
    }

    @Override
    public void startDailyPowerListActivity(String listName, String listId) {
        this.openDailyPowerList(listName, listId);
    }

    @Override
    public void startPowerDetailsActivity(String powerId, String powerListId) {
        this.openPowerDetailsActivity(powerId, powerListId);
    }
}
