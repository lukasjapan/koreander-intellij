package de.cvguy.kotlin.koreander.intellij

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as HC
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.idea.highlighter.KotlinHighlighter

class KoreanderSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getTokenHighlights(type: IElementType) = colorMapping.get(type.toString()) ?: kotlinHighlighter.getTokenHighlights(type)

    override fun getHighlightingLexer() = lexer

    companion object {
        val lexer = KoreanderLexer()
        val kotlinHighlighter = KotlinHighlighter()
        val colorMapping = mapOf(
                "DOC_TYPE_IDENTIFIER" to arrayOf(HC.LINE_COMMENT),
                "DOC_TYPE" to arrayOf(HC.LINE_COMMENT),
                "NEW_LINE_INDENT" to arrayOf(HC.STRING), // ?
                "ELEMENT_IDENTIFIER" to arrayOf(HC.VALID_STRING_ESCAPE),
                "ELEMENT_ID_IDENTIFIER" to arrayOf(HC.VALID_STRING_ESCAPE),
                "ELEMENT_CLASS_IDENTIFIER" to arrayOf(HC.VALID_STRING_ESCAPE),
                "ATTRIBUTE_KEY" to arrayOf(HC.IDENTIFIER),
                "ATTRIBUTE_CONNECTOR" to arrayOf(HC.VALID_STRING_ESCAPE),
                "FILTER_IDENTIFIER" to arrayOf(HC.IDENTIFIER),
                "STRING" to arrayOf(HC.STRING),
                "TEXT" to arrayOf(HC.STRING),
                "QUOTED_STRING" to arrayOf(HC.IDENTIFIER),
                "EXPRESSION" to arrayOf(HC.LOCAL_VARIABLE),
                "BRACKET_EXPRESSION" to arrayOf(HC.VALID_STRING_ESCAPE),
                "CODE_IDENTIFIER" to arrayOf(HC.IDENTIFIER),
                "LAMBDA_VARIABLES_IDENTIFIER" to arrayOf(HC.IDENTIFIER),
                "LAMBDA_VARIABLES" to arrayOf(HC.IDENTIFIER),
                "SILENT_CODE_IDENTIFIER" to arrayOf(HC.IDENTIFIER),
                "COMMENT_IDENTIFIER" to arrayOf(HC.LINE_COMMENT),
                "COMMENT" to arrayOf(HC.LINE_COMMENT)
        )
    }
}