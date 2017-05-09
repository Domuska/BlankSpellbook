package tomi.piipposoft.blankspellbook.Utils;

import android.util.Log;

import tomi.piipposoft.blankspellbook.PowerList.SpellGroup;

/**
 * Created by Domu on 12-Jun-16.
 */
public class Spell {

    private String name;
    private String range;
    private String rechargeTime;
    private String target;
    private String attackRoll;
    private String hitDamage;
    private String missDamage;
    private String playerNotes;
    private String groupName;
    private String spellId;
    private String castingTime;



    private String spellListId;


    public String getName() {
        return name;
    }

    public Spell setName(String name) {
        this.name = name;
        return this;
    }

    public String getRange() {
        return range;
    }

    public Spell setRange(String range) {
        this.range = range;
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

    public String getHitDamage() {
        return hitDamage;
    }

    public Spell setHitDamage(String hitDamage) {
        this.hitDamage = hitDamage;
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
