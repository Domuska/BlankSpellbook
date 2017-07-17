package tomi.piipposoft.blankspellbook.MainActivity;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

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

        //add and remove data from powersFragment
        void addNewPowerToList(Spell power, String powerListName);
        void removePowerFromList(Spell power);

        //for starting the activities when fragment elements are clicked
        void startPowerListActivity(String name, String id,
                                    android.view.View originView, int powerListColor);
        void startDailyPowerListActivity(String listName, String listId);
        void startPowerDetailsActivity(String powerId, String powerListId, android.view.View transitionOrigin);

        //showing powers filtered in the filter fragment
        void showFilteredPowers(ArrayList<Spell> filteredPowers);
        void showFilteredPowerGroups(ArrayList<Spell> filteredPowers);
        void showFilteredPowerLists(ArrayList<Spell> filteredPowers);
    }

    interface UserActionListener{
        void resumeActivity();
        void pauseActivity();
        void userSwitchedTo(int selectedList);
        ArrayList<String> getGroupNamesForFilter();
        ArrayList<String> getClassNamesForFilter();
    }

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
        void onPowerClicked(String powerId, String powerListId, android.view.View originView);
    }

    interface FilterFragmentUserActionListener{
        void filterGroupsAndPowersWithPowerListName(String powerListName);
        void filterPowerListsAndPowersWithGroupName(String groupName);
    }

    /**
     * interface for informing presenter when certain fragments have been
     * created so presenter can supply the data to be shown
     */
    interface PagerAdapterListener{
        void onDailyPowerListFragmentCreated();
        void onPowerListFragmentCreated();
        void onPowersFragmentCreated();
    }
}
