package tomi.piipposoft.blankspellbook.PowerList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

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
        void showPowerDetailsUI(String itemId, String itemName, String powerListId,
                                android.view.View transitioningView, String powerListName);

        /**
         * Indicate that the PowerDetailsActivity should be started
         * called when a new power is to be added
         */
        void showNewPowerUI(String powerListId, String powerListName);

        /**
         * Add a single Spell object to the currently displayed list
         * @param spell the Spell object to be added
         */
        void addSpellToList(Spell spell);

        /**
         * Remove a single spell from the currently displayed list of spells
         * @param spell an initialized Spell object to be removed
         * @param isLastPowerInGroup if power was last in this group and group should be removed
         */
        void removeSpellFromList(Spell spell, boolean isLastPowerInGroup);

        /**
         * Show power the power deleted snackbar to user
         */
        void showPowerDeletedSnackBar();

        void showEmptyPowersList();
    }

    interface UserActionListener{
        /**
         * Get presenter to display the list of spells
         * @param powerListId ID of the power list
         */
        void getPowersForDisplay(String powerListId);

        /**
         * User is opening the powerDetailsActivity
         * @param itemId the item that has been clicked, either empty string or a valid FireBase ID
         * @param transitioningView the view used for activityTransition animation origin, can be null
         */
        void openPowerDetails(@NonNull String itemId, @Nullable String itemName, @Nullable android.view.View transitioningView);

        /**
         * Inform presenter that activity is resuming
         */
        void resumeActivity();

        /**
         * Inform presenter that activity is pausing
         */
        void pauseActivity();

        /**
         * User is pressing delete button to delete selected powers
         */
        void userDeletingPowersFromList(ArrayList<Spell> deletablePowers);

        /**
         * User is pushing undo in the SnackBar when removing powers from list
         */
        void userPushingUndo();
    }
}
