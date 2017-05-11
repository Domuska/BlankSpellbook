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
    private static Spell thisSpell;

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
        thisSpell = spell;

        //check that spell has at least empty string in all fields
        if (spell.getAttackType() == null)
            spell.setAttackType("");
        if(spell.getAttackRoll() == null)
            spell.setAttackRoll("");
        if(spell.getCastingTime() == null)
            spell.setCastingTime("");
        if(spell.getGroupName() == null)
            spell.setGroupName("");
        if(spell.getHitDamageOrEffect() == null)
            spell.setHitDamageOrEffect("");
        if(spell.getMissDamage() == null)
            spell.setMissDamage("");
        if(spell.getName() == null)
            spell.setName("");
        if(spell.getPlayerNotes() == null)
            spell.setPlayerNotes("");
        if(spell.getRechargeTime() == null)
            spell.setRechargeTime("");
        if(spell.getTarget() == null)
            spell.setTarget("");
        if(spell.getAdventurerFeat() == null)
            spell.setAdventurerFeat("");
        if(spell.getChampionFeat() == null)
            spell.setChampionFeat("");
        if(spell.getEpicFeat() == null)
            spell.setEpicFeat("");
        if(spell.getTrigger() == null)
            spell.setTrigger("");
        mPowerDetailsView.showFilledFields(spell);
    }


    // FROM PowerDetailsContract.UserActionListener

    @Override
    public void showPowerDetails() {
        if(powerId.equals(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS)){
            mPowerDetailsView.showEmptyForms();
            mPowerDetailsView.setCancelAsGoBack(true);
        }
        else{
            mPowerDetailsView.setCancelAsGoBack(false);
            DataSource.getSpellWithId(powerId);
        }
    }

    @Override
    public void userSavingPower(Spell spell) {
        DataSource.saveSpell(spell);
        // TODO: 11.5.2017 hide the fields that are not in the newly saved spell 
        //after this we wait for db to send us the just-saved spell until we show it to the user
    }

    @Override
    public void userEditingPower(Spell spell) {
        mPowerDetailsView.showSpellEditView(spell);
    }

    @Override
    public void userSavingModifiedPower(Spell spell) {
        DataSource.updateSpell(spell, powerId);
    }

    @Override
    public void userCancelingEdits() {
        mPowerDetailsView.showFilledFields(thisSpell);
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
