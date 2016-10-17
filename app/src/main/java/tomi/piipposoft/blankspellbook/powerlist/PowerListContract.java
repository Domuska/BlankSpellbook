package tomi.piipposoft.blankspellbook.powerlist;

import android.content.Context;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface PowerListContract {

    interface View{
        void showPowerDetailsUI(long itemId);
        void showNewPowerUI();
    }

    interface UserActionListener{
        ArrayList<Spell> getSpellList(Context context, long powerListId);
        void openPowerDetails(long itemId, boolean addingNewPower);
    }
}
