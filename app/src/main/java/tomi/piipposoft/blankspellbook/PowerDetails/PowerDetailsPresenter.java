package tomi.piipposoft.blankspellbook.PowerDetails;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import java.util.ArrayList;

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
        implements PowerDetailsContract.UserActionListener{

    private static final String TAG = "PowerDetailsPresenter";
    private static PowerDetailsContract.View mPowerDetailsView;
    private static String powerId;
    private String powerListId;
    private static Spell thisPower;

    private ArrayList<String> previouslySavedPowerListIds;
    private String[] savedPowerIds;
    private boolean addedToPowerList;

    //used when activity gets savedInstanceState bundle, user might have been editing a power
    private static boolean wasUserEditingPower = false;

    public PowerDetailsPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull PowerDetailsContract.View powerDetailsView,
            @NonNull DrawerHelper drawerHelper,
            String spellId,
            String powerLIstId){
        super(dbHelper, drawerHelper, (DrawerContract.ViewActivity) powerDetailsView);
        mPowerDetailsView = powerDetailsView;
        powerId = spellId;
        this.powerListId = powerLIstId;
        thisPower = new Spell();
    }

    /**
     * Fills in blank fields of the spell and tells view to show it
     * @param spell a spell object
     * @param id ID of the spell object
     */
    public static void handleFetchedSpell(Spell spell, String id) {
        if(spell != null) {
            powerId = id;
            thisPower = spell;

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

            if(wasUserEditingPower) {
                mPowerDetailsView.showSpellEditView(spell);
                // since we have handled this situation already, set as false,
                // this method is called again after edits have been done and power saved to DB)
                wasUserEditingPower = false;
            }
            else
                mPowerDetailsView.showFilledFields(spell);
        }
    }

    public static void handleFetchedSpellGroups(String[] powerGroups){
        if(powerGroups != null)
            mPowerDetailsView.populatePowerGroupSuggestions(powerGroups);
    }


    // FROM PowerDetailsContract.UserActionListener

    @Override
    public void showPowerDetails(boolean wasUserEditing) {
        wasUserEditingPower = wasUserEditing;
        if (powerId.equals(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS)) {
            mPowerDetailsView.showEmptyFields();
            mPowerDetailsView.setCancelAsGoBack(true);
        } else {
            mPowerDetailsView.setCancelAsGoBack(false);
            DataSource.getSpellWithId(powerId);
        }

        //powerListId might be null if this power doesn't belong to any group
        if(powerListId != null && !powerListId.equals("")) {
            //get data for the group name's autofill feature
            DataSource.getPowerGroupsWithListId(powerListId);
        }
    }

    @Override
    public void userSavingPower(ArrayMap<String, String> powerData) {

        if(powerData.isEmpty()) {
            mPowerDetailsView.showErrorSavingEmptyFields();
        }
        else{
            // TODO: 12.5.2017 should disable the edit buttons and such until spell saved to DB, otherwise could encounter weird things
            Spell spell = constructPowerFromFields(powerData);
            DataSource.saveSpellAndAddListener(spell, powerListId);
            mPowerDetailsView.hideUnUsedFields(spell);
            mPowerDetailsView.setCancelAsGoBack(false);
            //after this we wait for db to send us the just-saved spell until we show it to the user
        }
    }

    @Override
    public void userEditingPower(Spell spell) {
        mPowerDetailsView.showSpellEditView(spell);
    }

    @Override
    public void userSavingModifiedPower(ArrayMap<String, String> powerData) {
        Spell spell = constructPowerFromFields(powerData);
        if(spell.getGroupName() != null && spell.getGroupName().equals(thisPower.getGroupName()))
            DataSource.updateSpell(spell, powerId, false, null, null);
        else
            DataSource.updateSpell(spell, powerId, true, thisPower.getGroupName(), powerListId);
        mPowerDetailsView.hideUnUsedFields(spell);
    }

    @Override
    public void userCancelingEdits() {
        mPowerDetailsView.showFilledFields(thisPower);
        mPowerDetailsView.hideUnUsedFields(thisPower);
    }

    @Override
    public void userPressingCancelButton(ArrayMap<String, String> powerData) {
        if(thisPower.equals(constructPowerFromFields(powerData)))
            mPowerDetailsView.cancelEdits();
        else
            mPowerDetailsView.showDiscardChangesDialog();
    }

    /**
     * Called to inform that there is new data for power list displaying
     * @param names Array containing the names of the power lists
     * @param ids Array containing the IDs of the power lists, same order names argument
     */
    public static void handleFetchedPowerLists(String[] names, String[] ids) {
        mPowerDetailsView.addPowerListsToFragment(names, ids);
    }

    /**
     * Called to inform that there is new data for daily power lists
     * @param names Array containing the names of the daily power lists
     * @param ids Array containing the IDs of the daily power lists, same order as names argument
     */
    public static void handleFetchedDailyPowerLists(String[] names, String[] ids) {
        mPowerDetailsView.addDailyPowerListsToFragment(names, ids);
    }

    @Override
    public void userCopyingPowerToLists(ArrayList<String> listIds, boolean addingToPowerList) {
        //save the IDs of the lists so we can undo (in snackbar likely)
        previouslySavedPowerListIds = listIds;
        //save whether we saved to a power list or daily power list as well
        addedToPowerList = addingToPowerList;
        //set spell id as null so it won't be saved as field to DB, we don't want that
        Spell saveableSpell = thisPower.setSpellId(null);
        if(addingToPowerList) {
            //save the power IDs so we can undo the action
            savedPowerIds = DataSource.copyPowerToPowerLists(listIds, saveableSpell);
        }
        else
            DataSource.addSpellToDailyPowerLists(listIds,
                    powerId, thisPower.getGroupName());
    }

    @Override
    public void userPushingUndo() {
        //remove the copied powers
        if(addedToPowerList) {
            for(int i = 0; i < previouslySavedPowerListIds.size(); i++) {
                //a copy has been made of the power, remove it, the method takes care of everything
                DataSource.deletePower(savedPowerIds[i]);
            }
        }
        //remove the references from daily power lists
        else {
            DataSource.removeSpellFromDailyPowerLists(previouslySavedPowerListIds, powerId, thisPower.getGroupName());
        }
    }

    @Override
    public void activityResumingWithFragment() {
        DataSource.getPowerLists(DataSource.POWERDETAILSPRESENTER);
        DataSource.getDailyPowerLists(6);
    }

    @Override
    public void userPressingAddToLists() {
        mPowerDetailsView.showAddToListsFragment();
        //get data from DB
        DataSource.getPowerLists(DataSource.POWERDETAILSPRESENTER);
        DataSource.getDailyPowerLists(DataSource.POWERDETAILSPRESENTER);
    }


    /**
     * Used for constructing a spell object
     * @param powerData ArrayMap containing fields for the Spell to be constructed, handles null values
     * @return Spell object initialized with the data,
     */
    private Spell constructPowerFromFields(ArrayMap<String, String> powerData){

        Spell spell = new Spell();

        if(powerData != null) {
            if (powerData.containsKey(PowerDetailsContract.name))
                spell.setName(powerData.get(PowerDetailsContract.name));
            if (powerData.containsKey(PowerDetailsContract.attackType))
                spell.setAttackType(powerData.get(PowerDetailsContract.attackType));
            if (powerData.containsKey(PowerDetailsContract.recharge))
                spell.setRechargeTime(powerData.get(PowerDetailsContract.recharge));
            if (powerData.containsKey(PowerDetailsContract.castingTime))
                spell.setCastingTime(powerData.get(PowerDetailsContract.castingTime));
            if (powerData.containsKey(PowerDetailsContract.target))
                spell.setTarget(powerData.get(PowerDetailsContract.target));
            if (powerData.containsKey(PowerDetailsContract.attackRoll))
                spell.setAttackRoll(powerData.get(PowerDetailsContract.attackRoll));
            if (powerData.containsKey(PowerDetailsContract.hitDamageOrEffect))
                spell.setHitDamageOrEffect(powerData.get(PowerDetailsContract.hitDamageOrEffect));
            if (powerData.containsKey(PowerDetailsContract.missDamage))
                spell.setMissDamage(powerData.get(PowerDetailsContract.missDamage));
            if (powerData.containsKey(PowerDetailsContract.adventurerFeat))
                spell.setAdventurerFeat(powerData.get(PowerDetailsContract.adventurerFeat));
            if (powerData.containsKey(PowerDetailsContract.championFeat))
                spell.setChampionFeat(powerData.get(PowerDetailsContract.championFeat));
            if (powerData.containsKey(PowerDetailsContract.epicFeat))
                spell.setEpicFeat(powerData.get(PowerDetailsContract.epicFeat));
            if (powerData.containsKey(PowerDetailsContract.groupName))
                spell.setGroupName(powerData.get(PowerDetailsContract.groupName));
            if (powerData.containsKey(PowerDetailsContract.playerNotes))
                spell.setPlayerNotes(powerData.get(PowerDetailsContract.playerNotes));
            if (powerData.containsKey(PowerDetailsContract.trigger))
                spell.setTrigger(powerData.get(PowerDetailsContract.trigger));
        }

        return spell;
    }
}
