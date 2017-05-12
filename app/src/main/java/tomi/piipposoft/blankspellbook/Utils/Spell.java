package tomi.piipposoft.blankspellbook.Utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.PowerList.SpellGroup;

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
