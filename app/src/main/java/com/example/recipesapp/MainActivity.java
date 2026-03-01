package com.example.recipesapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private EditText etSearch;
    private LinearLayout recipesContainer;
    private Button btnLanguage;
    private TextView tvNoRecipes;
    
    private Chip chipAll;
    private Chip chipBreakfast;
    private Chip chipLunch;
    private Chip chipDinner;

    private Set<String> currentFilters = new HashSet<>();
    private String currentSearch = "";

    private MediaPlayer mpLaunch;
    private MediaPlayer mpRecipe;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        etSearch = findViewById(R.id.etSearch);
        chipAll = findViewById(R.id.chipAll);
        chipBreakfast = findViewById(R.id.chipBreakfast);
        chipLunch = findViewById(R.id.chipLunch);
        chipDinner = findViewById(R.id.chipDinner);
        recipesContainer = findViewById(R.id.recipesContainer);
        btnLanguage = findViewById(R.id.btnLanguage);
        tvNoRecipes = findViewById(R.id.tvNoRecipes);

        mpLaunch = MediaPlayer.create(this, R.raw.launch);
        mpRecipe = MediaPlayer.create(this, R.raw.recipe);

        mpLaunch.start();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLanguage.setOnClickListener(v -> showLanguageDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().toLowerCase();
                updateRecipeButtons();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupChipListeners();

        updateRecipeButtons();
    }
    
    private void setupChipListeners() {
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onAllChanged(isChecked);
        });
        
        chipBreakfast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onCategoryChanged();
        });
        
        chipLunch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onCategoryChanged();
        });
        
        chipDinner.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onCategoryChanged();
        });
    }
    
    private void removeAllListeners() {
        chipAll.setOnCheckedChangeListener(null);
        chipBreakfast.setOnCheckedChangeListener(null);
        chipLunch.setOnCheckedChangeListener(null);
        chipDinner.setOnCheckedChangeListener(null);
    }
    
    private void onAllChanged(boolean isChecked) {
        if (isChecked) {

            removeAllListeners();
            chipBreakfast.setChecked(false);
            chipLunch.setChecked(false);
            chipDinner.setChecked(false);
            currentFilters.clear();
            setupChipListeners();
            updateRecipeButtons();
        } else {

            if (!chipBreakfast.isChecked() && !chipLunch.isChecked() && !chipDinner.isChecked()) {
                removeAllListeners();
                chipAll.setChecked(true);
                setupChipListeners();
            } else {
                updateFiltersFromChips();
                updateRecipeButtons();
            }
        }
    }
    
    private void onCategoryChanged() {

        int count = 0;
        if (chipBreakfast.isChecked()) count++;
        if (chipLunch.isChecked()) count++;
        if (chipDinner.isChecked()) count++;
        
        if (count == 3) {

            removeAllListeners();
            chipAll.setChecked(true);
            chipBreakfast.setChecked(false);
            chipLunch.setChecked(false);
            chipDinner.setChecked(false);
            currentFilters.clear();
            setupChipListeners();
            updateRecipeButtons();
        } else if (count == 0) {

            removeAllListeners();
            chipAll.setChecked(true);
            currentFilters.clear();
            setupChipListeners();
            updateRecipeButtons();
        } else {

            if (chipAll.isChecked()) {
                removeAllListeners();
                chipAll.setChecked(false);
                setupChipListeners();
            }
            updateFiltersFromChips();
            updateRecipeButtons();
        }
    }
    
    private void updateFiltersFromChips() {
        currentFilters.clear();
        if (chipBreakfast.isChecked()) currentFilters.add("breakfast");
        if (chipLunch.isChecked()) currentFilters.add("lunch");
        if (chipDinner.isChecked()) currentFilters.add("dinner");
    }
    
    private void updateFiltersAndUI() {
        currentFilters.clear();
        if (chipBreakfast.isChecked()) currentFilters.add("breakfast");
        if (chipLunch.isChecked()) currentFilters.add("lunch");
        if (chipDinner.isChecked()) currentFilters.add("dinner");
        
        updateRecipeButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRecipeButtons();
    }

    private void showLanguageDialog() {
        String[] languages = {
            getString(R.string.lang_english),
            getString(R.string.lang_russian),
            getString(R.string.lang_italian)
        };

        String[] languageCodes = {
            LocaleHelper.LANGUAGE_ENGLISH,
            LocaleHelper.LANGUAGE_RUSSIAN,
            LocaleHelper.LANGUAGE_ITALIAN
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_language);
        builder.setItems(languages, (dialog, which) -> {
            String selectedLanguage = languageCodes[which];
            LocaleHelper.setLanguage(this, selectedLanguage);
            recreate();
        });
        builder.show();
    }

    private void updateRecipeButtons() {
        recipesContainer.removeAllViews();
        tvNoRecipes.setVisibility(View.GONE);

        Recipe[] allRecipes = Recipe.getAllRecipes();
        Resources res = getResources();

        for (Recipe recipe : allRecipes) {
            boolean matchesFilter = currentFilters.isEmpty() || 
                                    currentFilters.contains(recipe.getCategory());

            String recipeName = "";
            try {
                int resId = res.getIdentifier(recipe.getNameKey(), "string", getPackageName());
                if (resId != 0) {
                    recipeName = res.getString(resId).toLowerCase();
                }
            } catch (Exception e) {
                recipeName = "";
            }

            boolean matchesSearch = currentSearch.isEmpty() || 
                                    recipeName.contains(currentSearch);

            if (matchesFilter && matchesSearch) {
                addRecipeButton(recipe);
            }
        }
        
        if (recipesContainer.getChildCount() == 0) {
            tvNoRecipes.setVisibility(View.VISIBLE);
        }
    }

    private void addRecipeButton(Recipe recipe) {
        MaterialButton button = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);

        int resId = getResources().getIdentifier(recipe.getNameKey(), "string", getPackageName());
        String recipeName = resId != 0 ? getString(resId) : recipe.getNameKey();
        
        button.setText(recipeName);
        button.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.setMargins(0, 8, 0, 8);
        button.setLayoutParams(params);
        
        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, recipe.getActivityClass());
            startActivity(intent);
            mpRecipe.start();
        });
        
        recipesContainer.addView(button);
    }
}
