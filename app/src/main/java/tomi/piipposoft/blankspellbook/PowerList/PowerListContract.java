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
        /**
         * Get presenter to display the list of spells
         * @param context
         * @param powerListId ID of the power list
         */
        void getSpellList(Context context, String powerListId);

        /**
         * User is opening the powerDetailsActivity
         * @param itemId the item that has been clicked, either empty string or a valid FireBase ID
         */
        void openPowerDetails(String itemId);

        /**
         * Activity is pausing, tell presenter to do necessary actions
         */
        void activityPausing();
    }
}
