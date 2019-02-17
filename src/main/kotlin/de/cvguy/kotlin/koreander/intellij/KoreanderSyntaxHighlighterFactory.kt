package de.cvguy.kotlin.koreander.intellij

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class KoreanderSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?) = INSTANCE

    companion object {
        val INSTANCE = KoreanderSyntaxHighlighter()
    }
}