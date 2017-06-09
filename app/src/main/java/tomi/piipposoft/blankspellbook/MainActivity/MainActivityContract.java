package tomi.piipposoft.blankspellbook.MainActivity;

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
        void startPowerListActivity(String name, String id);
        void startDailyPowerListActivity(String listName, String listId);
        void startPowerDetailsActivity(String powerId, String powerListId);
    }

    interface UserActionListener{
        void resumeActivity();
        void pauseActivity();
        void userSwitchedTo(int selectedList);
    }

    interface FragmentUserActionListener {
        void onPowerListClicked(String listName, String listId);
        void onPowerClicked(String powerId, String powerListId);
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
