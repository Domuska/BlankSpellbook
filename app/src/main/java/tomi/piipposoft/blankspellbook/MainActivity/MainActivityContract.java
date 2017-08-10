package tomi.piipposoft.blankspellbook.MainActivity;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.TreeSet;

import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface MainActivityContract {

    interface View{
        //add and remove data from the powerLists fragment
        void addPowerListData(String name, String id);
        void addPowerNameToPowerList(String name, String powerListId);
        void removePowerListData(String powerListName, String id);

        //add and remove data from the dailyPowerLists fragment
        void addDailyPowerListData(String name, String id);
        void removeDailyPowerListData(String dailyPowerListName, String id);
        void addPowerNameToDailyPowerList(String powerName, String dailyPowerListId);

        //add, set and remove data from powersFragment
        void addNewPowerToList(Spell power, String powerListName);
        void removePowerFromList(Spell power);
        void setPowerListData(ArrayList<Spell> powers);

        //for starting the activities when fragment elements are clicked
        void startPowerListActivity(String name, String id,
                                    android.view.View originView, int powerListColor);
        void startDailyPowerListActivity(String listName, String listId);
        void startPowerDetailsActivity(String powerId, String powerListId,
                                       android.view.View transitionOrigin, String powerListName);

        //for showing, filtering and removing data from the filter fragment
        //void givePowerListNamesForFilter(ArrayList<String> powerListNames);
        //void givePowerGroupNamesForFilter(ArrayList<String> powerGroupNames);
        void showFilteredGroups(TreeSet<String> filteredPowers);
        void showFilteredPowerLists(TreeSet<String> filteredPowers);


    }

    interface UserActionListener{
        void resumeActivity();
        void pauseActivity();
        void saveInstanceState(Bundle outState);
        void userSwitchedTo(int selectedList);
        TreeSet<String> getGroupNamesForFilter();
        ArrayList<String> getSelectedGroupsForFilter();
        TreeSet<String> getPowerListNamesForFilter();
        ArrayList<String> getSelectedPowerListsForFilter();
    }

    /**
     * Interface for communication between PowersFragment/RecyclerListFragment and presenter
     */
    interface FragmentUserActionListener {
        /**
         * Fragment telling presenter that user has clicked on a power list item
         * @param listName Name of the list
         * @param listId ID of the list
         * @param originView View where transitionAnimation can start, can be null
         * @param powerListColor Color that is displayed in the power list, can be null
         */
        void onPowerListClicked(@NonNull String listName, @ NonNull String listId,
                                @Nullable android.view.View originView, int powerListColor);
        void onPowerClicked(String powerId, String powerListId,
                            android.view.View originView, String powerListName);
    }

    /**
     * Interface for communication between presenter and SpellFilterFragment
     */
    interface FilterFragmentUserActionListener{
        void filterGroupsAndPowersWithPowerListName(String powerListName);
        void filterPowerListsAndPowersWithGroupName(String groupName);
        void removeFilter(String filterName, int filterType);
    }

    /**
     * interface for informing presenter when certain fragments have been
     * created so presenter can supply the data to be shown
     */
    interface ListeningStateInterface {
        void startListeningForDailyPowerLists();
        void startListeningForPowerLists();
        void startListeningForPowers();
    }

    interface preferencesInterface{
        void filterStyleChanged(boolean newValue);
    }
}
