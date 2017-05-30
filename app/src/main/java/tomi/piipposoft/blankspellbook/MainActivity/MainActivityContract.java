package tomi.piipposoft.blankspellbook.MainActivity;

import java.util.ArrayList;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface MainActivityContract {

    interface View{
        void addPowerListData(String name, String id, ArrayList<String> groupNames);
        void addDailyPowerListData(String name, String id, ArrayList<String> groupNames);
        void removePowerListData(String powerListName, String id);
        void removeDailyPowerListData(String dailyPowerListName, String id);
    }

    interface UserActionListener{
        void resumeActivity();
        void pauseActivity();
    }
}
