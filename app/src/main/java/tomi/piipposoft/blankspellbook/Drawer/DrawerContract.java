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
     * Used by activities to tell presenter that an item in the list has been clicked
     */
    interface UserActionListener{

        void addPowerList(@NonNull String powerListName);
        void addDailyPowerList(@NonNull String dailyPowerListName);

        void drawerOpened();

        void powerListItemClicked(String itemId, String name);
        void dailyPowerListItemClicked(long itemId);

        void powerListProfileSelected();
        void dailyPowerListProfileSelected();



    }
}
