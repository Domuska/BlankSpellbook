package tomi.piipposoft.blankspellbook.PowerDetails;

import android.support.annotation.NonNull;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.Drawer.DrawerPresenter;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public class PowerDetailsPresenter extends DrawerPresenter
        implements PowerDetailsContract.UserActionListener,
        DrawerContract.UserActionListener{

    private static PowerDetailsContract.View mPowerDetailsView;
    private final DrawerContract.ViewActivity mDrawerActivityView;
    private static String powerId;

    public PowerDetailsPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull PowerDetailsContract.View powerDetailsView,
            @NonNull DrawerHelper drawerHelper,
            String spellId){
        super(dbHelper, drawerHelper);
        mPowerDetailsView = powerDetailsView;
        mDrawerActivityView = (DrawerContract.ViewActivity) mPowerDetailsView;
        powerId = spellId;
    }

    public static void handleFetchedSpell(Spell spell, String id) {
        powerId = id;
        mPowerDetailsView.showFilledFields(spell);
    }


    // FROM PowerDetailsContract.UserActionListener

    @Override
    public void showPowerDetails(String powerId) {
        if(powerId.equals(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS)){
            mPowerDetailsView.showEmptyForms();
        }
        else{
            DataSource.getSpellWithId(powerId);
        }
    }

    @Override
    public void userSavingPower(Spell spell) {
        DataSource.saveSpell(spell);
    }

    @Override
    public void userEditingPower(Spell spell) {
        mPowerDetailsView.showSpellEditView(spell);
    }

    @Override
    public void userSavingModifiedPower(Spell spell) {
        DataSource.updateSpell(spell, powerId);
        mPowerDetailsView.showFilledFields(spell);
    }

    // FROM DRAWERCONTRACT USERACTIONLISTENER
    @Override
    public void addPowerList(@NonNull String powerListName) {
        this.addNewPowerList(powerListName);
    }

    @Override
    public void addDailyPowerList(@NonNull String dailyPowerListName) {
        this.addNewDailyPowerList(dailyPowerListName);
    }

    @Override
    public void drawerOpened() {
    }

    @Override
    public void powerListItemClicked(String itemId, String name) {
        mDrawerActivityView.openPowerList(itemId, name);
    }

    @Override
    public void dailyPowerListItemClicked(long itemId) {
        mDrawerActivityView.openDailyPowerList(itemId);
    }

    @Override
    public void powerListProfileSelected() {
        this.showPowerLists();
        if(DrawerPresenter.spellListChildListener == null){
            this.attachSpellListDrawerListener();
        }
    }

    @Override
    public void dailyPowerListProfileSelected() {
        this.showDailyPowerLists();
        if(DrawerPresenter.dailySpellListChildListener == null){
            this.attachDailySpellListDrawerListener();
        }
    }
}
