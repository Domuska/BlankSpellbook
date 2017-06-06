import org.junit.Test;

import tomi.piipposoft.blankspellbook.Utils.Spell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by OMISTAJA on 6.6.2017.
 */

public class SpellTest {

    @Test
    public void SpellEquals_CompareSpellWithSomeEmptyFields_ReturnTrue(){
        Spell spell = new Spell()
                .setName("saf")
                .setGroupName("dsjf")
                .setAdventurerFeat("sduf")
                .setAttackRoll("sfd");

        Spell spell2 = new Spell()
                .setName("saf")
                .setGroupName("dsjf")
                .setAdventurerFeat("sduf")
                .setAttackRoll("sfd");

        assertTrue(spell.equals(spell2));
    }

}
