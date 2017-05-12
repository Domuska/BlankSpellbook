package tomi.piipposoft.blankspellbook.PowerDetails;

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
    }

    interface UserActionListener{
        void showPowerDetails();
        void userSavingPower(Spell spell);
        void userEditingPower(Spell spell);
        void userSavingModifiedPower(Spell spell);
        void userCancelingEdits();
    }
}
