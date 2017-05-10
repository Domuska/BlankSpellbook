package tomi.piipposoft.blankspellbook.PowerDetails;

import java.util.List;

import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface PowerDetailsContract {

    interface View {
        void showEmptyForms();
        void showFilledForms(Spell spell);
        void showEditableFields();
    }

    interface UserActionListener{
        void showPowerDetails(String powerId);
        void userSavingPower(Spell spell);
    }
}
