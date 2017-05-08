package tomi.piipposoft.blankspellbook.PowerList;

import android.content.Context;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface PowerListContract {

    interface View{
        void showPowerDetailsUI(String itemId);
        void showNewPowerUI();
        void addSpellToAdapter(Spell spell);
    }

    interface UserActionListener{
        void getSpellList(Context context, String powerListId);
        void openPowerDetails(String itemId, boolean addingNewPower);
    }
}
