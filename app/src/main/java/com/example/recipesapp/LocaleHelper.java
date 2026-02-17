package com.example.recipesapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleHelper {
    
    private static final String PREFS_NAME = " recipes_prefs";
    private static final String KEY_LANGUAGE = "language";
    
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_RUSSIAN = "ru";
    public static final String LANGUAGE_ITALIAN = "it";
    public static final String LANGUAGE_SYSTEM = "system";

    public static Context setLocale(Context context, String language) {
        return updateResources(context, language);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale;
        
        if (language.equals(LANGUAGE_SYSTEM)) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            locale = new Locale(language);
        }
        
        Locale.setDefault(locale);
        
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new android.os.LocaleList(locale));
        } else {
            config.locale = locale;
        }
        
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        
        return context;
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM);
    }

    public static void setLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public static String getDeviceLanguage() {
        String deviceLanguage = Locale.getDefault().getLanguage();
        
        // Check if device language is one of our supported languages
        if (deviceLanguage.equals("en")) {
            return LANGUAGE_ENGLISH;
        } else if (deviceLanguage.equals("ru")) {
            return LANGUAGE_RUSSIAN;
        } else if (deviceLanguage.equals("it")) {
            return LANGUAGE_ITALIAN;
        }
        
        // Default to English if not supported
        return LANGUAGE_ENGLISH;
    }

    public static Context onAttach(Context context) {
        String lang = getLanguage(context);
        return setLocale(context, lang);
    }

    public static String getLanguageDisplayName(String languageCode) {
        switch (languageCode) {
            case LANGUAGE_ENGLISH:
                return "English";
            case LANGUAGE_RUSSIAN:
                return "Русский";
            case LANGUAGE_ITALIAN:
                return "Italiano";
            default:
                return "English";
        }
    }

    public static String[] getSupportedLanguages() {
        return new String[]{
            LANGUAGE_ENGLISH,
            LANGUAGE_RUSSIAN,
            LANGUAGE_ITALIAN
        };
    }
}
