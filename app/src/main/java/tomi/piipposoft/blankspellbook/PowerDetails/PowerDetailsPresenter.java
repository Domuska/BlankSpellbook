package tomi.piipposoft.blankspellbook.PowerDetails;

import android.provider.ContactsContract;
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
    private String powerListId;
    private static Spell thisSpell;

    public PowerDetailsPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull PowerDetailsContract.View powerDetailsView,
            @NonNull DrawerHelper drawerHelper,
            String spellId,
            String powerLIstId){
        super(dbHelper, drawerHelper);
        mPowerDetailsView = powerDetailsView;
        mDrawerActivityView = (DrawerContract.ViewActivity) mPowerDetailsView;
        powerId = spellId;
        this.powerListId = powerLIstId;
        thisSpell = new Spell();
    }

    public static void handleFetchedSpell(Spell spell, String id) {
        if(spell != null) {
            powerId = id;
            thisSpell = spell;

            //check that spell has at least empty string in all fields
            if (spell.getAttackType() == null)
                spell.setAttackType("");
            if (spell.getAttackRoll() == null)
                spell.setAttackRoll("");
            if (spell.getCastingTime() == null)
                spell.setCastingTime("");
            if (spell.getGroupName() == null)
                spell.setGroupName("");
            if (spell.getHitDamageOrEffect() == null)
                spell.setHitDamageOrEffect("");
            if (spell.getMissDamage() == null)
                spell.setMissDamage("");
            if (spell.getName() == null)
                spell.setName("");
            if (spell.getPlayerNotes() == null)
                spell.setPlayerNotes("");
            if (spell.getRechargeTime() == null)
                spell.setRechargeTime("");
            if (spell.getTarget() == null)
                spell.setTarget("");
            if (spell.getAdventurerFeat() == null)
                spell.setAdventurerFeat("");
            if (spell.getChampionFeat() == null)
                spell.setChampionFeat("");
            if (spell.getEpicFeat() == null)
                spell.setEpicFeat("");
            if (spell.getTrigger() == null)
                spell.setTrigger("");
            mPowerDetailsView.showFilledFields(spell);
        }
    }


    // FROM PowerDetailsContract.UserActionListener

    @Override
    public void showPowerDetails() {
        if(powerId.equals(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS)){
            mPowerDetailsView.showEmptyFields();
            mPowerDetailsView.setCancelAsGoBack(true);
        }
        else{
            mPowerDetailsView.setCancelAsGoBack(false);
            DataSource.getSpellWithId(powerId);
        }
    }

    @Override
    public void userSavingPower(Spell spell) {
// TODO: 12.5.2017 should disable the edit buttons and such until spell saved to DB, otherwise could encounter weird things
        DataSource.saveSpell(spell, powerListId);
        mPowerDetailsView.hideUnUsedFields(spell);
        mPowerDetailsView.setCancelAsGoBack(false);
        //after this we wait for db to send us the just-saved spell until we show it to the user
    }

    @Override
    public void userEditingPower(Spell spell) {
        mPowerDetailsView.showSpellEditView(spell);
    }

    @Override
    public void userSavingModifiedPower(Spell spell) {
        DataSource.updateSpell(spell, powerId);
        mPowerDetailsView.hideUnUsedFields(spell);
    }

    @Override
    public void userCancelingEdits() {
        mPowerDetailsView.showFilledFields(thisSpell);
        mPowerDetailsView.hideUnUsedFields(thisSpell);
    }

    @Override
    public void userPressingCancelButton(Spell spell) {
        if(thisSpell.equals(spell))
            mPowerDetailsView.cancelEdits();
        else
            mPowerDetailsView.showDiscardChangesDialog();
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
        showPowerLists();
    }

    @Override
    public void dailyPowerListProfileSelected() {
        this.showDailyPowerLists();
    }

    @Override
    public void userPressingAddToLists() {
        mPowerDetailsView.showAddToListsFragment();
        //get data from DB
        String[] list1 = {"seppo", "ismo", "asko", "mursu", "heppu", "joppa", "test11", "test2",
                "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3"};
        String[] list2 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
                "13", "14", "15", "16"};
        String[] list3 = {"sepon tämänpäiväiset", "suuren big bad evil guyn dailyt"};
        String[] list4 = {"123", "3312"};

        DataSource.getPowerLists();
        DataSource.getDailyPowerLists();
        //mPowerDetailsView.addPowerListsToFragment(list1, list2, list3, list4);
    }

    public static void handleFetchedPowerLists(String[] names, String[] ids) {
        mPowerDetailsView.addPowerListsToFragment(names, ids);
    }

    public static void handleFetchedDailyPowerLists(String[] names, String[] ids) {
        mPowerDetailsView.addDailyPowerListsToFragment(names, ids);
    }
}
