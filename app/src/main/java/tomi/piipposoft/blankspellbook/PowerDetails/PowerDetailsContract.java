package tomi.piipposoft.blankspellbook.PowerDetails;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface PowerDetailsContract {

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
    }

    interface UserActionListener{
        void showPowerDetails(boolean wasUserEditingSpell);
        void userSavingPower(Spell spell);
        void userEditingPower(Spell spell);
        void userSavingModifiedPower(Spell spell);
        void userCancelingEdits();
        void userPressingCancelButton(Spell spell);
        void userPressingAddToLists();
        void userAddingPowerToLists(ArrayList<String> listIds, boolean addingToPowerList);
    }
}
