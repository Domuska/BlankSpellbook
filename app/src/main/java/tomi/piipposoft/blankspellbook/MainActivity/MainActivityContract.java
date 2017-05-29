package tomi.piipposoft.blankspellbook.MainActivity;


import android.support.v4.util.ArrayMap;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface MainActivityContract {


    interface View{
        void addPowerListData(String name, String id);
        void addDailyPowerListData(String name, String id);
        void removePowerListData(String powerListName, String id);
        void removeDailyPowerListData(String dailyPowerListName, String id);
    }

    interface UserActionListener{
        void resumeActivity();
    }
}
