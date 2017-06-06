package tomi.piipposoft.blankspellbook.MainActivity;

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

    private PowersFragment powersFragment;
    private RecyclerListFragment powerListsFragment, dailyPowerListsFragment;
    private MainActivityContract.FragmentListActionListener actionListener;
    public static final String TAG = "MainActiviPagerAdapter";

    public MainActivityPagerAdapter(FragmentManager manager,
                                    MainActivityContract.FragmentListActionListener actionListener){
        super(manager);
        this.actionListener = actionListener;
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
                //attach listener
                dailyPowerListsFragment.attachClickListener(actionListener);
                break;
            case 1:
                powerListsFragment = (RecyclerListFragment) createdFragment;
                //attach the listener
                powerListsFragment.attachClickListener(actionListener);
                break;
            case 2:
                powersFragment = (PowersFragment) createdFragment;
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


    void addPowerListToFragment(String name, String id, ArrayList<String> groupNames){
        if(powerListsFragment != null){
            powerListsFragment.handleNewListItem(name, id, groupNames);
        }
    }

    void removePowerListsFromFragment(){
        if(powerListsFragment != null){
            powerListsFragment.removeAllLists();
        }
    }

    void addDailyPowerListToFragment(String name, String id, ArrayList<String> groupNames) {
        if(dailyPowerListsFragment != null)
            dailyPowerListsFragment.handleNewListItem(name, id, groupNames);
    }

    void removeDailyPowerListsFromFragment(){
        if(dailyPowerListsFragment != null){
            dailyPowerListsFragment.removeAllLists();
        }
    }

    void removePowerListItem(String name, String id){
        if(powerListsFragment != null)
            powerListsFragment.removeListItem(name, id);
    }

    void removeDailyPowerListItem(String name, String id){
        if(dailyPowerListsFragment != null)
            dailyPowerListsFragment.removeListItem(name, id);
    }

    void addPowerToFragment(Spell power){
        if(powersFragment != null)
            powersFragment.handleNewPower(power);
    }

    void removePowerFromFragment(Spell power){
        if(powersFragment != null)
            powersFragment.removePower(power);
    }

    void removeAllPowers(){
        if(powersFragment != null)
            powersFragment.removeAllPowers();
    }
}

