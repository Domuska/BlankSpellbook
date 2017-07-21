package tomi.piipposoft.blankspellbook.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Domu on 12-Jun-16.
 */
public class Spell {

    //from database schema
    private String attackType;
    private String attackRoll;
    private String castingTime;
    private String groupName;
    private String hitDamageOrEffect;
    private String missDamage;
    private String name;
    private String playerNotes;
    private String rechargeTime;
    private String spellListId;
    private String target;
    private String adventurerFeat;
    private String championFeat;
    private String epicFeat;
    private String trigger;

    //powerListName for MainActivity, should never be saved to database
    private String powerListName;

    private String powerListId;
    private HashMap<String, Boolean> dailyPowerLists;

    //not in database schema, needed still
    private String spellId;

    public String getName() {
        return name;
    }

    public Spell setName(String name) {
        this.name = name;
        return this;
    }

    public String getRechargeTime() {
        return rechargeTime;
    }

    public Spell setRechargeTime(String rechargeTime) {
        this.rechargeTime = rechargeTime;
        return this;
    }

    public String getTarget() {
        return target;
    }

    public Spell setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getAttackRoll() {
        return attackRoll;
    }

    public Spell setAttackRoll(String attackRoll) {
        this.attackRoll = attackRoll;
        return this;
    }


    public String getMissDamage() {
        return missDamage;
    }

    public Spell setMissDamage(String missDamage) {
        this.missDamage = missDamage;
        return this;
    }

    public String getPlayerNotes() {
        return playerNotes;
    }

    public Spell setPlayerNotes(String playerNotes) {
        this.playerNotes = playerNotes;
        return this;
    }

    public String getGroupName() {
        return groupName;
    }

    public Spell setGroupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public String getSpellId() {
        return spellId;
    }

    public Spell setSpellId(String spellId) {
        this.spellId = spellId;
        return this;
    }

    public String getSpellListId() {
        return spellListId;
    }

    public Spell setSpellListId(String spellListId) {
        this.spellListId = spellListId;
        return this;
    }

    public String getCastingTime() {
        return castingTime;
    }

    public Spell setCastingTime(String castingTime) {
        this.castingTime = castingTime;
        return this;
    }

    public String getAttackType() {
        return attackType;
    }

    public Spell setAttackType(String attackType) {
        this.attackType = attackType;
        return this;
    }

    public String getHitDamageOrEffect() {
        return hitDamageOrEffect;
    }

    public Spell setHitDamageOrEffect(String hitDamageOrEffect) {
        this.hitDamageOrEffect = hitDamageOrEffect;
        return this;
    }

    public String getAdventurerFeat() {
        return adventurerFeat;
    }

    public Spell setAdventurerFeat(String adventurerFeat) {
        this.adventurerFeat = adventurerFeat;
        return this;
    }

    public String getChampionFeat() {
        return championFeat;
    }

    public Spell setChampionFeat(String championFeat) {
        this.championFeat = championFeat;
        return this;
    }

    public String getEpicFeat() {
        return epicFeat;
    }

    public Spell setEpicFeat(String epicFeat) {
        this.epicFeat = epicFeat;
        return this;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getPowerListId() {
        return powerListId;
    }

    public void setPowerListId(String powerListId) {
        this.powerListId = powerListId;
    }

    public HashMap<String, Boolean> getDailyPowerLists() {
        return dailyPowerLists;
    }

    public void setDailyPowerLists(HashMap<String, Boolean> dailypowerLists) {
        this.dailyPowerLists = dailypowerLists;
    }

    public String getPowerListName() {
        return powerListName;
    }

    public void setPowerListName(String powerListName) {
        this.powerListName = powerListName;
    }

    /**
     * Returns all the fields (shown in UI, ID not included) held by the object, order of fields is not guaranteed
     * @return array of strings containing the fields of the POJO
     */
    private String[] getVisibleFields(){
        if (this.getAttackType() == null)
            this.setAttackType("");
        if (this.getAttackRoll() == null)
            this.setAttackRoll("");
        if (this.getCastingTime() == null)
            this.setCastingTime("");
        if (this.getGroupName() == null)
            this.setGroupName("");
        if (this.getHitDamageOrEffect() == null)
            this.setHitDamageOrEffect("");
        if (this.getMissDamage() == null)
            this.setMissDamage("");
        if (this.getName() == null)
            this.setName("");
        if (this.getPlayerNotes() == null)
            this.setPlayerNotes("");
        if (this.getRechargeTime() == null)
            this.setRechargeTime("");
        if (this.getTarget() == null)
            this.setTarget("");
        if (this.getAdventurerFeat() == null)
            this.setAdventurerFeat("");
        if (this.getChampionFeat() == null)
            this.setChampionFeat("");
        if (this.getEpicFeat() == null)
            this.setEpicFeat("");
        if (this.getTrigger() == null)
            this.setTrigger("");
        return new String[]{attackType, attackRoll,
                castingTime, groupName, hitDamageOrEffect, missDamage, name, playerNotes,
                rechargeTime, target, adventurerFeat, championFeat, epicFeat, trigger};
    }

    @Override
    public boolean equals(Object o) {
        boolean areEqual = false;
        if(o != null && o instanceof Spell) {
            String myFields[] = this.getVisibleFields();
            Spell spell = (Spell) o;
            String objectFields[] = spell.getVisibleFields();
            if(Arrays.equals(myFields, objectFields))
               areEqual = true;
        }
        return areEqual;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
