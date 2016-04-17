package tomi.piipposoft.blankspellbook.powerlist;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface PowerListContract {

    interface View{
        void showPowerDetailUI(long itemId);
    }

    interface UserActionListener{
        void openPowerDetails(long itemId);
    }
}
