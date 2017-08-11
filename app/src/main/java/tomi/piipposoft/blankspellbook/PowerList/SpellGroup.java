package tomi.piipposoft.blankspellbook.PowerList;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import tomi.piipposoft.blankspellbook.Utils.Spell;
import tomi.piipposoft.blankspellbook.Utils.SpellNameComparator;


/**
 * Created by Domu on 12-Jun-16.
 */
public class SpellGroup implements ParentListItem, Comparable<SpellGroup>{

    private TreeSet<Spell> spellsList;
    private String groupName;
    private final String TAG = "SpellGroup";

    public SpellGroup(String groupName, List<Spell> spells){
        this.groupName = groupName;
        spellsList = new TreeSet<>(new SpellNameComparator());
        spellsList.addAll(spells);
    }

    SpellGroup(String groupName, Spell spell){
        this.groupName = groupName;
        spellsList = new TreeSet<>(new SpellNameComparator());
        spellsList.add(spell);
    }

    public String getGroupName() {
        return groupName;
    }

    /**
     * Add a new spell to this spell group
     * @param newSpell a spell that is to be added
     * @return the index where the spell was added
     */
    int addSpell(Spell newSpell){
        spellsList.add(newSpell);
        return spellsList.contains(newSpell)? spellsList.headSet(newSpell).size(): -1;
    }

    /**
     * remove a spell from the group
     * @param spell the spell to be removed
     * @return the index where the spell was
     */
    int removeSpell(Spell spell){
        //convoluted way of getting index of the spell, see here
        //https://stackoverflow.com/questions/7911621/how-to-find-the-index-of-an-element-in-a-treeset
        int spellIndex = spellsList.contains(spell)? spellsList.headSet(spell).size(): -1;
        spellsList.remove(spell);
        return spellIndex;
    }

    boolean containsSpell(Spell power){
        return spellsList.contains(power);
    }

    int getListSize(){
        return this.spellsList.size();
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    @Override
    public List<?> getChildItemList() {
        return new ArrayList<>(spellsList);
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

