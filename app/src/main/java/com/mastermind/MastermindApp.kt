package com.mastermind

import android.app.Application
import com.mastermind.utils.getSavedLanguage
import java.util.*

class MastermindApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val lang = getSavedLanguage(this)
        if (lang.isNotEmpty()) {
            val locale = Locale(lang)
            Locale.setDefault(locale)
        }
    }
}