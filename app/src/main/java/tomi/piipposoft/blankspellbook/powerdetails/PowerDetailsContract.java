package tomi.piipposoft.blankspellbook.powerdetails;

/**
 * Created by Domu on 17-Apr-16.
 */
public interface PowerDetailsContract {

    interface View {
        void showEmptyForms();
        void showFilledForms();
    }

    interface UserActionListener{

        void showPowerDetails(long powerId);
    }
}
