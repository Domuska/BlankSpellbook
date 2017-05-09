package tomi.piipposoft.blankspellbook.Drawer;

import android.support.annotation.NonNull;

import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.List;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface DrawerContract {


    /**
     * Used by presenters to tell the drawer to show a list of items
     */
    interface View {
        void showDailyPowerList(List<IDrawerItem> drawerItems);
        void showPowerList(List<IDrawerItem> drawerItems);
        void addDrawerItem(IDrawerItem item);
        void removeDrawerItems();
    }

    /**
     * Used by presenters to tell the running activity to start new activites
     */
    interface ViewActivity {
        void openPowerList(String powerListId, String name);
        void openDailyPowerList(Long dailyPowerListId);
    }

    /**
     * Used by activities to tell presenter that an item has been clicked or should be added
     */
    interface UserActionListener{

        /**
         * Add a new list of powers (activated by clicking the add new power in the drawer)
         * @param powerListName
         */
        void addPowerList(@NonNull String powerListName);

        /**
         * Add a new list of daily powers (activated by clicking the add new power list in drawer)
         * @param dailyPowerListName
         */
        void addDailyPowerList(@NonNull String dailyPowerListName);

        void drawerOpened();

        void powerListItemClicked(String itemId, String name);
        void dailyPowerListItemClicked(long itemId);

        /**
         * The user selected the power lists section of the drawer
         */
        void powerListProfileSelected();

        /**
         * the user has selected the daily power lists section of the drawer
         */
        void dailyPowerListProfileSelected();



    }
}
