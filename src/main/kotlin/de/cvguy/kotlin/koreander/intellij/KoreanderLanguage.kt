package de.cvguy.kotlin.koreander.intellij

import com.intellij.lang.Language

class KoreanderLanguage : Language("Koreander") {
    companion object {
        val INSTANCE = KoreanderLanguage()
    }
}