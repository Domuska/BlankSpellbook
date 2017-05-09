package tomi.piipposoft.blankspellbook.PowerList;

import android.content.Context;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface PowerListContract {

    interface View{

        /**
         * Indicate that the PowerDetailsActivity should be started
         * called when an existing power is to be shown
         * @param itemId the ID of the power that is opened
         */
        void showPowerDetailsUI(String itemId);

        /**
         * Indicate that the PowerDetailsActivity should be started
         * called when a new power is to be added
         */
        void showNewPowerUI();

        /**
         * Add a single Spell object to the currently displayed list
         * @param spell the Spell object to be added
         */
        void addSpellToList(Spell spell);

        /**
         * Remove a single spell from the currently displayed list of spells
         * @param spell an initialized Spell object to be removed
         */
        void removeSpellFromList(Spell spell);
    }

    interface UserActionListener{
        void getSpellList(Context context, String powerListId);
        void openPowerDetails(String itemId, boolean addingNewPower);
    }
}
