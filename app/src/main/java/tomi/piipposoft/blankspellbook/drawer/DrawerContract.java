package tomi.piipposoft.blankspellbook.drawer;

import android.support.annotation.NonNull;

import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.List;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface DrawerContract {

    interface View {

        void showPowerListItems(List<IDrawerItem> drawerItems);
        void showDailyPowerListItems(List<IDrawerItem> drawerItems);

        void showDailyPowerList();
        void showPowerList();

    }

    interface UserActionListener{

        void addPowerList(@NonNull String powerListName);
        void addDailyPowerList(@NonNull String dailyPowerListName);

        void listPowerListItemClicked(@NonNull long itemId);
        void listDailyPowerListItemClicked(@NonNull long itemId);

        void powerListProfileSelected();
        void dailyPowerListProfileSelected();



    }
}
