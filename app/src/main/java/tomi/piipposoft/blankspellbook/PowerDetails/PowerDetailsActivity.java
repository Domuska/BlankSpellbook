package tomi.piipposoft.blankspellbook.PowerDetails;

import android.content.Context;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ViewStubCompat;
import android.text.method.KeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.ApplicationActivity;
import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Utils.Helper;
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
    public static final String EXTRA_POWER_LIST_NAME = "powerListName";

    private final float FAB_CANCEL_ANIMATION_SLIDE_IN_DP_PER_SECOND = 50;
    private final float FAB_CANCEL_FINAL_POSITION_IN_SCREEN_DP = 128;
    private final float FAB_CANCEL_FINAL_POSITION_OUTSIDE_SCREEN_DP = 0;
    private float fabCancelPositionInScreen, fabCancelPositionOutsideScreen, pixelPerSecondSlideFab;

    private final String TAG = "PowerDetailsActivity";
    private final int MENU_ITEM_CANCEL = 1;
    private PowerDetailsContract.UserActionListener mActionListener;

    private String powerId, spellBookId;

    private boolean goBackOnCancelPress = false;
    private boolean editingSpell = false;

    private TextInputLayout spellNameLayout, attackTypeLayout, rechargeLayout, castingTimeLayout,
    targetLayout, attackRollLayout, hitDamageEffectLayout, missDamageLayout, adventurerFeatLayout,
    championFeatLayout, epicFeatLayout, groupLayout, notesLayout, triggerLayout;

    private TextInputEditText spellNameTextEdit, attackTypeTextEdit, rechargeTextEdit, castingTimeTextEdit,
            targetTextEdit, attackRollTextEdit, hitDamageEffectTextEdit, missDamageTextEdit, adventurerFeatTextEdit, championFeatTextEdit,
            epicFeatTextEdit, notesTextEdit, triggerTextEdit;

    private TextView spellNameText, attackTypeText, rechargeText, castingTimeText, groupText,
            targetText, attackRollText, hitDamageEffectText, missDamageText, adventurerFeatText, championFeatText,
            epicFeatText, notesText, triggerText;

    private ProgressBar progressBar;

    private NestedScrollView editTextScrollView, textScrollView;

    private AutoCompleteTextView groupTextEdit;

    private FloatingActionButton fab, fabCancel;

    private AddToPowerListDialog addToPowerListDialogFragment;

    private Bundle savedState;

    private Animation shrinkFabAnimation, expandFabAnimation,
            shrinkToSizeFabAnimation;
    //flag for knowing if we should be playing the shrinking animation for fab
    private boolean activityJustStarting = true;

    SpringAnimation fabSpringSlideInAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_details);

        if(!SharedPreferencesHandler.isDatabasePersistanceSet(this)){
            DataSource.setDatabasePersistance();
            SharedPreferencesHandler.setDatabasePersistance(true, this);
        }

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitle(getString(R.string.title_power_details));
        setSupportActionBar(toolbar);
        savedState = savedInstanceState;

        //load animations for fab
        shrinkFabAnimation =
                AnimationUtils.loadAnimation(this, R.anim.fab_scale_animation_shrink_to_none);
        shrinkFabAnimation.setAnimationListener(new shrinkAnimationListener());

        expandFabAnimation =
                AnimationUtils.loadAnimation(this, R.anim.fab_scale_animation_expand_over);
        expandFabAnimation.setAnimationListener(new expandAnimationListener());

        shrinkToSizeFabAnimation =
                AnimationUtils.loadAnimation(this, R.anim.fab_scale_animation_shrink_to_size);

        //final position for this screen size when cancel fab is in the screen
        fabCancelPositionInScreen = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        FAB_CANCEL_FINAL_POSITION_IN_SCREEN_DP,
                        getResources().getDisplayMetrics());
        //final position for this screen size when cancel fab is off screen
        fabCancelPositionOutsideScreen = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        FAB_CANCEL_FINAL_POSITION_OUTSIDE_SCREEN_DP,
                        getResources().getDisplayMetrics());

        //animation for sliding in fab cancel in for this screen size
        pixelPerSecondSlideFab = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        FAB_CANCEL_ANIMATION_SLIDE_IN_DP_PER_SECOND,
                        getResources().getDisplayMetrics());

        progressBar = findViewById(R.id.progressBar);

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
        String powerListName = getIntent().getStringExtra(EXTRA_POWER_LIST_NAME);

        this.drawerHelper = DrawerHelper.getInstance(this, (Toolbar)findViewById(R.id.my_toolbar));
        mActionListener = new PowerDetailsPresenter(
                DataSource.getDatasource(this),
                this,
                this.drawerHelper,
                powerId,
                powerListId,
                powerListName
        );

        fab = findViewById(R.id.fab);
        fabCancel = findViewById(R.id.fabLeft);

        //animations for the fab cancel button

        fabSpringSlideInAnimation = new SpringAnimation(fabCancel,
                DynamicAnimation.TRANSLATION_X, 0);

        fabSpringSlideInAnimation.getSpring().setStiffness(SpringForce.STIFFNESS_LOW);
        fabSpringSlideInAnimation.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        fabSpringSlideInAnimation.setStartVelocity(pixelPerSecondSlideFab);

        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListener.userPressingCancelButton(constructDataFromFields());
            }
        });

        this.drawerActionListener = (DrawerContract.UserActionListener)mActionListener;
        this.drawerActionListener.powerListProfileSelected();

        textScrollView = findViewById(R.id.scrollView_text);

        //add name
        if(!"".equals(powerName)) {
            ((TextView) findViewById(R.id.text_spellName)).setText(powerName);
        }

        if(savedState == null)
            mActionListener.showPowerDetails(false);
        else
            mActionListener.showPowerDetails(savedState.getBoolean("userEditingPower"));


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

        spellNameTextEdit = (TextInputEditText)findViewById(R.id.editText_spellName);
        if(fieldHasText(spellNameTextEdit))
            map.put(PowerDetailsContract.name, spellNameTextEdit.getText().toString());
        if(fieldHasText(attackTypeTextEdit))
            map.put(PowerDetailsContract.attackType, attackTypeTextEdit.getText().toString());
        if(fieldHasText(rechargeTextEdit))
            map.put(PowerDetailsContract.recharge, rechargeTextEdit.getText().toString());
        if(fieldHasText(castingTimeTextEdit))
            map.put(PowerDetailsContract.castingTime, castingTimeTextEdit.getText().toString());
        if(fieldHasText(targetTextEdit))
            map.put(PowerDetailsContract.target, targetTextEdit.getText().toString());
        if(fieldHasText(attackRollTextEdit))
            map.put(PowerDetailsContract.attackRoll, attackRollTextEdit.getText().toString());
        if(fieldHasText(hitDamageEffectTextEdit))
            map.put(PowerDetailsContract.hitDamageOrEffect, hitDamageEffectTextEdit.getText().toString());
        if(fieldHasText(missDamageTextEdit))
            map.put(PowerDetailsContract.missDamage, missDamageTextEdit.getText().toString());
        if(fieldHasText(adventurerFeatTextEdit))
            map.put(PowerDetailsContract.adventurerFeat, adventurerFeatTextEdit.getText().toString());
        if(fieldHasText(championFeatTextEdit))
            map.put(PowerDetailsContract.championFeat, championFeatTextEdit.getText().toString());
        if(fieldHasText(epicFeatTextEdit))
            map.put(PowerDetailsContract.epicFeat, epicFeatTextEdit.getText().toString());
        if(fieldHasText(groupTextEdit))
            map.put(PowerDetailsContract.groupName, groupTextEdit.getText().toString());
        if(fieldHasText(notesTextEdit))
            map.put(PowerDetailsContract.playerNotes, notesTextEdit.getText().toString());
        if(fieldHasText(triggerTextEdit))
            map.put(PowerDetailsContract.trigger, triggerTextEdit.getText().toString());

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
        //pass custom dialog fragment style
        new AlertDialog.Builder(PowerDetailsActivity.this, R.style.dialogFragment_title_style)
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

        textScrollView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        if(editTextScrollView == null) {
            editTextScrollView = (NestedScrollView)
                    ((ViewStubCompat) findViewById(R.id.editText_viewStub)).inflate();
        }
        editTextScrollView.setVisibility(View.VISIBLE);

        fabSpringSlideInAnimation.getSpring().setFinalPosition(fabCancelPositionInScreen);
        fabSpringSlideInAnimation.start();


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


        triggerTextEdit = (TextInputEditText)findViewById(R.id.editText_trigger);
        epicFeatTextEdit = (TextInputEditText)findViewById(R.id.editText_epic_feat);
        championFeatTextEdit = (TextInputEditText)findViewById(R.id.editText_champion_feat);
        adventurerFeatTextEdit = (TextInputEditText)findViewById(R.id.editText_adventurer_feat);
        targetTextEdit = (TextInputEditText)findViewById(R.id.editText_target);
        rechargeTextEdit = (TextInputEditText)findViewById(R.id.editText_recharge);
        notesTextEdit = (TextInputEditText)findViewById(R.id.editText_notes);
        spellNameTextEdit = (TextInputEditText)findViewById(R.id.editText_spellName);
        missDamageTextEdit = (TextInputEditText)findViewById(R.id.editText_miss_damage);
        hitDamageEffectTextEdit = (TextInputEditText)findViewById(R.id.editText_hitDamage_effect);
        groupTextEdit = (AutoCompleteTextView) findViewById(R.id.editText_group);
        castingTimeTextEdit = (TextInputEditText)findViewById(R.id.editText_castingTime);
        attackRollTextEdit = (TextInputEditText)findViewById(R.id.editText_attackRoll);
        attackTypeTextEdit = (TextInputEditText)findViewById(R.id.editText_attackType);

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

        //make sure keyboard does not pop up
        View currentFocus = getCurrentFocus();
        if(currentFocus != null){
            Log.d(TAG, "current focus not null, clearing");
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    @Override
    public void showNonEditableFilledFields(final Spell spell, String powerListName) {

        Log.d(TAG, "starting method call showNonEditableFilledFields");

        if(editTextScrollView != null)
            editTextScrollView.setVisibility(View.GONE);

        progressBar.setVisibility(View.GONE);
        //the textscrollview is not inflated unless it's needed
        textScrollView.setVisibility(View.VISIBLE);

        //have to check if we are just starting this activity
        if(!activityJustStarting)
            fab.startAnimation(shrinkFabAnimation);
        activityJustStarting = false;

        fab.setImageResource(R.drawable.ic_mode_edit_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.userEditingPower(spell);
            }
        });
        fab.setVisibility(View.VISIBLE);

        fabSpringSlideInAnimation.getSpring()
                .setFinalPosition(fabCancelPositionOutsideScreen);
        fabSpringSlideInAnimation.start();

        if(!powerListName.equals("")) {
            ((TextView) findViewById(R.id.text_powerListName)).setText(powerListName);
            findViewById(R.id.powerDetails_mainDivider).setBackgroundColor(
                    Helper.getRandomColorFromString(powerListName)
            );
        }

        //wonder if this is the smartest way to go about this? would some switchcase work better?
        if(!spell.getAttackType().equals("")){
            attackTypeText = findViewById(R.id.text_attackType);
            attackTypeText.setText(spell.getAttackType());
            /*attackTypeLayout = (TextInputLayout)findViewById(R.id.input_layout_attackType);
            attackTypeTextEdit = (TextInputEditText)findViewById(R.id.editText_attackType);
            attackTypeLayout.setVisibility(View.VISIBLE);
            attackTypeTextEdit.setText(spell.getAttackType());
            //set keylistener as null so we have elements that can't be edited, this is re-set in edit view
            attackTypeTextEdit.setKeyListener(null);
            Log.d(TAG, "spell attack type: " + spell.getAttackType());*/
        }

        if(!spell.getAttackRoll().equals("")){
            attackRollText = findViewById(R.id.text_attackRoll);
            attackRollText.setText(spell.getAttackRoll());
            /*attackRollLayout = (TextInputLayout)findViewById(R.id.input_layout_attackRoll);
            attackRollTextEdit = (TextInputEditText)findViewById(R.id.editText_attackRoll);
            attackRollLayout.setVisibility(View.VISIBLE);
            attackRollTextEdit.setText(spell.getAttackRoll());
            attackRollTextEdit.setKeyListener(null);
            Log.d(TAG, "spell attack roll: " + spell.getAttackRoll());*/
        }

        if(!"".equals(spell.getCastingTime())){
            castingTimeText = findViewById(R.id.text_castingTime);
            castingTimeText.setText(spell.getCastingTime());
            /*castingTimeLayout = (TextInputLayout)findViewById(R.id.input_layout_castingTime);
            castingTimeTextEdit = (TextInputEditText)findViewById(R.id.editText_castingTime);
            castingTimeLayout.setVisibility(View.VISIBLE);
            castingTimeTextEdit.setText(spell.getCastingTime());
            castingTimeTextEdit.setKeyListener(null);*/
        }

        if(!spell.getGroupName().equals("")){
            groupText = findViewById(R.id.text_group);
            groupText.setText(spell.getGroupName());
            /*groupLayout = (TextInputLayout)findViewById(R.id.input_layout_group);
            groupTextEdit = (AutoCompleteTextView) findViewById(R.id.editText_group);
            groupLayout.setVisibility(View.VISIBLE);
            groupTextEdit.setText(spell.getGroupName());
            groupTextEdit.setKeyListener(null);
            //hide the dropdown menu, if user has focus on it it will pop visible
            groupTextEdit.dismissDropDown();*/
        }

        if(!spell.getHitDamageOrEffect().equals("")){
            hitDamageEffectText = findViewById(R.id.text_hitDamage_effect);
            hitDamageEffectText.setText(spell.getHitDamageOrEffect());
            /*hitDamageEffectLayout = (TextInputLayout)findViewById(R.id.input_layout_damage_effect);
            hitDamageEffectTextEdit = (TextInputEditText)findViewById(R.id.editText_hitDamage_effect);
            hitDamageEffectLayout.setVisibility(View.VISIBLE);
            hitDamageEffectTextEdit.setText(spell.getHitDamageOrEffect());
            hitDamageEffectTextEdit.setKeyListener(null);*/
        }

        if(!spell.getMissDamage().equals("")){
            missDamageText = findViewById(R.id.text_miss_damage);
            missDamageText.setText(spell.getMissDamage());
            /*missDamageLayout = (TextInputLayout)findViewById(R.id.input_layout_miss_damage);
            missDamageTextEdit = (TextInputEditText)findViewById(R.id.editText_miss_damage);
            missDamageLayout.setVisibility(View.VISIBLE);
            missDamageTextEdit.setText(spell.getMissDamage());
            missDamageTextEdit.setKeyListener(null);*/
        }

        if(!spell.getName().equals("")){
            spellNameText = findViewById(R.id.text_spellName);
            spellNameText.setText(spell.getName());
            /*spellNameLayout = (TextInputLayout)findViewById(R.id.input_layout_spell_name);
            spellNameTextEdit = (TextInputEditText)findViewById(R.id.editText_spellName);
            spellNameLayout.setVisibility(View.VISIBLE);
            //spellNameTextEdit.setFocusable(false);
            spellNameTextEdit.setText(spell.getName());
            spellNameTextEdit.setKeyListener(null);*/
            //spellNameTextEdit.setFocusable(true);
            //InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            //imm.hideSoftInputFromWindow(spellNameTextEdit.getWindowToken(), 0);
        }

        if(!spell.getPlayerNotes().equals("")){
            notesText = findViewById(R.id.text_notes);
            notesText.setText(spell.getPlayerNotes());
            /*notesLayout = (TextInputLayout)findViewById(R.id.input_layout_notes);
            notesTextEdit = (TextInputEditText)findViewById(R.id.editText_notes);
            notesLayout.setVisibility(View.VISIBLE);
            notesTextEdit.setText(spell.getPlayerNotes());
            notesTextEdit.setKeyListener(null);*/
        }

        if(!spell.getRechargeTime().equals("")){
            rechargeText = findViewById(R.id.text_recharge);
            rechargeText.setText(spell.getRechargeTime());
            /*rechargeLayout = (TextInputLayout)findViewById(R.id.input_layout_recharge);
            rechargeTextEdit = (TextInputEditText)findViewById(R.id.editText_recharge);
            rechargeLayout.setVisibility(View.VISIBLE);
            rechargeTextEdit.setText(spell.getRechargeTime());
            rechargeTextEdit.setKeyListener(null);*/
        }

        if(!spell.getTarget().equals("")){
            targetText = findViewById(R.id.text_target);
            targetText.setText(spell.getTarget());
            /*targetLayout = (TextInputLayout)findViewById(R.id.input_layout_target);
            targetTextEdit = (TextInputEditText)findViewById(R.id.editText_target);
            targetLayout.setVisibility(View.VISIBLE);
            targetTextEdit.setText(spell.getTarget());
            targetTextEdit.setKeyListener(null);*/
        }

        if(!spell.getAdventurerFeat().equals("")){
            adventurerFeatText = findViewById(R.id.text_adventurer_feat);
            adventurerFeatText.setText(spell.getAdventurerFeat());
            /*adventurerFeatLayout = (TextInputLayout)findViewById(R.id.input_layout_adventurer_feat);
            adventurerFeatTextEdit = (TextInputEditText)findViewById(R.id.editText_adventurer_feat);
            adventurerFeatLayout.setVisibility(View.VISIBLE);
            adventurerFeatTextEdit.setText(spell.getAdventurerFeat());
            adventurerFeatTextEdit.setKeyListener(null);*/
        }

        if(!spell.getChampionFeat().equals("")){
            championFeatText = findViewById(R.id.text_champion_feat);
            championFeatText.setText(spell.getChampionFeat());
            /*championFeatLayout = (TextInputLayout)findViewById(R.id.input_layout_champion_feat);
            championFeatTextEdit = (TextInputEditText)findViewById(R.id.editText_champion_feat);
            championFeatLayout.setVisibility(View.VISIBLE);
            championFeatTextEdit.setText(spell.getChampionFeat());
            championFeatTextEdit.setKeyListener(null);*/
        }

        if(!spell.getEpicFeat().equals("")){
            epicFeatText = findViewById(R.id.text_epic_feat);
            epicFeatText.setText(spell.getChampionFeat());
            /*epicFeatLayout = (TextInputLayout)findViewById(R.id.input_layout_epic_feat);
            epicFeatTextEdit = (TextInputEditText)findViewById(R.id.editText_epic_feat);
            epicFeatLayout.setVisibility(View.VISIBLE);
            epicFeatTextEdit.setText(spell.getEpicFeat());
            epicFeatTextEdit.setKeyListener(null);*/
        }

        if(!spell.getTrigger().equals("")){
            triggerText = findViewById(R.id.text_trigger);
            triggerText.setText(spell.getTrigger());
            /*triggerLayout = (TextInputLayout)findViewById(R.id.input_layout_trigger);
            triggerTextEdit = (TextInputEditText)findViewById(R.id.editText_trigger);
            triggerLayout.setVisibility(View.VISIBLE);
            triggerTextEdit.setText(spell.getTrigger());
            triggerTextEdit.setKeyListener(null);*/
        }

        /*FlingAnimation fabFlingAnimation = new FlingAnimation(fab, DynamicAnimation.SCROLL_X);
        //get the fling speed as pixel/s for this device
        float dpPerSecond = 100;
        float pixelPerSecond = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        dpPerSecond,
                        getResources().getDisplayMetrics());
        fabFlingAnimation.setStartVelocity(pixelPerSecond)
                .setMaxValue(500)
                .start();*/
    }

    @Override
    public void showEditableFilledFields(Spell spell) {

        editingSpell = true;

        progressBar.setVisibility(View.GONE);
        if(editTextScrollView == null) {
            editTextScrollView = (NestedScrollView)
                    ((ViewStubCompat) findViewById(R.id.editText_viewStub)).inflate();
        }
        editTextScrollView.setVisibility(View.VISIBLE);

        textScrollView.setVisibility(View.GONE);

        //have to check if we are just starting this activity
        if(!activityJustStarting)
            fab.startAnimation(shrinkFabAnimation);
        activityJustStarting = false;

        fab.setImageResource(R.drawable.ic_done_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.userSavingModifiedPower(constructDataFromFields());
                editingSpell = false;
            }
        });
        fab.setVisibility(View.VISIBLE);

        fabCancel.setVisibility(View.VISIBLE);
        //fabCancel.startAnimation(slideInFabAnimation);

        fabSpringSlideInAnimation.getSpring().setFinalPosition(fabCancelPositionInScreen);
        fabSpringSlideInAnimation.start();

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
        spellNameTextEdit = (TextInputEditText)findViewById(R.id.editText_spellName);
        spellNameTextEdit.setText(spell.getName());
        spellNameTextEdit.setKeyListener(newListener);

        attackTypeTextEdit = (TextInputEditText)findViewById(R.id.editText_attackType);
        attackTypeTextEdit.setText(spell.getAttackType());
        attackTypeTextEdit.setKeyListener(newListener);

        rechargeTextEdit = (TextInputEditText)findViewById(R.id.editText_recharge);
        rechargeTextEdit.setText(spell.getRechargeTime());
        rechargeTextEdit.setKeyListener(newListener);

        castingTimeTextEdit = (TextInputEditText)findViewById(R.id.editText_castingTime);
        castingTimeTextEdit.setText(spell.getCastingTime());
        castingTimeTextEdit.setKeyListener(newListener);

        targetTextEdit = (TextInputEditText)findViewById(R.id.editText_target);
        targetTextEdit.setText(spell.getTarget());
        targetTextEdit.setKeyListener(newListener);

        attackRollTextEdit = (TextInputEditText)findViewById(R.id.editText_attackRoll);
        attackRollTextEdit.setText(spell.getAttackRoll());
        attackRollTextEdit.setKeyListener(newListener);

        hitDamageEffectTextEdit = (TextInputEditText)findViewById(R.id.editText_hitDamage_effect);
        hitDamageEffectTextEdit.setText(spell.getHitDamageOrEffect());
        hitDamageEffectTextEdit.setKeyListener(newListener);

        missDamageTextEdit = (TextInputEditText)findViewById(R.id.editText_miss_damage);
        missDamageTextEdit.setText(spell.getMissDamage());
        missDamageTextEdit.setKeyListener(newListener);

        adventurerFeatTextEdit = (TextInputEditText)findViewById(R.id.editText_adventurer_feat);
        adventurerFeatTextEdit.setText(spell.getAdventurerFeat());
        adventurerFeatTextEdit.setKeyListener(newListener);

        championFeatTextEdit = (TextInputEditText)findViewById(R.id.editText_champion_feat);
        championFeatTextEdit.setText(spell.getChampionFeat());
        championFeatTextEdit.setKeyListener(newListener);

        epicFeatTextEdit = (TextInputEditText)findViewById(R.id.editText_epic_feat);
        epicFeatTextEdit.setText(spell.getEpicFeat());
        epicFeatTextEdit.setKeyListener(newListener);

        groupTextEdit = (AutoCompleteTextView) findViewById(R.id.editText_group);
        groupTextEdit.setText(spell.getGroupName());
        groupTextEdit.setKeyListener(newListener);

        notesTextEdit = (TextInputEditText)findViewById(R.id.editText_notes);
        notesTextEdit.setText(spell.getPlayerNotes());
        notesTextEdit.setKeyListener(newListener);

        triggerTextEdit = (TextInputEditText)findViewById(R.id.editText_trigger);
        triggerTextEdit.setText(spell.getTrigger());
        triggerTextEdit.setKeyListener(newListener);

        //make sure keyboard does not pop up
        View currentFocus = getCurrentFocus();
        if(currentFocus != null){
            Log.d(TAG, "current focus not null, clearing");
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
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
        Snackbar snackbar = Snackbar.make(
            findViewById(R.id.coordinatorLayout),
            R.string.error_empty_fields,
            Snackbar.LENGTH_SHORT);

        //to set the text colour
        int snackBarTextId = android.support.design.R.id.snackbar_text;
        TextView snackBarText = (TextView) snackbar.getView().findViewById(snackBarTextId);
        snackBarText.setTextColor(ContextCompat.getColor(this, R.color.myTextColorPrimary));

        snackbar.show();
    }

    @Override
    public void populatePowerGroupSuggestions(String[] powerGroups) {
        //create the adapter with the group names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.popup_list_row_item,
                powerGroups);


        if(groupTextEdit == null)
            groupTextEdit = (AutoCompleteTextView) findViewById(R.id.editText_group);
        //set adapter for the text view
        groupTextEdit.setAdapter(adapter);
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

        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout),
                getString(R.string.toast_powers_copied),
                Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.action_undo),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mActionListener.userPushingUndo();
                            }
                        });
        //to set the text colour
        int snackBarTextId = android.support.design.R.id.snackbar_text;
        TextView snackBarText = (TextView) snackbar.getView().findViewById(snackBarTextId);
        snackBarText.setTextColor(ContextCompat.getColor(this, R.color.myTextColorPrimary));

        snackbar.show();
    }

    // From ConfirmDeletionDialog.ConfirmDeletionListener
    @Override
    public void onPositiveClick() {
        Log.d(TAG, "onPositiveClick: deleting power...");
        mActionListener.userPressingDeletePower();
    }

    /**
     * Listener for the shrink to nothingness animation for main fab, after
     * the fab has shrunk completely the expandFabAnimation is launched to bring back
     * the fab to visibility
     */
    private class shrinkAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            fab.startAnimation(expandFabAnimation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    /**
     * Listener attached to the main fab's expansion animation,
     * the animation extends a bit larger than the final fab size is so
     * after it is finished the shrinkToSizeFabAnimation is launched
     */
    private class expandAnimationListener implements Animation.AnimationListener{
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            fab.startAnimation(shrinkToSizeFabAnimation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}
