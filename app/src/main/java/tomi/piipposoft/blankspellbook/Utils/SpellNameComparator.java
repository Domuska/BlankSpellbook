package tomi.piipposoft.blankspellbook.Utils;

import java.util.Comparator;

/**
 * Created by OMISTAJA on 11.8.2017.
 *
 * Custom comparator for the Spell class, used for TreeSet when sorting the spells to
 * alphabetical order
 */

public class SpellNameComparator implements Comparator<Spell> {

    @Override
    public int compare(Spell spell, Spell compareTo) {
        return spell.getName().compareTo(compareTo.getName());
    }
}
