package tomi.piipposoft.blankspellbook.Database;

import java.util.List;

import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by OMISTAJA on 5.5.2017.
 */

public class DailySpellList {
    private String name;
    private List<Spell> spells;

    public DailySpellList(String name) {
        this.name = name;
    }

    public DailySpellList() {
        // Default constructor required for calls to DataSnapshot.getValue(DailySpellList.class)
    }

    public String getName() {
        return name;
    }

    public List<Spell> getSpells() {
        return spells;
    }
}
