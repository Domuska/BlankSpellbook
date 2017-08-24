package tomi.piipposoft.blankspellbook.MainActivity;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.bowyer.app.fabtoolbar.BuildConfig;
import com.bowyer.app.fabtoolbar.FabToolbar;

import java.util.ArrayList;
import java.util.TreeSet;

import tomi.piipposoft.blankspellbook.ApplicationActivity;
import tomi.piipposoft.blankspellbook.Drawer.SetDailyPowerListNameDialog;
import tomi.piipposoft.blankspellbook.Drawer.SetPowerListNameDialog;
import tomi.piipposoft.blankspellbook.PowerDetails.PowerDetailsActivity;
import tomi.piipposoft.blankspellbook.PowerList.PowerListActivity;
import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.SettingsActivity;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.Utils.Helper;
import tomi.piipposoft.blankspellbook.Utils.SharedPreferencesHandler;
import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 *  Activity that hosts three fragments - daily power lists, power lists and spells fragments.
 *  These are contained in a ViewPager.
 *  Activity has a presenter that takes care of communication with the back-end, holding the data
 *  and filtering the data when user uses the filters in powers fragment.
 *
 *  PagerAdapter takes care of telling presenter when data should be fetched - it knows when
 *  it's ready to receive it.
 *
 */

public class MainActivity extends ApplicationActivity
        implements MainActivityContract.View,
        SharedPreferences.OnSharedPreferenceChangeListener,
        PowersFragment.ListScrolledInterface,
        SpellFilterFragment.FragmentExpandedInterface{

    private final String DATABASE_PERSISTANCE_SET_KEY = "databasePersistanceSet";
    private final String TAG = "MainActivity";
    private final String FRAGMENT_LAST_VISIBLE = "lastVisibleFragment";
    private final String FILTER_FRAGMENT_TAG = "filterFragment";
    private final String WAS_FAB_EXPANDED = "wasFabExpanded";
    private final float NOT_INITIALIZED = -9998;

    private ActionBarDrawerToggle mDrawerToggle;
    private MainActivityContract.UserActionListener mActionlistener;
    private MainActivityContract.ListeningStateInterface listeningStateInterface;

    private MainActivityPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private View secondaryToolbarTools;
    private TextView secondaryToolbarText;
    private FabToolbar bottomToolbar;

    private boolean databasePersistanceSet = false;
    private ViewPager.OnPageChangeListener onPageChangeListener;
    private boolean canFabAnimationSkipToEnd = false;

    //we have actually two fabs, but only one is visible at a time. This is so the bottom toolbar animation
    //is much more simple to accomplish
    private FloatingActionButton mainToolbarFab, bottomFab;
    View.OnClickListener powersListener, dailyPowerListListener, powerListListener;

    //default selection is the spell lists fragment
    private int currentlySelectedList = MainActivityPresenter.POWER_LISTS_SELECTED;
    SpellFilterFragment filterFragment;
    FragmentManager fragmentManager;

    private ImageButton searchButton, filterButton, addSpellButton;
    private SearchView searchView;
    private int bottomToolbarDefaultColor, bottomToolbarSearchColor;

    //flag when activity is destroyed or paused, so we can restore expansion
    private boolean wasFabExpanded;

    SpringAnimation springXAnimation, springYAnimation;
    DynamicAnimation.OnAnimationEndListener fabMoveAnimationEndListener;

    //coordinates where the fab should be animated to when moving between spells fragment and
    //power lists fragments. 0,0 position lets the layoutParams decide location, other coordinates
    //are relative to this resting position
    float fabToolbarXPosition = 0;
    float fabToolbarYPosition = 0;
    float fabBottomXPosition = NOT_INITIALIZED;
    float fabBottomYPosition = NOT_INITIALIZED;

    ObjectAnimator bottomToolbarAnimator;

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

            //if we have savedInstanceState, the filter fragment might might be in the fragmentManager
            filterFragment = (SpellFilterFragment)
                    getSupportFragmentManager().findFragmentByTag(FILTER_FRAGMENT_TAG);

            wasFabExpanded = savedInstanceState.getBoolean(WAS_FAB_EXPANDED);

            //don't animate the fab if the activity is re-created. That is odd.
            canFabAnimationSkipToEnd = true;
        }

        bottomToolbarDefaultColor = ContextCompat.getColor(this, R.color.my_color_app_accent);
        bottomToolbarSearchColor = ContextCompat.getColor(this, R.color.my_color_app_accent_lighter);

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
        listeningStateInterface = (MainActivityContract.ListeningStateInterface) mActionlistener;

        mainToolbarFab = findViewById(R.id.mainactivity_fab);
        //mainToolbarFab listeners
        powersListener = new OnNewPowerClickListener();
        dailyPowerListListener = new OnNewDailyPowerListClickListener();
        powerListListener = new OnNewPowerListClickListener();

        initializeViewPager();
        fragmentManager = getSupportFragmentManager();

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

        initializeBottomToolbar();
        initializeFabAnimations();

        // Make the drawer initialize itself
        this.drawerActionListener.powerListProfileSelected();
        mActionlistener.resumeActivity();

        //set the list that was previously selected
        viewPager.setCurrentItem(currentlySelectedList);
        //make sure the onPageChangeListener is called after fragments are ready so UI for the fragment is initialized
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                onPageChangeListener.onPageSelected(viewPager.getCurrentItem());
            }
        });

        //set mainToolbarFab onclicklistener and possible icon
        setFabFunctionality(currentlySelectedList);
        mActionlistener.userSwitchedTo(currentlySelectedList);
        setPresenterFilterMode();
    }

    /**
     * Initialize the two animations used for moving fab to and from bottom right corner
     */
    private void initializeFabAnimations(){

        springXAnimation = new SpringAnimation(mainToolbarFab, DynamicAnimation.TRANSLATION_X);
        springYAnimation = new SpringAnimation(mainToolbarFab, DynamicAnimation.TRANSLATION_Y);

        SpringForce xForce = new SpringForce();
        xForce.setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY).setStiffness(SpringForce.STIFFNESS_LOW);
        springXAnimation.setSpring(xForce);

        SpringForce yForce = new SpringForce();
        yForce.setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY).setStiffness(SpringForce.STIFFNESS_LOW);
        springYAnimation.setSpring(yForce);

        int dpPerSecond = 2;
        float pixelPerSecond = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpPerSecond,
                getResources().getDisplayMetrics());

        springXAnimation.setStartVelocity(pixelPerSecond);
        springYAnimation.setStartVelocity(pixelPerSecond);

        //listener for animation end - should be run after the fab is animated to bottom of screen
        fabMoveAnimationEndListener = new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                mainToolbarFab.setVisibility(View.INVISIBLE);
                bottomFab.setVisibility(View.VISIBLE);
            }
        };

        //mainToolbarFab.setVisibility(View.INVISIBLE);
        /*CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) mainToolbarFab.getLayoutParams();
        layoutParams.setAnchorId(R.id.fragmentFrameLayout);
        layoutParams.anchorGravity = Gravity.BOTTOM | Gravity.END;
        mainToolbarFab.setLayoutParams(layoutParams);*/

        /*Display mdisp = getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        int width = mdispSize.x;
        int height = mdispSize.y;
        float fabMargin = getResources().getDimension(R.dimen.fab_margin);
        float newWidth = width - mainToolbarFab.getWidth() - mainToolbarFab.getX()
                - fabMargin;
        float newHeight = height - mainToolbarFab.getHeight() - mainToolbarFab.getY()
                - fabMargin;*/

        //Log.d(TAG, "fab final x: " + mainToolbarFab.getX() + " fab final y: " + mainToolbarFab.getY());
    }

    /**
     * Initialize the ViewPager. Initialize the pager
     * if it is null and set up the listeners for page changes
     */
    private void initializeViewPager(){
        if(viewPager == null) {
            viewPager = findViewById(R.id.pager);
            Log.d(TAG, "viewPager is null, creating new: " + viewPager);
            //create a new adapter and give it the actionListener to attach to the fragments
            pagerAdapter = new MainActivityPagerAdapter(
                    getSupportFragmentManager(),
                    (MainActivityContract.FragmentUserActionListener) mActionlistener,
                    (MainActivityContract.ListeningStateInterface) mActionlistener);
            viewPager.setAdapter(pagerAdapter);
        }
        else {
            //listeningStateInterface.startListeningForPowerLists();
        }


        //set the number of screens that are away from currently focused screen
        //if this is smaller, the fragments are re-created and data needs to be re-fetched
        viewPager.setOffscreenPageLimit(2);

        onPageChangeListener = new ViewPager.OnPageChangeListener() {
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
                        hideFilterFragment(fragmentManager);
                        mainToolbarFab.setVisibility(View.VISIBLE);
                        bottomFab.setVisibility(View.GONE);
                        bottomToolbar.setVisibility(View.GONE);
                        break;

                    case MainActivityPresenter.POWER_LISTS_SELECTED:
                        secondaryToolbarText.setText(getString(R.string.toolbar_text_power_lists));
                        if (secondaryToolbarTools != null)
                            secondaryToolbarTools.setVisibility(View.GONE);
                        mActionlistener.userSwitchedTo(MainActivityPresenter.POWER_LISTS_SELECTED);
                        mainToolbarFab.setVisibility(View.VISIBLE);
                        hideFilterFragment(fragmentManager);

                        //set the drawable back to plus icon if it has been changed
                        mainToolbarFab.setImageResource(R.drawable.ic_add_black_36dp);

                        //animate fab to correct position. Run needlessly at app start-up once, but
                        //that shouldn't consume too much resources :)
                        animateFABToToolbar();

                        //hide the toolbar and secondary fab if they are visible
                        bottomFab.setVisibility(View.INVISIBLE);
                        bottomToolbar.setVisibility(View.GONE);
                        break;


                    case MainActivityPresenter.SPELLS_SELECTED:
                        secondaryToolbarText.setText(getText(R.string.toolbar_text_spells));
                        if (secondaryToolbarTools == null) {
                            initializeSecondaryToolbarTools();
                        }
                        else
                            secondaryToolbarTools.setVisibility(View.VISIBLE);

                        //set the bottom bar and visible (fab does not need to be set, test and remove)
                        bottomToolbar.setVisibility(View.VISIBLE);
                        bottomFab.setVisibility(View.INVISIBLE);

                        //if filter fragment created, it should be re-opened
                        if(fragmentManager.findFragmentByTag(FILTER_FRAGMENT_TAG) != null){
                            //add filter fragment
                            addFilterFragment(fragmentManager);
                            bottomFab.setVisibility(View.INVISIBLE);
                            //put the expanded bottom toolbar to top of filter
                            bottomToolbar.expandFab(false);
                        }
                        //else see if fab was expanded earlier
                        if(!bottomToolbar.isFabExpanded()) {
                            //calculate where the fab should be animated to
                            if (fabBottomXPosition == NOT_INITIALIZED && fabBottomYPosition == NOT_INITIALIZED)
                                calculateFabBottomPosition();

                            //set drawable for the fab, looks better than setting it after animation
                            mainToolbarFab.setImageResource(R.drawable.ic_expand_more_black_36dp);
                            animateFABToBottom(canFabAnimationSkipToEnd);
                            //animation skipped once, set to false. Set to true at onCreate if needed.
                            canFabAnimationSkipToEnd = false;

                        }
                        else{
                            //if bottom fab is expanded, hide the toolbar fab anyway since animation end listener
                            //is not run
                            mainToolbarFab.setVisibility(View.INVISIBLE);
                        }
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
        };
        viewPager.addOnPageChangeListener(onPageChangeListener);

    }


    /**
     * Initialize the secondary fab and toolbar
     */
    private void initializeBottomToolbar(){

        bottomFab = findViewById(R.id.mainactivity_fab2);
        bottomToolbar = findViewById(R.id.fabtoolbar);
        bottomToolbar.setFab(bottomFab);

        filterButton = findViewById(R.id.bottombar_filterButton);
        searchButton = findViewById(R.id.bottombar_searchbutton);
        addSpellButton = findViewById(R.id.bottombar_addSpellButton);
        searchView = findViewById(R.id.search_view);

        //have to use a helper method to set the text color - using styles or such does not seem to work
        Helper.changeSearchViewTextColor(searchView, this);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                showBottomBarButtons();
                return false;
            }
        });

        filterButton.setOnClickListener(new OpenFilterClickListener());
        addSpellButton.setOnClickListener(new OnNewPowerClickListener());
        searchButton.setOnClickListener(new SearchViewClickListener());

        bottomFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomToolbar.expandFab();
            }
        });

        //if we are resuming, fab might have to be expanded
        if(wasFabExpanded)
            bottomToolbar.expandFab(false);
    }

    /**
     * Initialize the methods shown in spells section of the ViewPager
     */
    private void initializeSecondaryToolbarTools() {
        secondaryToolbarTools = ((ViewStub) findViewById(R.id.toolbar_viewStub)).inflate();
        //filterTextView = findViewById(R.id.showFiltersView);
        //filterTextView.setOnClickListener(new OpenFilterClickListener());
    }

    /**
     * Calculate where the fab should be animated to when moving it to bottom right corner
     */
    private void calculateFabBottomPosition(){
        fabBottomXPosition = bottomFab.getX() - mainToolbarFab.getX();
        fabBottomYPosition = bottomFab.getY() - mainToolbarFab.getY();
        Log.d(TAG, "fab bottom x " + fabBottomXPosition + " fab bottom y " + fabBottomYPosition);
    }

    /**
     * Move the fab to bottom right corner, the fabBottomXPosition and fabBottomYPosition
     * should be calculated beforehand
     */
    private void animateFABToBottom(boolean skipAnimation){
        Log.d(TAG, "animating fab to " + fabBottomXPosition + "," + fabBottomYPosition);
        if(fabBottomXPosition != NOT_INITIALIZED && fabBottomYPosition != NOT_INITIALIZED) {

            //add the end listener so the toolbar fab is set invisible and secondary fab set visible after
            springXAnimation.addEndListener(fabMoveAnimationEndListener);

            if(skipAnimation){
                //hide the fab so it does not flash visible at toolbar position
                //better could be if we just could skip the whole animation, not sure if that's too much work,
                //if we set the fab position with layoutparams the code becomes much more complicated
                //when we need to return the fab to the toolbar
                bottomFab.setVisibility(View.INVISIBLE);
                if(springXAnimation.canSkipToEnd()) {
                    springXAnimation.animateToFinalPosition(fabBottomXPosition);
                    springXAnimation.skipToEnd();
                }
                if(springYAnimation.canSkipToEnd()) {
                    springYAnimation.animateToFinalPosition(fabBottomYPosition);
                    springYAnimation.skipToEnd();
                }
                bottomFab.setVisibility(View.VISIBLE);
            }
            else {
                springXAnimation.animateToFinalPosition(fabBottomXPosition);
                springYAnimation.animateToFinalPosition(fabBottomYPosition);
            }
        }
        else
            throw new RuntimeException("fabBottomXPosition and fabBottomYPosition need to be set before calling");
    }

    /**
     * Move the fab to the toolbar, fabToolbarXPosition and fabToolbarYPosition should
     * be calculated beforehand
     */
    private void animateFABToToolbar(){
        //sanity check, these should never be anything else but 0
        if(BuildConfig.DEBUG && (fabToolbarXPosition != 0 || fabToolbarYPosition != 0)) {
            throw new RuntimeException("fabToolbarXPosition should be 0 and fabToolbarYPosition should be 0, " +
                    "were instead " + fabToolbarXPosition + ", " + fabToolbarYPosition);
        }

        mainToolbarFab.setVisibility(View.VISIBLE);
        bottomFab.setVisibility(View.INVISIBLE);
        //remove the end listener so the correct fabs will stay visible/invisible
        springXAnimation.removeEndListener(fabMoveAnimationEndListener);
        springXAnimation.animateToFinalPosition(fabToolbarXPosition);
        springYAnimation.animateToFinalPosition(fabToolbarYPosition);

    }

    /**
     * Get the filtering mode (join or cross-section) from shared preferences and pass it to presenter
     */
    private void setPresenterFilterMode() {
        ((MainActivityContract.preferencesInterface) mActionlistener).filterStyleChanged(
                SharedPreferencesHandler.getFilterSPellByCrossSection(this)
        );
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        mActionlistener.pauseActivity();
        wasFabExpanded = bottomToolbar.isFabExpanded();

        /*FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragmentManager.findFragmentByTag(FILTER_FRAGMENT_TAG));
        transaction.commit();
        filterFragment = null;*/


        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        fabBottomXPosition = NOT_INITIALIZED;
        fabBottomYPosition = NOT_INITIALIZED;
        super.onDestroy();
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
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;

            case R.id.action_about:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //let presenter save its' state
        mActionlistener.saveInstanceState(outState);
        outState.putBoolean(DATABASE_PERSISTANCE_SET_KEY, databasePersistanceSet);
        outState.putInt(FRAGMENT_LAST_VISIBLE, viewPager.getCurrentItem());
        outState.putBoolean(WAS_FAB_EXPANDED, bottomToolbar.isFabExpanded());
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
                removeFilterFragment(fragmentManager);
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

    /**
     * Set onClickListener, icon and other attributes for FAB depending on which fragment is shown
     * @param selectedFragment fragment that is currently visible
     */
    private void setFabFunctionality(int selectedFragment){
        // TODO: 15.8.2017 use enums or something to actually tell which fragment is which
        switch(selectedFragment){
            case 0:
                mainToolbarFab.setOnClickListener(dailyPowerListListener);
                break;
            case 1:
                mainToolbarFab.setOnClickListener(powerListListener);
                break;
            case 2:
                //mainToolbarFab.setOnClickListener(powersListener);
                mainToolbarFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomToolbar.expandFab();
                    }
                });
                break;
            default:
                throw new RuntimeException("Unknown value in setFabFunctionality: use 0, 1 or 2");
        }
    }

    //FROM MAIN ACTIVITY CONTRACT INTERFACE

    @Override
    public void addPowerListData(String name, String id) {
        Log.d(TAG, "addPowerListData: got new power list, name: " + name);
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
    public void setPowerListData(ArrayList<Spell> powers) {
        pagerAdapter.setPowersData(powers);
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
    public void startPowerDetailsActivity(String powerId, String powerListId,
                                          View transitionOrigin, String powerListName) {
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
            this.openPowerDetailsActivity(powerId, powerListId, powerListName);
    }

    @Override
    public void showFilteredGroups(TreeSet<String> displayedGroups) {
        filterFragment.setDisplayedGroupNames(displayedGroups);
    }

    @Override
    public void showFilteredPowerLists(TreeSet<String> displayedPowerLists) {
        filterFragment.setDisplayedPowerListNames(displayedPowerLists);
    }

    /**
     * Add the filter fragment to R.id.fragmentFrameLayout layout. If the fragment is already in
     * the activity, will show it instead.
     * @param fragmentManager The fragment manager for the activity
     */
    private void addFilterFragment(FragmentManager fragmentManager) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(fragmentManager.findFragmentByTag(FILTER_FRAGMENT_TAG) == null) {
            transaction.setCustomAnimations(R.anim.filter_fragment_slide_in, R.anim.filter_fragment_slide_out);
            filterFragment = new SpellFilterFragment();
            //give fragment the actionListener
            filterFragment.attachActionListener(
                    (MainActivityContract.FilterFragmentUserActionListener) mActionlistener);
            //add the group names to be displayed
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(SpellFilterFragment.GROUP_NAMES_BUNDLE,
                    new ArrayList<>(mActionlistener.getGroupNamesForFilter()));
            bundle.putStringArrayList(SpellFilterFragment.GROUP_NAMES_SELECTED_BUNDLE,
                    mActionlistener.getSelectedGroupsForFilter());
            bundle.putStringArrayList(SpellFilterFragment.POWER_LIST_NAMES_BUNDLE,
                    new ArrayList<>(mActionlistener.getPowerListNamesForFilter()));
            bundle.putStringArrayList(SpellFilterFragment.POWER_LIST_NAMES_SELECTED_BUNDLE,
                    mActionlistener.getSelectedPowerListsForFilter());
            filterFragment.setArguments(bundle);
            //add the fragment and tag so we can find the fragment later
            transaction.add(R.id.fragmentFrameLayout, filterFragment, FILTER_FRAGMENT_TAG);

        }
        else{
            if(filterFragment != null)
                transaction.show(filterFragment);
            else {
                Toast.makeText(this, "Something went wrong, can't animate the filter fragment.",
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "addFilterFragment: filterFragment is empty, not animating");
            }
        }

        transaction.commit();
    }

    /**
     * Removes the filter fragment from the transaction altogether.
     * Silently takes care of situation where the fragment might exist.
     * For hiding the fragment, see {@link #hideFilterFragment(FragmentManager)}
     * @param fragmentManager fragmentManager for the current activity
     * @return boolean true if filterFragment was visible, false otherwise
     */
    private boolean removeFilterFragment(FragmentManager fragmentManager){
        if(fragmentManager.findFragmentByTag(FILTER_FRAGMENT_TAG) != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.filter_fragment_slide_in, R.anim.filter_fragment_slide_out);
            transaction.remove(fragmentManager.findFragmentByTag(FILTER_FRAGMENT_TAG));
            transaction.commit();
            filterFragment = null;
            return true;
        }
        else
            return false;
    }

    /**
     * Hide the FilterFragment if it is visible, for removing see {@link #removeFilterFragment(FragmentManager)}
     * @param fragmentManager FragmentManager for this activity
     */
    private void hideFilterFragment(FragmentManager fragmentManager){
        if(fragmentManager.findFragmentByTag(FILTER_FRAGMENT_TAG) != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.hide(filterFragment);
            transaction.commit();
        }
    }

    private void animateBottomToolbarToTopOfFilter(){
        float filterLayoutTop = findViewById(R.id.powerListFilterRecyclerView).getY();
        Log.d(TAG, "animateBottomToolbarToTopOfFilter: filterLayoutTop: " + filterLayoutTop);
        float animationTarget2 = filterLayoutTop - bottomToolbar.getY();


        Log.d(TAG, "animateBottomToolbarToTopOfFilter: bottom frame y coord: " + bottomToolbar.getY());
        //request the top coordinates of layout where filter fragment is in (fragment bound by it)
        float frameTop = findViewById(R.id.viewPagerLayout).getY();
        //calculate the top coordinates of the filter, remove the toolbar height
        float toolbarTop = frameTop
                + getResources().getDimension(R.dimen.margin_top_filter_fragment)
                - bottomToolbar.getHeight();
        //calculate the movement needed based on toolbar current location
        float animationTarget = toolbarTop - bottomToolbar.getY();

        //create the animation and run it
        bottomToolbarAnimator = ObjectAnimator.ofFloat(bottomToolbar, "translationY", animationTarget2);
        bottomToolbarAnimator.setDuration(getResources().getInteger(R.integer.filter_slide_in_animation_length));
        bottomToolbarAnimator.start();
    }

    private void animateBottomToolbarToTopOfFilter(float fragmentHeight){

        Log.d(TAG, "animateBottomToolbarToTopOfFilter");

        if(fragmentManager.findFragmentByTag(FILTER_FRAGMENT_TAG) != null) {
            //filter's height as negative value is the toolbar animation target
            //animations work as relative to current view position, so here we move the toolbar up
            //by the filter fragment's height
            float animationTarget = -fragmentHeight;
            //create the animation and run it
            bottomToolbarAnimator = ObjectAnimator.ofFloat(bottomToolbar, "translationY", animationTarget);
            bottomToolbarAnimator.setDuration(getResources().getInteger(R.integer.filter_slide_in_animation_length));
            bottomToolbarAnimator.start();
        }
    }

    private void animateToolbarToBottomOfScreen(){
        /*Log.d(TAG, "animateToolbarToBottomOfScreen: bottom frame y coord: " + bottomToolbar.getY());
        View fragmentFrame = findViewById(R.id.fragmentFrameLayout);
        float animationTarget = fragmentFrame.getBottom() - bottomToolbar.getHeight();
        Log.d(TAG, "animateToolbarToBottomOfScreen: animationtarget " + animationTarget);
        float animateTo = animationTarget - bottomToolbar.getTop();
        Log.d(TAG, "animateToolbarToBottomOfScreen: animateto " + animateTo);*/

        //for some reason, if we give 0 as target for the animation it is animated to its' original place
        //might be that if we saved the objectAnimator in animateBottomToolbarToTopOfFilter we could just
        //call .reverse on it.
        if(bottomToolbarAnimator != null)
            bottomToolbarAnimator.reverse();
        else {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(bottomToolbar, "translationY", 0);
            objectAnimator.setDuration(getResources().getInteger(R.integer.filter_slide_out_animation_length));
            objectAnimator.start();
        }
        Log.d(TAG, "animateToolbarToBottomOfScreen: toolbar y after animation " + bottomToolbar.getY());
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
                addFilterFragment(fragmentManager);
                //animateBottomToolbarToTopOfFilter();
            }
            else{
                removeFilterFragment(fragmentManager);
                animateToolbarToBottomOfScreen();
            }
        }
    }

    /**
     * OnClickListener for when user presses 'add new power' button. Opens the powerDetails activity.
     */
    private class OnNewPowerClickListener implements View.OnClickListener{

        //https://android-developers.googleblog.com/2014/10/implementing-material-design-in-your.html
        // Activity + Fragment Transitions
        //https://github.com/codepath/android_guides/wiki/Shared-Element-Activity-Transition
        @Override
        public void onClick(View v) {
            //activity transition stuff when starting activity. Commented out since they
            //make the fab (where transition is bound to now) visible when it shouldnt be. The transition
            //is not too useful either so...
            Intent i = new Intent(MainActivity.this, PowerDetailsActivity.class);
            /*String transitionName = "fabTransition";
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            MainActivity.this,
                            mainToolbarFab,
                            transitionName);
            Bundle bundle = options.toBundle();*/

            i.putExtra(PowerDetailsActivity.EXTRA_POWER_DETAIL_ID,
                    PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS);
            //ActivityCompat.startActivity(MainActivity.this, i, bundle);
            ActivityCompat.startActivity(MainActivity.this, i, null);
        }
    }

    /**
     * OnClickListener for when user clicks on the Search button. Hides other bottom toolbar buttons,
     * brings SearchView visible and changes toolbar colour.
     */
    private class SearchViewClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            //close the filter if it's open
            boolean filterWasVisible = removeFilterFragment(getSupportFragmentManager());
            //animate the toolbar to bottom of screen if needed
            if(filterWasVisible)
                animateToolbarToBottomOfScreen();
            bottomToolbar.setColor(bottomToolbarSearchColor);
            //hide the buttons and bring searchView visible
            filterButton.setVisibility(View.GONE);
            searchButton.setVisibility(View.GONE);
            addSpellButton.setVisibility(View.GONE);
            searchView.setVisibility(View.VISIBLE);
            //stopSearchButton.setVisibility(View.VISIBLE);
            searchView.onActionViewExpanded();
        }
    }

    private class StopSearchClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            showBottomBarButtons();
        }
    }

    private void showBottomBarButtons(){
        bottomToolbar.setColor(bottomToolbarDefaultColor);
        filterButton.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.VISIBLE);
        addSpellButton.setVisibility(View.VISIBLE);
        searchView.setVisibility(View.GONE);
        searchView.setQuery("", false);
    }

    //interface PowersFragment.ListScrolledInterface

    /**
     * Called when the list in the powers fragment has been scrolled. Used for retracting the toolbar
     * into the fab
     */
    @Override
    public void listScrolled() {
        //hide the bottom toolbar
        if(bottomToolbar.isFabExpanded() && filterFragment == null)
            bottomToolbar.slideOutFab();

        //if there is nothing in search, hide it
        String searchViewText = searchView.getQuery().toString().trim();
        if(searchViewText.equals("")){
            showBottomBarButtons();
        }
    }

    //interface SpellFilterFragment.onFragmentExpandedListenerInterface

    /**
     * Called when the filter fragment has been expanded and layout calculated.
     * Will animate the bottom toolbar to top of the fragment.
     * @param fragmentTopY the Y coordinates of the fragment
     */
    @Override
    public void fragmentExpanded(float fragmentTopY) {
        animateBottomToolbarToTopOfFilter(fragmentTopY);
    }

    //interface OnSharedPreferenceChangeListener
    //needed if later we add capability for large screen that might have the settings visible as additional fragment
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.preferences_filter_by_cross_section))) {
            setPresenterFilterMode();
        }
    }
}
