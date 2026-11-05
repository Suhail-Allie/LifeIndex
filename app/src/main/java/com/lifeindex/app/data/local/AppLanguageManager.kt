package com.lifeindex.app.data.local

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Stores and applies the user's preferred LifeIndex language.
 *
 * Android 13+ uses the platform per-app language API. Older Android versions
 * use a persisted preference and a locale-wrapped Activity context.
 */
object AppLanguageManager {
    private const val PREFERENCES_NAME = "lifeindex_language_preferences"
    private const val LANGUAGE_KEY = "selected_language"

    private val supportedLanguages = setOf("en", "af", "xh")

    fun currentLanguage(context: Context): String {
        val language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
            if (!locales.isEmpty) {
                locales[0]?.language ?: Locale.getDefault().language
            } else {
                Locale.getDefault().language
            }
        } else {
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(LANGUAGE_KEY, null)
                ?: Locale.getDefault().language
        }

        return language.takeIf { it in supportedLanguages } ?: "en"
    }

    fun applyLanguage(context: Context, languageCode: String) {
        if (languageCode !in supportedLanguages) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            val currentLocales = localeManager.applicationLocales
            val currentLanguage = if (!currentLocales.isEmpty) {
                currentLocales[0]?.language
            } else {
                null
            }

            if (currentLanguage != languageCode) {
                localeManager.applicationLocales = LocaleList.forLanguageTags(languageCode)
            }
        } else {
            val preferences = context.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            val savedLanguage = preferences.getString(LANGUAGE_KEY, null)

            if (savedLanguage != languageCode) {
                preferences.edit()
                    .putString(LANGUAGE_KEY, languageCode)
                    .apply()

                findActivity(context)?.recreate()
            }
        }
    }

    fun wrap(context: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context
        }

        val savedLanguage = context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        ).getString(LANGUAGE_KEY, null) ?: return context

        if (savedLanguage !in supportedLanguages) return context

        val locale = Locale.forLanguageTag(savedLanguage)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    private tailrec fun findActivity(context: Context): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> findActivity(context.baseContext)
            else -> null
        }
    }
}
