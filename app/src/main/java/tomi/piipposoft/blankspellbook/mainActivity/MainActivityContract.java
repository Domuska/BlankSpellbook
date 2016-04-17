package tomi.piipposoft.blankspellbook.mainActivity;

import android.support.annotation.NonNull;

import tomi.piipposoft.blankspellbook.drawer.DrawerContract;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface MainActivityContract {

    interface View{

        void dummyMethod();
    }

    interface UserActionListener{
        DrawerContract.UserActionListener getInstance();
    }
}
