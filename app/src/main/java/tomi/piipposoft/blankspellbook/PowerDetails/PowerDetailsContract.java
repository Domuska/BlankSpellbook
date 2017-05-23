package tomi.piipposoft.blankspellbook.PowerDetails;

import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface PowerDetailsContract {

    String name = "name";
    String attackType = "attackType";
    String recharge = "recharge";
    String castingTime = "castingTime";
    String target = "target";
    String attackRoll = "attackRoll";
    String hitDamageOrEffect = "hitDamageOrEffect";
    String missDamage = "missDamage";
    String adventurerFeat = "adventurerFeat";
    String championFeat = "championFeat";
    String epicFeat = "epicFeat";
    String groupName = "groupName";
    String playerNotes = "playerNotes";
    String trigger = "trigger";

    interface View {
        void showEmptyFields();
        void showFilledFields(Spell spell);
        void showSpellEditView(Spell spell);
        void setCancelAsGoBack(boolean b);
        void hideUnUsedFields(Spell spell);
        void showDiscardChangesDialog();
        void cancelEdits();
        void showAddToListsFragment();
        void addPowerListsToFragment(String[] powerListNames, String[] powerListIds);
        void addDailyPowerListsToFragment(String[] dailyPowerListNames, String[] dailyPowerListIds);
        void showErrorSavingEmptyFields();
    }

    interface UserActionListener{
        void showPowerDetails(boolean wasUserEditingSpell);
        void userSavingPower(ArrayMap<String, String> powerData);

        // TODO: 23.5.2017 why is userEditingPower passing in spell argument?
        void userEditingPower(Spell spell);
        void userSavingModifiedPower(ArrayMap<String, String> powerData);
        void userCancelingEdits();
        void userPressingCancelButton(ArrayMap<String, String> powerData);
        void userPressingAddToLists();
        void userAddingPowerToLists(ArrayList<String> listIds, boolean addingToPowerList);

        /**
         * Handle AddToPowerListDialog re-creation in onresume, should re-populate
         * the list in the dialog
         */
        void activityResumingWithFragment();
    }
}
