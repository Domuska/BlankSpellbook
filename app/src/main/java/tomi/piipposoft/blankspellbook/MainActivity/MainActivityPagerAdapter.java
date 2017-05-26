package tomi.piipposoft.blankspellbook.MainActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by OMISTAJA on 26.5.2017.
 */

public class MainActivityPagerAdapter extends FragmentPagerAdapter{

    Fragment spellsFragment, spellListFragment;

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
        if(position == 1)
            return spellsFragment;
        else
            return spellListFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }
}
