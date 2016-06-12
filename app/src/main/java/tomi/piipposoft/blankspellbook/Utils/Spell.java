package tomi.piipposoft.blankspellbook.Utils;

/**
 * Created by Domu on 12-Jun-16.
 */
public class Spell {

    private String name, range, rechargeTime, target, attackRoll,
            hitDamage, missDamage, playerNotes, groupName;
    private long spellId, powerListId;


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

    public long getSpellId() {
        return spellId;
    }

    public Spell setSpellId(long spellId) {
        this.spellId = spellId;
        return this;
    }

    public long getPowerListId() {
        return powerListId;
    }

    public Spell setPowerListId(long powerListId) {
        this.powerListId = powerListId;
        return this;
    }


}
