package tomi.piipposoft.blankspellbook.PowerDetails;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.ApplicationActivity;
import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Utils.SharedPreferencesHandler;
import tomi.piipposoft.blankspellbook.Utils.Spell;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;

public class PowerDetailsActivity extends ApplicationActivity
    implements
        AddToPowerListDialog.NoticeDialogListener,
        ConfirmDeletionDialog.ConfirmDeletionListener,
        PowerDetailsContract.View{

    public static final String EXTRA_POWER_DETAIL_ID = "powerDetailId";
    public static final String EXTRA_POWER_DETAIL_NAME = "powerDetailName";
    public static final String EXTRA_ADD_NEW_POWER_DETAILS = "";
    public static final String EXTRA_POWER_LIST_ID = "powerListId";


    private final String TAG = "PowerDetailsActivity";
    private final int MENU_ITEM_CANCEL = 1;
    private PowerDetailsContract.UserActionListener mActionListener;

    private String powerId, spellBookId;

    private boolean goBackOnCancelPress = false;
    private boolean editingSpell = false;

    private TextInputLayout spellNameLayout, attackTypeLayout, rechargeLayout, castingTimeLayout,
    targetLayout, attackRollLayout, hitDamageEffectLayout, missDamageLayout, adventurerFeatLayout,
    championFeatLayout, epicFeatLayout, groupLayout, notesLayout, triggerLayout;

    private TextInputEditText spellNameText, attackTypeText, rechargeText, castingTimeText,
    targetText, attackRollText, hitDamageEffectText, missDamageText, adventurerFeatText, championFeatText,
    epicFeatText, notesText, triggerText;

    private AutoCompleteTextView groupText;

    private FloatingActionButton fab, fabCancel;

    private AddToPowerListDialog addToPowerListDialogFragment;

    private Bundle savedState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_details);

        if(!SharedPreferencesHandler.isDatabasePersistanceSet(this)){
            DataSource.setDatabasePersistance();
            SharedPreferencesHandler.setDatabasePersistance(true, this);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle(getString(R.string.title_power_details));
        setSupportActionBar(toolbar);
        savedState = savedInstanceState;

        //add cancel button to toolbar
        Log.d(TAG, "onCreate called");
    }

    @Override
    protected void onResume() {
        super.onResume();

        powerId = getIntent().getStringExtra(EXTRA_POWER_DETAIL_ID);
        Log.i(TAG, "onResume: power ID extra got: " + powerId);
        String powerListId = getIntent().getStringExtra(EXTRA_POWER_LIST_ID);
        Log.d(TAG, "onResume: power list id got: " + powerListId);
        String powerName = getIntent().getStringExtra(EXTRA_POWER_DETAIL_NAME);

        this.drawerHelper = DrawerHelper.getInstance(this, (Toolbar)findViewById(R.id.my_toolbar));
        mActionListener = new PowerDetailsPresenter(
                DataSource.getDatasource(this),
                this,
                this.drawerHelper,
                powerId,
                powerListId
        );

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fabCancel = (FloatingActionButton)findViewById(R.id.fabLeft);

        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListener.userPressingCancelButton(constructDataFromFields());
            }
        });

        this.drawerActionListener = (DrawerContract.UserActionListener)mActionListener;
        this.drawerActionListener.powerListProfileSelected();

        if(savedState == null)
            mActionListener.showPowerDetails(false);
        else
            mActionListener.showPowerDetails(savedState.getBoolean("userEditingPower"));

        //set the power name
        spellNameText = (TextInputEditText)findViewById(R.id.editText_spellName);
        spellNameText.setFocusable(false);
        spellNameText.setText(powerName);
        spellNameText.setFocusable(true);

        //check if there is addToPowerList fragment visible, if so let presenter handle this
        Fragment prev = getSupportFragmentManager().findFragmentByTag("addToPowerListDialog");
        if(prev != null){
            Log.d(TAG, "we have addToPowerListDialog fragment in onResume");
            addToPowerListDialogFragment = (AddToPowerListDialog) prev;
            mActionListener.activityResumingWithFragment();
        }
        Log.d(TAG, "onResume called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_power_details, menu);

        //tell action listener to tell us what to show
        //mActionListener.showPowerDetails();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_add_to_powerlist:
                Log.d(TAG, "onOptionsItemSelected: pressed the add to power list button!");
                mActionListener.userPressingAddToLists();
                return true;
            case R.id.action_delete_power:
                Log.d(TAG, "onOptionsItemSelected: deleting power from DB");
                showConfirmDeleteDialog();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && editingSpell) {
            mActionListener.userPressingCancelButton(constructDataFromFields());
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //save if user is editing a power
        outState.putBoolean("userEditingPower", editingSpell);
        super.onSaveInstanceState(outState);
    }

    private void showConfirmDeleteDialog() {
        DialogFragment dialog = new ConfirmDeletionDialog();
        dialog.show(getSupportFragmentManager(), "ConfirmDeletionDialog");
    }

    private ArrayMap<String, String> constructDataFromFields(){
        ArrayMap<String, String> map = new ArrayMap<>();

        spellNameText = (TextInputEditText)findViewById(R.id.editText_spellName);
        if(fieldHasText(spellNameText))
            map.put(PowerDetailsContract.name, spellNameText.getText().toString());
        if(fieldHasText(attackTypeText))
            map.put(PowerDetailsContract.attackType, attackTypeText.getText().toString());
        if(fieldHasText(rechargeText))
            map.put(PowerDetailsContract.recharge, rechargeText.getText().toString());
        if(fieldHasText(castingTimeText))
            map.put(PowerDetailsContract.castingTime, castingTimeText.getText().toString());
        if(fieldHasText(targetText))
            map.put(PowerDetailsContract.target, targetText.getText().toString());
        if(fieldHasText(attackRollText))
            map.put(PowerDetailsContract.attackRoll, attackRollText.getText().toString());
        if(fieldHasText(hitDamageEffectText))
            map.put(PowerDetailsContract.hitDamageOrEffect, hitDamageEffectText.getText().toString());
        if(fieldHasText(missDamageText))
            map.put(PowerDetailsContract.missDamage, missDamageText.getText().toString());
        if(fieldHasText(adventurerFeatText))
            map.put(PowerDetailsContract.adventurerFeat, adventurerFeatText.getText().toString());
        if(fieldHasText(championFeatText))
            map.put(PowerDetailsContract.championFeat, championFeatText.getText().toString());
        if(fieldHasText(epicFeatText))
            map.put(PowerDetailsContract.epicFeat, epicFeatText.getText().toString());
        if(fieldHasText(groupText))
            map.put(PowerDetailsContract.groupName, groupText.getText().toString());
        if(fieldHasText(notesText))
            map.put(PowerDetailsContract.playerNotes, notesText.getText().toString());
        if(fieldHasText(triggerText))
            map.put(PowerDetailsContract.trigger, triggerText.getText().toString());

        return map;
    }

    /**
     * Check if a textView has text
     * @param text TextInputEditText object
     * @return boolean whether text field has string besides empty string
     */
    private boolean fieldHasText(EditText text){
        return text != null && !"".equals(text.getText().toString());
    }

    /**
     *  Called when user has indicated they want to discard the edits they have made in the edit view,
     *  after this we should be back in the view where spell's fields are displayed in non-edit mode
     */
    private void handleCancelButton(){
        editingSpell = false;
        if(goBackOnCancelPress)
            PowerDetailsActivity.this.finish();
        else
            mActionListener.userCancelingEdits();
    }


    ////// FROM PowerDetailsContract.View

    @Override
    public void showAddToListsFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("addToPowerListDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        addToPowerListDialogFragment = AddToPowerListDialog.newInstance();
        addToPowerListDialogFragment.show(ft, "addToPowerListDialog");
    }

    @Override
    public void addPowerListsToFragment(String[] powerListNames, String[] powerListIds) {
        addToPowerListDialogFragment.setPowerListData(powerListNames, powerListIds);
    }

    @Override
    public void addDailyPowerListsToFragment(String[] dailyPowerListNames, String[] dailyPowerListIds) {
        addToPowerListDialogFragment.setDailyPowerListData(dailyPowerListNames, dailyPowerListIds);
    }

    @Override
    public void showDiscardChangesDialog(){
        new AlertDialog.Builder(PowerDetailsActivity.this)
                .setMessage(getString(R.string.spell_details_discard_changes))
                .setPositiveButton(getString(R.string.action_yes)
                        , new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handleCancelButton();
                            }
                        })
                .setNegativeButton(getString(R.string.action_no), null)
                .show();
    }

    @Override
    public void setCancelAsGoBack(boolean b) {
        goBackOnCancelPress = b;
    }

    @Override
    public void cancelEdits() {
        handleCancelButton();
    }

    @Override
    public void showEmptyFields() {

        editingSpell = true;
        Log.d(TAG, "showing empty fields...");
        findViewById(R.id.input_layout_attackType).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_attackRoll).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_castingTime).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_group).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_damage_effect).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_miss_damage).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_spell_name).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_notes).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_recharge).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_target).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_adventurer_feat).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_champion_feat).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_epic_feat).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_trigger).setVisibility(View.VISIBLE);


        triggerText = (TextInputEditText)findViewById(R.id.editText_trigger);
        epicFeatText = (TextInputEditText)findViewById(R.id.editText_epic_feat);
        championFeatText = (TextInputEditText)findViewById(R.id.editText_champion_feat);
        adventurerFeatText = (TextInputEditText)findViewById(R.id.editText_adventurer_feat);
        targetText = (TextInputEditText)findViewById(R.id.editText_target);
        rechargeText = (TextInputEditText)findViewById(R.id.editText_recharge);
        notesText = (TextInputEditText)findViewById(R.id.editText_notes);
        spellNameText = (TextInputEditText)findViewById(R.id.editText_spellName);
        missDamageText = (TextInputEditText)findViewById(R.id.editText_miss_damage);
        hitDamageEffectText = (TextInputEditText)findViewById(R.id.editText_hitDamage_effect);
        groupText = (AutoCompleteTextView) findViewById(R.id.editText_group);
        castingTimeText = (TextInputEditText)findViewById(R.id.editText_castingTime);
        attackRollText = (TextInputEditText)findViewById(R.id.editText_attackRoll);
        attackTypeText = (TextInputEditText)findViewById(R.id.editText_attackType);

        fab.setImageResource(R.drawable.ic_done_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.userSavingPower(constructDataFromFields());
                editingSpell = false;
            }
        });

        fab.setVisibility(View.VISIBLE);
        fabCancel.setVisibility(View.VISIBLE);

        //spellNameText.clearFocus();
        View currentFocus = getCurrentFocus();
        if(currentFocus != null){
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    @Override
    public void showFilledFields(final Spell spell) {

        Log.d(TAG, "starting method call showFilledFields");

        fab.setImageResource(R.drawable.ic_mode_edit_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 11.5.2017 some animation here to show that we are editing spell
                mActionListener.userEditingPower(spell);
            }
        });
        fab.setVisibility(View.VISIBLE);

        //cancelItem.setVisible(false);
        fabCancel.setVisibility(View.GONE);

        // All text fields are invisible if there is not data for them
        // this should work nicer than hiding ones that don't have data,
        // network call might take a while to arrive and it would cause UI to jump around

        //wonder if this is the smartest way to go about this? would some switchcase work better?
        if(!spell.getAttackType().equals("")){
            attackTypeLayout = (TextInputLayout)findViewById(R.id.input_layout_attackType);
            attackTypeText = (TextInputEditText)findViewById(R.id.editText_attackType);
            attackTypeLayout.setVisibility(View.VISIBLE);
            attackTypeText.setText(spell.getAttackType());
            //set keylistener as null so we have elements that can't be edited, this is re-set in edit view
            attackTypeText.setKeyListener(null);
            Log.d(TAG, "spell attack type: " + spell.getAttackType());
        }

        if(!spell.getAttackRoll().equals("")){
            attackRollLayout = (TextInputLayout)findViewById(R.id.input_layout_attackRoll);
            attackRollText = (TextInputEditText)findViewById(R.id.editText_attackRoll);
            attackRollLayout.setVisibility(View.VISIBLE);
            attackRollText.setText(spell.getAttackRoll());
            attackRollText.setKeyListener(null);
            Log.d(TAG, "spell attack roll: " + spell.getAttackRoll());
        }

        if(!"".equals(spell.getCastingTime())){
            castingTimeLayout = (TextInputLayout)findViewById(R.id.input_layout_castingTime);
            castingTimeText = (TextInputEditText)findViewById(R.id.editText_castingTime);
            castingTimeLayout.setVisibility(View.VISIBLE);
            castingTimeText.setText(spell.getCastingTime());
            castingTimeText.setKeyListener(null);
        }

        if(!spell.getGroupName().equals("")){
            groupLayout = (TextInputLayout)findViewById(R.id.input_layout_group);
            groupText = (AutoCompleteTextView) findViewById(R.id.editText_group);
            groupLayout.setVisibility(View.VISIBLE);
            groupText.setText(spell.getGroupName());
            groupText.setKeyListener(null);
            //hide the dropdown menu, if user has focus on it it will pop visible
            groupText.dismissDropDown();
        }

        if(!spell.getHitDamageOrEffect().equals("")){
            hitDamageEffectLayout = (TextInputLayout)findViewById(R.id.input_layout_damage_effect);
            hitDamageEffectText = (TextInputEditText)findViewById(R.id.editText_hitDamage_effect);
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
            //spellNameText.setFocusable(false);
            spellNameText.setText(spell.getName());
            spellNameText.setKeyListener(null);
            //spellNameText.setFocusable(true);
            //InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            //imm.hideSoftInputFromWindow(spellNameText.getWindowToken(), 0);

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

        if(!spell.getTrigger().equals("")){
            triggerLayout = (TextInputLayout)findViewById(R.id.input_layout_trigger);
            triggerText = (TextInputEditText)findViewById(R.id.editText_trigger);
            triggerLayout.setVisibility(View.VISIBLE);
            triggerText.setText(spell.getTrigger());
            triggerText.setKeyListener(null);
        }
    }

    @Override
    public void showSpellEditView(Spell spell) {

        editingSpell = true;
        fab.setImageResource(R.drawable.ic_done_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.userSavingModifiedPower(constructDataFromFields());
                editingSpell = false;
            }
        });
        fab.setVisibility(View.VISIBLE);

        //cancelItem.setVisible(true);
        fabCancel.setVisibility(View.VISIBLE);

        //set all field layouts as visible
        findViewById(R.id.input_layout_attackType).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_attackRoll).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_castingTime).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_group).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_damage_effect).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_miss_damage).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_spell_name).setVisibility(View.VISIBLE);
        //findViewById(R.id.input_layout_spell_name).setClickable(true);
        findViewById(R.id.input_layout_notes).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_recharge).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_target).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_adventurer_feat).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_champion_feat).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_epic_feat).setVisibility(View.VISIBLE);
        findViewById(R.id.input_layout_trigger).setVisibility(View.VISIBLE);

        //set text fields as editable, save variables since constructDataFromFields needs them to be initialized
        KeyListener newListener = new TextInputEditText(getApplicationContext()).getKeyListener();
        spellNameText = (TextInputEditText)findViewById(R.id.editText_spellName);
        spellNameText.setKeyListener(newListener);
        attackTypeText = (TextInputEditText)findViewById(R.id.editText_attackType);
        attackTypeText.setKeyListener(newListener);
        rechargeText = (TextInputEditText)findViewById(R.id.editText_recharge);
        rechargeText.setKeyListener(newListener);
        castingTimeText = (TextInputEditText)findViewById(R.id.editText_castingTime);
        castingTimeText.setKeyListener(newListener);
        targetText = (TextInputEditText)findViewById(R.id.editText_target);
        targetText.setKeyListener(newListener);
        attackRollText = (TextInputEditText)findViewById(R.id.editText_attackRoll);
        attackRollText.setKeyListener(newListener);
        hitDamageEffectText = (TextInputEditText)findViewById(R.id.editText_hitDamage_effect);
        hitDamageEffectText.setKeyListener(newListener);
        missDamageText = (TextInputEditText)findViewById(R.id.editText_miss_damage);
        missDamageText.setKeyListener(newListener);
        adventurerFeatText = (TextInputEditText)findViewById(R.id.editText_adventurer_feat);
        adventurerFeatText.setKeyListener(newListener);
        championFeatText = (TextInputEditText)findViewById(R.id.editText_champion_feat);
        championFeatText.setKeyListener(newListener);
        epicFeatText = (TextInputEditText)findViewById(R.id.editText_epic_feat);
        epicFeatText.setKeyListener(newListener);
        groupText = (AutoCompleteTextView) findViewById(R.id.editText_group);
        groupText.setKeyListener(newListener);
        notesText = (TextInputEditText)findViewById(R.id.editText_notes);
        notesText.setKeyListener(newListener);
        triggerText = (TextInputEditText)findViewById(R.id.editText_trigger);
        triggerText.setKeyListener(newListener);

    }

    @Override
    public void hideUnUsedFields(Spell spell) {

        if(spell.getAttackType() == null || "".equals(spell.getAttackType())) {
            findViewById(R.id.input_layout_attackType).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_attackType)).setText("");
        }
        if(spell.getAttackRoll() == null || "".equals(spell.getAttackRoll())) {
            findViewById(R.id.input_layout_attackRoll).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_attackRoll)).setText("");
        }
        if(spell.getCastingTime() == null || "".equals(spell.getCastingTime())) {
            findViewById(R.id.input_layout_castingTime).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_castingTime)).setText("");
        }
        if(spell.getGroupName() == null || "".equals(spell.getGroupName())) {
            findViewById(R.id.input_layout_group).setVisibility(View.GONE);
            ((AutoCompleteTextView)findViewById(R.id.editText_group)).setText("");
        }
        if(spell.getHitDamageOrEffect() == null || "".equals(spell.getHitDamageOrEffect())) {
            findViewById(R.id.input_layout_damage_effect).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_hitDamage_effect)).setText("");
        }
        if(spell.getMissDamage() == null || "".equals(spell.getMissDamage())) {
            findViewById(R.id.input_layout_miss_damage).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_miss_damage)).setText("");
        }
        if(spell.getName() == null || "".equals(spell.getName())) {
            findViewById(R.id.input_layout_spell_name).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_spellName)).setText("");
        }
        if(spell.getPlayerNotes() == null || "".equals(spell.getPlayerNotes())) {
            findViewById(R.id.input_layout_notes).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_notes)).setText("");
        }
        if(spell.getRechargeTime() == null || "".equals(spell.getRechargeTime())) {
            findViewById(R.id.input_layout_recharge).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_recharge)).setText("");
        }
        if(spell.getTarget() == null || "".equals(spell.getTarget())) {
            findViewById(R.id.input_layout_target).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_target)).setText("");
        }
        if(spell.getAdventurerFeat() == null || "".equals(spell.getAdventurerFeat())) {
            findViewById(R.id.input_layout_adventurer_feat).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_adventurer_feat)).setText("");
        }
        if(spell.getChampionFeat() == null || "".equals(spell.getChampionFeat())) {
            findViewById(R.id.input_layout_champion_feat).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_champion_feat)).setText("");
        }
        if(spell.getEpicFeat() == null || "".equals(spell.getEpicFeat())) {
            findViewById(R.id.input_layout_epic_feat).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_epic_feat)).setText("");
        }
        if(spell.getTrigger() == null || "".equals(spell.getTrigger())) {
            findViewById(R.id.input_layout_trigger).setVisibility(View.GONE);
            ((TextInputEditText)findViewById(R.id.editText_trigger)).setText("");
        }
    }

    @Override
    public void showErrorSavingEmptyFields() {
        Snackbar.make(
            findViewById(R.id.coordinatorLayout),
            R.string.error_empty_fields,
            Snackbar.LENGTH_SHORT)
        .show();
    }

    @Override
    public void populatePowerGroupSuggestions(String[] powerGroups) {
        //create the adapter with the group names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                powerGroups);

        if(groupText == null)
            groupText = (AutoCompleteTextView) findViewById(R.id.editText_group);
        //set adapter for the text view
        groupText.setAdapter(adapter);
    }


    // FROM ADDTOPOWERLIST FRAGMENT INTERFACE

    @Override
    public void onAddToListPositiveClick(DialogFragment dialog, ArrayList<String> listIds, boolean addingToPowerLists) {
        for(int i = 0; i < listIds.size(); i++){
            Log.d(TAG, "onAddToListPositiveClick: selected item id: " + listIds.get(i));
        }

        if(listIds.size() != 0){
            mActionListener.userCopyingPowerToLists(listIds, addingToPowerLists);
        }
        //Toast.makeText(this, getString(R.string.toast_powers_copied), Toast.LENGTH_SHORT).show();
        Snackbar.make(findViewById(R.id.coordinatorLayout),
                getString(R.string.toast_powers_copied),
                Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.action_undo),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mActionListener.userPushingUndo();
                            }
                        })
                .show();
    }

    // From ConfirmDeletionDialog.ConfirmDeletionListener
    @Override
    public void onPositiveClick() {
        Log.d(TAG, "onPositiveClick: deleting power...");
        mActionListener.userPressingDeletePower();
    }
}
