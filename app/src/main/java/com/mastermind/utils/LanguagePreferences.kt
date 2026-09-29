package com.mastermind.utils

import android.content.Context
import java.util.Locale
import androidx.core.content.edit

fun persistLanguage(context: Context, langCode: String) {
    context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        .edit {
            putString("language", langCode)
        }
}

fun getSavedLanguage(context: Context): String {
    return context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        .getString("language", Locale.getDefault().language) ?: "en"
}
