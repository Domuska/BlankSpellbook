package tomi.piipposoft.blankspellbook.MainActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.ApplicationActivity;
import tomi.piipposoft.blankspellbook.Drawer.SetDailyPowerListNameDialog;
import tomi.piipposoft.blankspellbook.Drawer.SetPowerListNameDialog;
import tomi.piipposoft.blankspellbook.PowerDetails.PowerDetailsActivity;
import tomi.piipposoft.blankspellbook.PowerList.PowerListActivity;
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
        implements MainActivityContract.View{

    private final String DATABASE_PERSISTANCE_SET_KEY = "databasePersistanceSet";
    private final String TAG = "MainActivity";
    private final String FRAGMENT_LAST_VISIBLE = "lastVisibleFragment";
    private final String FILTER_FRAGMENT_TAG = "filterFragment";

    private ActionBarDrawerToggle mDrawerToggle;
    private MainActivityContract.UserActionListener mActionlistener;

    private MainActivityPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private View secondaryToolbarTools;
    private TextView secondaryToolbarText, filterTextView;

    private boolean databasePersistanceSet = false;

    private FloatingActionButton fab;
    View.OnClickListener powersListener, dailyPowerListListener, powerListListener;

    //default selection is the spell lists fragment
    private int currentlySelectedList = MainActivityPresenter.POWER_LISTS_SELECTED;
    SpellFilterFragment filterFragment;

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

        secondaryToolbarText = findViewById(R.id.toolar_secondary_text);

        //set the support library's toolbar as application toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
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

        fab = findViewById(R.id.mainactivity_fab);
        //fab listeners
        powersListener = new OnNewPowerClickListener();
        dailyPowerListListener = new OnNewDailyPowerListClickListener();
        powerListListener = new OnNewPowerListClickListener();

        viewPager = findViewById(R.id.pager);

        // TODO: 12.7.2017 finish working pagerAdapter so it doesnt flash when activity resumes
        if(pagerAdapter == null) {
            Log.d(TAG, "pagerAdapter is null!");
            //create a new adapter and give it the actionListener to attach to the fragments
            pagerAdapter = new MainActivityPagerAdapter(
                    getSupportFragmentManager(),
                    (MainActivityContract.FragmentUserActionListener) mActionlistener,
                    (MainActivityContract.PagerAdapterListener) mActionlistener);
        }

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
                setFabFunctionality(position);
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
                        if (secondaryToolbarTools == null) {
                            initializeSecondaryToolbarTools();
                        }
                        else
                            secondaryToolbarTools.setVisibility(View.VISIBLE);
                        mActionlistener.userSwitchedTo(MainActivityPresenter.SPELLS_SELECTED);
                        break;
                    default:
                        Log.e(TAG, "onPageSelected: something went terribly wrong, position: " + position);
                        Toast.makeText(getApplicationContext(),
                                "Something went really wrong, please inform the developer, position" +
                                        "in onPageSelected: " + position + ". Restart the application.",
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
        //set the list that was previously selected
        viewPager.setCurrentItem(currentlySelectedList);
        //set fab onclicklistener and possible icon
        setFabFunctionality(currentlySelectedList);
        mActionlistener.userSwitchedTo(currentlySelectedList);
    }

    /**
     * Initialize the methods shown in spells section of the ViewPager
     */
    private void initializeSecondaryToolbarTools() {
        secondaryToolbarTools = ((ViewStub) findViewById(R.id.toolbar_viewStub)).inflate();
        filterTextView = findViewById(R.id.showFiltersView);
        filterTextView.setOnClickListener(new OpenFilterClickListener());

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
        if(keyCode == KeyEvent.KEYCODE_BACK ) {
            //close the filter fragment if it's visible
            if(filterFragment != null)
                removeFilterFragment();
            //ask if user wants to quit the app
            else {
                //pass builder our custom dialog fragment style
                new AlertDialog.Builder(MainActivity.this, R.style.dialogFragment_title_style)
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
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    //click listeners for the FAB
    private class OnNewDailyPowerListClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            DialogFragment dialog = new SetDailyPowerListNameDialog();
            dialog.show(MainActivity.this.getSupportFragmentManager(), "SetDailyPowerListNameDialog");
        }
    }

    private class OnNewPowerListClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            DialogFragment dialog = new SetPowerListNameDialog();
            dialog.show(MainActivity.this.getSupportFragmentManager(), "SetSpellBookNameDialogFragment");
        }
    }

    private class OnNewPowerClickListener implements View.OnClickListener{

        //https://android-developers.googleblog.com/2014/10/implementing-material-design-in-your.html
        // Activity + Fragment Transitions
        //https://github.com/codepath/android_guides/wiki/Shared-Element-Activity-Transition
        @Override
        public void onClick(View v) {
            //Intent i = new Intent(MainActivity.this, PowerDetailsActivity.class);
            /*i.putExtra(PowerDetailsActivity.EXTRA_POWER_DETAIL_ID,
                    PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS);*/
            //MainActivity.this.startActivity(i);
            Intent i = new Intent(MainActivity.this, PowerDetailsActivity.class);
            String transitionName = "fabTransition";
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            MainActivity.this,
                            fab,
                            transitionName);
            Bundle bundle = options.toBundle();

            i.putExtra(PowerDetailsActivity.EXTRA_POWER_DETAIL_ID,
                    PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS);
            ActivityCompat.startActivity(MainActivity.this, i, bundle);
        }
    }

    /**
     * Set onClickListener, icon and other attributes for FAB depending on which fragment is shown
     * @param selectedFragment fragment that is currently visible
     */
    private void setFabFunctionality(int selectedFragment){
        switch(selectedFragment){
            case 0:
                fab.setOnClickListener(dailyPowerListListener);
                break;
            case 1:
                fab.setOnClickListener(powerListListener);
                break;
            case 2:
                fab.setOnClickListener(powersListener);
                break;
            default:
                throw new RuntimeException("Unknown value in setFabFunctionality: use 0, 1 or 2");
        }
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
    public void startPowerListActivity(String name, String id,
                                       View transitionOrigin, int powerListColor) {
        if (transitionOrigin != null){
            Intent i = new Intent(MainActivity.this, PowerListActivity.class);
            String transitionName = getString(R.string.transition_powerlist_name);
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            MainActivity.this,
                            transitionOrigin,
                            transitionName);
            Bundle bundle = options.toBundle();

            i.putExtra(PowerListActivity.EXTRA_POWER_LIST_NAME, name);
            i.putExtra(PowerListActivity.EXTRA_POWER_LIST_ID, id);
            i.putExtra(PowerListActivity.EXTRA_POWER_LIST_COLOR, powerListColor);
            ActivityCompat.startActivity(MainActivity.this, i, bundle);
        }
        else
            this.openPowerListActivity(id, name);
    }

    @Override
    public void startDailyPowerListActivity(String listName, String listId) {
        this.openDailyPowerList(listName, listId);
    }

    @Override
    public void startPowerDetailsActivity(String powerId, String powerListId, View transitionOrigin) {
        //transition disabled for now since starting the powerDetails is choppy, maybe add transition
        //if the activity start-up is made smoother
        /*if(transitionOrigin != null){
            Intent i = new Intent(MainActivity.this, PowerDetailsActivity.class);
            String transitionName = getString(R.string.transition_powerDetails_name);
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            MainActivity.this,
                            transitionOrigin,
                            transitionName);
            Bundle bundle = options.toBundle();

            i.putExtra(PowerDetailsActivity.EXTRA_POWER_DETAIL_ID, powerId);
            i.putExtra(PowerDetailsActivity.EXTRA_POWER_LIST_ID, powerListId);
            ActivityCompat.startActivity(MainActivity.this, i, bundle);
        }
        else*/
            this.openPowerDetailsActivity(powerId, powerListId);
    }

    @Override
    public void showFilteredPowers(ArrayList<Spell> filteredPowers) {
        //tell powersFragment to remove the powers not in the list
        // TODO: 17.7.2017 finish
    }

    @Override
    public void showFilteredPowerGroups(ArrayList<String> displayedGroups) {
        filterFragment.setDisplayedGroupNames(displayedGroups);
    }

    @Override
    public void showFilteredPowerLists(ArrayList<String> displayedPowerLists) {
        filterFragment.setDisplayedPowerListNames(displayedPowerLists);
    }

    private void removeFilterFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.filter_fragment_slide_in, R.anim.filter_fragment_slide_out);
        transaction.remove(filterFragment);
        transaction.commit();
        filterFragment = null;
    }

    /**
     * Click listener for the filter button, open if it is closed, close it if it is open
     */
    private class OpenFilterClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            //check if we have the fragment in the manager already
            if (fragmentManager.findFragmentByTag(FILTER_FRAGMENT_TAG) == null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.filter_fragment_slide_in, R.anim.filter_fragment_slide_out);
                filterFragment = new SpellFilterFragment();
                //give fragment the actionListener
                filterFragment.attachActionListener(
                        (MainActivityContract.FilterFragmentUserActionListener)mActionlistener);
                //add the group names to be displayed
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(SpellFilterFragment.GROUP_NAMES_BUNDLE,
                        mActionlistener.getGroupNamesForFilter());
                bundle.putStringArrayList(SpellFilterFragment.POWER_LIST_NAMES_BUNDLE,
                        mActionlistener.getPowerListNamesForFilter());
                filterFragment.setArguments(bundle);
                //add the fragment and tag so we can find the fragment later
                transaction.add(R.id.fragmentFrameLayout, filterFragment, FILTER_FRAGMENT_TAG);
                transaction.commit();
            }
            else{
                removeFilterFragment();
            }
        }
    }
}
