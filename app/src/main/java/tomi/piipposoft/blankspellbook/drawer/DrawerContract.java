package tomi.piipposoft.blankspellbook.drawer;

import android.support.annotation.NonNull;

import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.List;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface DrawerContract {

    interface View {

        void showPowerListItems();
        void showDailyPowerListItems();

        void showDailyPowerList(List<IDrawerItem> drawerItems);
        void showPowerList(List<IDrawerItem> drawerItems);

    }

    interface UserActionListener{

        void addPowerList(@NonNull String powerListName);
        void addDailyPowerList(@NonNull String dailyPowerListName);

        void drawerOpened();

        void listPowerListItemClicked(@NonNull long itemId);
        void listDailyPowerListItemClicked(@NonNull long itemId);

        void powerListProfileSelected();
        void dailyPowerListProfileSelected();



    }
}
