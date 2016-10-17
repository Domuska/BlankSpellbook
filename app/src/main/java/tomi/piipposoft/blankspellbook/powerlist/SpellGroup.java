package tomi.piipposoft.blankspellbook.powerlist;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.Utils.Spell;



/**
 * Created by Domu on 12-Jun-16.
 */
public class SpellGroup implements ParentListItem{

    private List<Spell> spellsList;
    private String groupName;

    public SpellGroup(String groupName, List<Spell> spells){
        this.groupName = groupName;
        spellsList = spells;
    }

    public SpellGroup(String groupName, Spell spell){
        this.groupName = groupName;
        spellsList = new ArrayList<>();
        spellsList.add(spell);
    }

    public String getGroupName() {
        return groupName;
    }

    public void addSpell(Spell newSpell){
        spellsList.add(newSpell);
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    @Override
    public List<?> getChildItemList() {
        return spellsList;
    }

    @Override
    public boolean equals(Object o) {

        if(o instanceof String){
            return o.equals(this.groupName);
        }
        if(o instanceof SpellGroup) {
            SpellGroup group = (SpellGroup) o;
            return group.getGroupName().equals(this.groupName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

