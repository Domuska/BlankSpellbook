package tomi.piipposoft.blankspellbook.MainActivity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by OMISTAJA on 26.5.2017.
 *
 * Used for handling fragments inside a ViewPager. Should contain 3 fragments,
 * one for displaying powers, one for displaying power lists and one for displaying daily power lists
 * This class should handle creation and communication to the fragments, do not create these
 * fragments outside this class. See instantiateItem for saving the fragment references
 * and the Stackoverflow answer https://stackoverflow.com/a/29269509/4768728
 *
 */

public class MainActivityPagerAdapter extends FragmentPagerAdapter{

    public final int FRAGMENTS_AMOUNT = 3;

    private static final String TAG = "MainActiviPagerAdapter";
    private PowersFragment powersFragment;
    private RecyclerListFragment powerListsFragment, dailyPowerListsFragment;

    //listener for presenter when user clicks on (daily) power list fragment list item,
    //attached to those fragments on creation
    private MainActivityContract.FragmentUserActionListener fragmentListActionListener;
    //listener for telling presenter when fragments have been created so it can supply the data
    private MainActivityContract.ListeningStateInterface listeningStateInterface;

    //list for powers that are added before fragment is ready
    private ArrayList<Spell> powersInQueue = new ArrayList<>();

    public MainActivityPagerAdapter(FragmentManager manager,
                                    MainActivityContract.FragmentUserActionListener actionListener,
                                    MainActivityContract.ListeningStateInterface listeningStateInterface){
        super(manager);
        this.fragmentListActionListener = actionListener;
        this.listeningStateInterface = listeningStateInterface;
    }

    //override this, so we can save the references to the fragments safely, we can call methods on them later
    //https://stackoverflow.com/a/29269509/4768728
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                dailyPowerListsFragment = (RecyclerListFragment) createdFragment;
                //attach presenter as listener
                dailyPowerListsFragment.attachClickListener(fragmentListActionListener);
                //inform presenter that fragment has been created
                listeningStateInterface.startListeningForDailyPowerLists();
                Log.d(TAG, "instantiateItem: daily power list fragment created");
                break;
            case 1:
                powerListsFragment = (RecyclerListFragment) createdFragment;
                //attach the presenter as listener
                powerListsFragment.attachClickListener(fragmentListActionListener);
                //inform presenter fragment has been created
                listeningStateInterface.startListeningForPowerLists();
                Log.d(TAG, "instantiateItem: power list fragment created");
                break;
            case 2:
                powersFragment = (PowersFragment) createdFragment;
                //attach the presenter as listener
                powersFragment.attachClickListener(fragmentListActionListener);
                //inform presenter that powers fragment has been created
                listeningStateInterface.startListeningForPowers();
                Log.d(TAG, "instantiateItem: powers fragment created. Queue size: " + powersInQueue.size());
                for(Spell power : powersInQueue){
                    powersFragment.handleNewPower(power, power.getPowerListName());
                }
                break;
        }
        return createdFragment;
    }

    @Override
    public int getCount() {
        return FRAGMENTS_AMOUNT;
    }


    @Override
    public Fragment getItem(int position) {
        //we return new fragments in here, this is only called when an instance
        //is not already saved in the FragmentManager. Do not save the fragment instances
        //here since this might not be called, save them in instantiateItem.
        switch(position){
            case 0:
                return new RecyclerListFragment();
            case 1:
                return new RecyclerListFragment();
            case 2:
                return new PowersFragment();
            default:
                Log.d(TAG,"Unknown value in getItem: " + position);
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    /**
     * Add a new item to power lists fragment
     * @param name name of the list
     * @param id ID of the list
     */
    void addPowerListToFragment(String name, String id){
        if(powerListsFragment != null){
            powerListsFragment.handleNewListItem(name, id);
        }
    }

    /**
     * add a name of a power to a power list
     * @param powerName name of the power
     * @param powerListId ID of the power list where the power is in
     */
    void addPowerNameToPowerList(String powerName, String powerListId){
        if(powerListsFragment != null){
            Log.d(TAG, "addPowerNameToPowerList: adding power with name " + powerName + " to power list " + powerListId);
            powerListsFragment.handleNewPowerName(powerName, powerListId);
        }
        else
            Log.d(TAG, "addPowerNameToPowerList: power list fragment still null");
    }

    /**
     * remove all items from power lists fragment
     */
    void removePowerListsFromFragment(){
        if(powerListsFragment != null){
            powerListsFragment.removeAllElements();
        }
    }


    /**
     * add an item to daily power lists fragment
     * @param name name of the daily power list
     * @param id ID of the daily power list
     */
    void addDailyPowerListToFragment(String name, String id) {
        if(dailyPowerListsFragment != null) {
            dailyPowerListsFragment.handleNewListItem(name, id);
        }
    }

    /**
     * add name of a power to daily power list
     * @param powerName name of the power
     * @param dailyPowerListId ID of the daily power list that has this item
     */
    void addPowerNameToDailyPowerList(String powerName, String dailyPowerListId) {
        if(dailyPowerListsFragment != null){
            dailyPowerListsFragment.handleNewPowerName(powerName, dailyPowerListId);
        }
    }

    /**
     * remove all items from daily power lists fragment
     */
    void removeDailyPowerListsFromFragment(){
        if(dailyPowerListsFragment != null){
            dailyPowerListsFragment.removeAllElements();
        }
    }

    /**
     * remove a power list from power lis ts fragment
     * @param name name of the power list
     * @param id ID of the power list
     */
    void removePowerListItem(String name, String id){
        if(powerListsFragment != null)
            powerListsFragment.removeListItem(name, id);
    }

    /**
     * remove a daily power list from daily power lists fragment
     * @param name name of the daily power list
     * @param id ID of the list
     */
    void removeDailyPowerListItem(String name, String id){
        if(dailyPowerListsFragment != null)
            dailyPowerListsFragment.removeListItem(name, id);
    }

    /**
     * add a new power to powers fragment
     * @param power the power that is to be added
     * @param powerListName name of the power list this power belongs to, can be null
     */
    void addPowerToFragment(@NonNull Spell power, @Nullable String powerListName){
        Log.d(TAG, "adding new power " + power.getName() + " to fragment");
        if(powersFragment != null)
            powersFragment.handleNewPower(power, powerListName);
        else{
            //add the power into the queue, they will be added to fragment when it is created
            Log.d(TAG, "adding power " + power.getName() + " to queue");
            powersInQueue.add(power);
        }
    }

    /**
     * remove a power from powers fragment
     * @param power the power that is to be removed
     */
    void removePowerFromFragment(Spell power){
        if(powersFragment != null)
            powersFragment.removePower(power);
    }

    /**
     * remove all powers from the powers fragment
     */
    void removeAllPowers(){
        if(powersFragment != null)
            powersFragment.removeAllPowers();
    }

    /**
     * Set the powers that should be shown in the fragment - used when filtering powers
     * @param powersData list of Spells that should be displayed
     */
    void setPowersData(ArrayList<Spell> powersData){
        if(powersFragment != null)
            powersFragment.setPowers(powersData);
    }
}

