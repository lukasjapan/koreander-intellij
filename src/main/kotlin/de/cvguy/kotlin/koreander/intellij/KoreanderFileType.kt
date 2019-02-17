package de.cvguy.kotlin.koreander.intellij

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader

class KoreanderFileType : LanguageFileType(KoreanderLanguage.INSTANCE) {
    override fun getIcon() = IconLoader.getIcon("/koreander32.png");
    override fun getName() = "Koreander"
    override fun getDefaultExtension() = "kor"
    override fun getDescription() = "Koreander template"
}