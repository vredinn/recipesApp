package com.example.recipesapp;

import android.content.Intent;

public class Recipe {
    private String id;
    private String nameKey;
    private String category;
    private Class<?> activityClass;

    public Recipe(String id, String nameKey, String category, Class<?> activityClass) {
        this.id = id;
        this.nameKey = nameKey;
        this.category = category;
        this.activityClass = activityClass;
    }

    public String getId() {
        return id;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getCategory() {
        return category;
    }

    public Class<?> getActivityClass() {
        return activityClass;
    }

    public static Recipe[] getAllRecipes() {
        return new Recipe[] {

            new Recipe("pancakes", "recipe_pancakes", "breakfast", PancakesActivity.class),
            new Recipe("omelette", "recipe_omelette", "breakfast", OmeletteActivity.class),
            new Recipe("boiled_eggs", "recipe_boiled_eggs", "breakfast", BoiledEggsActivity.class),

            new Recipe("salad", "recipe_salad", "lunch", SaladActivity.class),
            new Recipe("soup", "recipe_soup", "lunch", SoupActivity.class),

            new Recipe("pasta", "recipe_pasta", "dinner", PastaActivity.class),
            new Recipe("steak", "recipe_steak", "dinner", SteakActivity.class)
        };
    }

    public static Recipe[] getRecipesByCategory(String category) {
        Recipe[] allRecipes = getAllRecipes();
        int count = 0;

        for (Recipe recipe : allRecipes) {
            if (recipe.getCategory().equals(category)) {
                count++;
            }
        }

        Recipe[] filtered = new Recipe[count];
        int index = 0;
        for (Recipe recipe : allRecipes) {
            if (recipe.getCategory().equals(category)) {
                filtered[index++] = recipe;
            }
        }
        
        return filtered;
    }
}
