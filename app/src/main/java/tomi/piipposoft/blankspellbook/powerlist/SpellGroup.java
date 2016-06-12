package tomi.piipposoft.blankspellbook.powerlist;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

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

    public String getGroupName() {
        return groupName;
    }


    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    @Override
    public List<?> getChildItemList() {
        return spellsList;
    }
}

