package tomi.piipposoft.blankspellbook.PowerList;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.Utils.Spell;



/**
 * Created by Domu on 12-Jun-16.
 */
public class SpellGroup implements ParentListItem, Comparable<SpellGroup>{

    private List<Spell> spellsList;
    private String groupName;
    private final String TAG = "SpellGroup";

    public SpellGroup(String groupName, List<Spell> spell){
        this.groupName = groupName;
        spellsList = spell;
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

    public int removeSpell(Spell spell){
        int spellIndex = spellsList.indexOf(spell);
        spellsList.remove(spell);
        return spellIndex;
    }

    boolean containsSpell(Spell power){
        return spellsList.contains(power);
    }

    public int getListSize(){
        return this.spellsList.size();
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

        if(o!= null && o instanceof String){
            Log.d(TAG, "comparing with a String: " + o);
            return o.equals(this.groupName);
        }
        if(o!= null && o instanceof SpellGroup) {
            SpellGroup group = (SpellGroup) o;
            if(group.getGroupName() != null)
                return group.getGroupName().equals(this.groupName);
        }
        return false;
    }



    @Override
    public int hashCode() {
        return super.hashCode();
    }

    //for sorting the spell groups by their name
    @Override
    public int compareTo(@NonNull SpellGroup spellGroup) {
        return groupName.compareToIgnoreCase(spellGroup.getGroupName());
    }
}

