package tomi.piipposoft.blankspellbook.Database;

import java.util.List;

import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by OMISTAJA on 5.5.2017.
 */

public class SpellList {

    private String name;
    private List<Spell> spells;

    public SpellList(){
        // Default constructor required for calls to DataSnapshot.getValue(SpellList.class)
    }

    public SpellList(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Spell> getSpells() {
        return spells;
    }

    public void setSpells(List<Spell> spells) {
        this.spells = spells;
    }

}
