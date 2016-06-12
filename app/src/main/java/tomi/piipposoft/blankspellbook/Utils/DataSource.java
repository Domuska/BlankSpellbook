package tomi.piipposoft.blankspellbook.Utils;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;

/**
 * Created by Domu on 17-Apr-16.
 */
public class DataSource {

    public static BlankSpellBookContract.DBHelper getDatasource(Activity activity){
        return new BlankSpellBookContract.DBHelper(activity.getApplicationContext());
    }

    public static ArrayList<Spell> getSpellsWithSpellBookId(long id){

        Spell spell = new Spell();

        spell
                .setName("Abi zalim's horrid wilting")
                .setHitDamage("1d10+CHA")
                .setAttackRoll("level + INT")
                .setGroupName("level 8");

        ArrayList<Spell> data = new ArrayList<>();
        data.add(spell);

        spell = new Spell();
        spell.setName("fireball")
                .setGroupName("level 8");
        data.add(spell);

        spell = new Spell();
        spell.setName("frost ray")
                .setGroupName("lvl 1");
        data.add(spell);

        for(int i = 0; i < 10; i++) {
            spell = new Spell();
            spell.setName("sepon säde").setGroupName("lvl 1");
            data.add(spell);
        }

        return data;
    }



}
