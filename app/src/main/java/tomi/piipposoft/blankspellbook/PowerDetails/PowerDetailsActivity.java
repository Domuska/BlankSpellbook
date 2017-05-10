package tomi.piipposoft.blankspellbook.PowerDetails;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Utils.Spell;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetDailyPowerListNameDialog;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetPowerListNameDialog;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.PowerList.PowerListActivity;

public class PowerDetailsActivity extends AppCompatActivity
    implements DrawerHelper.DrawerListener,
        SetPowerListNameDialog.NoticeDialogListener,
        SetDailyPowerListNameDialog.NoticeDialogListener,
        DrawerContract.ViewActivity,
        PowerDetailsContract.View{

    public static final String EXTRA_POWER_DETAIL_ID = "powerDetailId";
    public static final String EXTRA_ADD_NEW_POWER_DETAILS = "";

    private final String TAG = "PowerDetailsActivity";
    private DrawerHelper mDrawerHelper;
    private DrawerContract.UserActionListener mDrawerActionListener;
    private PowerDetailsContract.UserActionListener mActionListener;

    private String powerId;

    private TextInputLayout spellNameLayout, attackTypeLayout, rechargeLayout, castingTimeLayout,
    targetLayout, attackRollLayout, hitDamageEffectLayout, missDamageLayout, adventurerFeatLayout,
    championFeatLayout, epicFeatLayout, groupLayout, notesLayout, triggerLayout;

    private TextInputEditText spellNameText, attackTypeText, rechargeText, castingTimeText,
    targetText, attackRollText, hitDamageEffectText, missDamageText, adventurerFeatText, championFeatText,
    epicFeatText, groupText, notesText, triggerText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_details);

        powerId = getIntent().getStringExtra(EXTRA_POWER_DETAIL_ID);
        Log.i(TAG, "onCreate: ID extra gotten " + powerId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("Power details activity");
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDrawerHelper = DrawerHelper.getInstance(this, (Toolbar)findViewById(R.id.my_toolbar));
        mActionListener = new PowerDetailsPresenter(
                DataSource.getDatasource(this),
                this,
                DrawerHelper.getInstance(this, (Toolbar)findViewById(R.id.my_toolbar))
        );

        mDrawerActionListener = (DrawerContract.UserActionListener)mActionListener;
        mDrawerActionListener.powerListProfileSelected();
        mActionListener.showPowerDetails(powerId);

;

    }

    // FROM PowerDetailsContract

    @Override
    public void showEmptyForms() {

    }

    @Override
    public void showFilledForms(Spell spell) {

        //wonder if this is the smartest way to go about this?

        if(!spell.getAttackType().equals("")){
            attackTypeLayout = (TextInputLayout)findViewById(R.id.input_layout_attackType);
            attackTypeText = (TextInputEditText)findViewById(R.id.editText_attackType);
            attackTypeLayout.setVisibility(View.VISIBLE);
            attackTypeText.setText(spell.getAttackType());
            attackTypeText.setKeyListener(null);
        }

        if(!spell.getAttackRoll().equals("")){
            attackRollLayout = (TextInputLayout)findViewById(R.id.input_layout_attackRoll);
            attackRollText = (TextInputEditText)findViewById(R.id.editText_attackRoll);
            attackRollLayout.setVisibility(View.VISIBLE);
            attackRollText.setText(spell.getAttackRoll());
            attackRollText.setKeyListener(null);
        }

        if(!spell.getCastingTime().equals("")){
            castingTimeLayout = (TextInputLayout)findViewById(R.id.input_layout_castingTime);
            castingTimeText = (TextInputEditText)findViewById(R.id.editText_castingTime);
            castingTimeLayout.setVisibility(View.VISIBLE);
            castingTimeText.setText(spell.getCastingTime());
            castingTimeText.setKeyListener(null);
        }

        if(!spell.getGroupName().equals("")){
            groupLayout = (TextInputLayout)findViewById(R.id.input_layout_group);
            groupText = (TextInputEditText)findViewById(R.id.editText_group);
            groupLayout.setVisibility(View.VISIBLE);
            groupText.setText(spell.getGroupName());
            groupText.setKeyListener(null);
        }

        if(!spell.getHitDamageOrEffect().equals("")){
            hitDamageEffectLayout = (TextInputLayout)findViewById(R.id.input_layout_damage_effect);
            hitDamageEffectText = (TextInputEditText)findViewById(R.id.editText_damage_effect);
            hitDamageEffectLayout.setVisibility(View.VISIBLE);
            hitDamageEffectText.setText(spell.getHitDamageOrEffect());
            hitDamageEffectText.setKeyListener(null);
        }

        if(!spell.getMissDamage().equals("")){
            missDamageLayout = (TextInputLayout)findViewById(R.id.input_layout_miss_damage);
            missDamageText = (TextInputEditText)findViewById(R.id.editText_miss_damage);
            missDamageLayout.setVisibility(View.VISIBLE);
            missDamageText.setText(spell.getMissDamage());
            missDamageText.setKeyListener(null);
        }

        if(!spell.getName().equals("")){
            spellNameLayout = (TextInputLayout)findViewById(R.id.input_layout_spell_name);
            spellNameText = (TextInputEditText)findViewById(R.id.editText_spellName);
            spellNameLayout.setVisibility(View.VISIBLE);
            spellNameText.setText(spell.getName());
            spellNameText.setKeyListener(null);
        }

        if(!spell.getPlayerNotes().equals("")){
            notesLayout = (TextInputLayout)findViewById(R.id.input_layout_notes);
            notesText = (TextInputEditText)findViewById(R.id.editText_notes);
            notesLayout.setVisibility(View.VISIBLE);
            notesText.setText(spell.getPlayerNotes());
            notesText.setKeyListener(null);
        }

        if(!spell.getRechargeTime().equals("")){
            rechargeLayout = (TextInputLayout)findViewById(R.id.input_layout_recharge);
            rechargeText = (TextInputEditText)findViewById(R.id.editText_recharge);
            rechargeLayout.setVisibility(View.VISIBLE);
            rechargeText.setText(spell.getRechargeTime());
            rechargeText.setKeyListener(null);
        }

        if(!spell.getTarget().equals("")){
            targetLayout = (TextInputLayout)findViewById(R.id.input_layout_target);
            targetText = (TextInputEditText)findViewById(R.id.editText_target);
            targetLayout.setVisibility(View.VISIBLE);
            targetText.setText(spell.getTarget());
            targetText.setKeyListener(null);
        }

        if(!spell.getAdventurerFeat().equals("")){
            adventurerFeatLayout = (TextInputLayout)findViewById(R.id.input_layout_adventurer_feat);
            adventurerFeatText = (TextInputEditText)findViewById(R.id.editText_adventurer_feat);
            adventurerFeatLayout.setVisibility(View.VISIBLE);
            adventurerFeatText.setText(spell.getAdventurerFeat());
            adventurerFeatText.setKeyListener(null);
        }

        if(!spell.getChampionFeat().equals("")){
            championFeatLayout = (TextInputLayout)findViewById(R.id.input_layout_champion_feat);
            championFeatText = (TextInputEditText)findViewById(R.id.editText_champion_feat);
            championFeatLayout.setVisibility(View.VISIBLE);
            championFeatText.setText(spell.getChampionFeat());
            championFeatText.setKeyListener(null);
        }

        if(!spell.getEpicFeat().equals("")){
            epicFeatLayout = (TextInputLayout)findViewById(R.id.input_layout_epic_feat);
            epicFeatText = (TextInputEditText)findViewById(R.id.editText_epic_feat);
            epicFeatLayout.setVisibility(View.VISIBLE);
            epicFeatText.setText(spell.getEpicFeat());
            epicFeatText.setKeyListener(null);
        }

    }


    // FROM DRAWER CONTRACT VIEW INTERFACE

    @Override
    public void dailyPowerListProfileSelected() {
        mDrawerActionListener.dailyPowerListProfileSelected();
    }

    @Override
    public void powerListProfileSelected() {
        mDrawerActionListener.powerListProfileSelected();
    }

    @Override
    public void powerListClicked(IDrawerItem clickedItem) {
        PrimaryDrawerItem item = (PrimaryDrawerItem)clickedItem;
        mDrawerActionListener.powerListItemClicked(
                (String)item.getTag(),
                item.getName().toString());
    }

    @Override
    public void dailyPowerListClicked(IDrawerItem clickedItem) {
        mDrawerActionListener.dailyPowerListItemClicked(clickedItem.getIdentifier());
    }


    // FROM DRAWER CONTRACT VIEW ACTIVITY INTERFACE
    @Override
    public void openPowerList(String powerListId, String name) {

        Intent i = new Intent(this, PowerListActivity.class);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_ID, powerListId);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_NAME, name);
        mDrawerHelper.closeDrawer();
        startActivity(i);
    }

    @Override
    public void openDailyPowerList(Long dailyPowerListId) {
        // do stuff
    }

    // FROM POPUP FRAGMENT INTERFACES

    @Override
    public void onSetPowerListNameDialogPositiveClick(DialogFragment dialog, String powerListName) {
        mDrawerActionListener.addPowerList(powerListName);
    }

    @Override
    public void onSetDailyPowerNameDialogPositiveClick(DialogFragment dialog, String dailyPowerListName) {
        mDrawerActionListener.addDailyPowerList(dailyPowerListName);
    }
}
