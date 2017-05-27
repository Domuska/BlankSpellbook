package tomi.piipposoft.blankspellbook.MainActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

/**
 * Created by OMISTAJA on 26.5.2017.
 */

public class MainActivityPagerAdapter extends FragmentPagerAdapter{

    private Fragment spellsFragment, spellListFragment;
    public static final String TAG = "MainActiviPagerAdapter";

    public MainActivityPagerAdapter(FragmentManager manager,
                                    Fragment spellsFragment, Fragment spellListFragment){
        super(manager);
        this.spellsFragment = spellsFragment;
        this.spellListFragment = spellListFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 1:
                return spellListFragment;
            case 2:
                return spellsFragment;
            default:
                Log.d(TAG,"Unknown value in getItem: " + position + ". Returning spellsFragment");
                return spellsFragment;

        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }
}
