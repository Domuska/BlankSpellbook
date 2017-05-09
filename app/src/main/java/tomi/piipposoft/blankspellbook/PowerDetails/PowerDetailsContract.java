package tomi.piipposoft.blankspellbook.PowerDetails;

import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface PowerDetailsContract {

    interface View {
        void showEmptyForms();
        void showFilledForms(Spell spell);
    }

    interface UserActionListener{
        void showPowerDetails(String powerId);
    }
}
