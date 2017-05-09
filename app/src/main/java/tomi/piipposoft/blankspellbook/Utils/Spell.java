package tomi.piipposoft.blankspellbook.Utils;

import android.util.Log;

import tomi.piipposoft.blankspellbook.PowerList.SpellGroup;

/**
 * Created by Domu on 12-Jun-16.
 */
public class Spell {

    //from database schema
    private String attackType, attackRoll, castingTime, effect, groupName, hitDamageFirstLevel,
    hitDamageThirdLevel, hitDamageFifthLevel, hitDamageSeventhLevel, hitDamageNinthLevel, missDamage,
    name, playerNotes, rechargeTime, spellListId, target, adventurerFeat, championFeat, epicFeat;

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

    public void setSpellListId(String spellListId) {
        this.spellListId = spellListId;
    }

    public String getCastingTime() {
        return castingTime;
    }

    public void setCastingTime(String castingTime) {
        this.castingTime = castingTime;
    }


    public String getAttackType() {
        return attackType;
    }

    public void setAttackType(String attackType) {
        this.attackType = attackType;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getHitDamageFirstLevel() {
        return hitDamageFirstLevel;
    }

    public void setHitDamageFirstLevel(String hitDamageFirstLevel) {
        this.hitDamageFirstLevel = hitDamageFirstLevel;
    }

    public String getHitDamageThirdLevel() {
        return hitDamageThirdLevel;
    }

    public void setHitDamageThirdLevel(String hitDamageThirdLevel) {
        this.hitDamageThirdLevel = hitDamageThirdLevel;
    }

    public String getHitDamageFifthLevel() {
        return hitDamageFifthLevel;
    }

    public void setHitDamageFifthLevel(String hitDamageFifthLevel) {
        this.hitDamageFifthLevel = hitDamageFifthLevel;
    }

    public String getHitDamageSeventhLevel() {
        return hitDamageSeventhLevel;
    }

    public void setHitDamageSeventhLevel(String hitDamageSeventhLevel) {
        this.hitDamageSeventhLevel = hitDamageSeventhLevel;
    }

    public String getHitDamageNinthLevel() {
        return hitDamageNinthLevel;
    }

    public void setHitDamageNinthLevel(String hitDamageNinthLevel) {
        this.hitDamageNinthLevel = hitDamageNinthLevel;
    }

    public String getAdventurerFeat() {
        return adventurerFeat;
    }

    public void setAdventurerFeat(String adventurerFeat) {
        this.adventurerFeat = adventurerFeat;
    }

    public String getChampionFeat() {
        return championFeat;
    }

    public void setChampionFeat(String championFeat) {
        this.championFeat = championFeat;
    }

    public String getEpicFeat() {
        return epicFeat;
    }

    public void setEpicFeat(String epicFeat) {
        this.epicFeat = epicFeat;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Spell) {
            Spell spell = (Spell) o;
            return spell.getSpellId().equals(this.spellId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
