package de.cvguy.kotlin.koreander.intellij

import com.intellij.lexer.Lexer
import com.intellij.lexer.LexerPosition
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import de.cvguy.kotlin.koreander.parser.Token
import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.backend.common.push
import org.jetbrains.kotlin.lexer.KotlinLexer
import de.cvguy.kotlin.koreander.parser.Lexer as KoreanderLexer

class KoreanderLexer : Lexer() {
    data class LexedToken(
            val offsetStart: Int,
            val offsetEnd: Int,
            val originalType: Token.Type,
            val type: IElementType
    )

    val koreanderLexer = KoreanderLexer()
    val kotlinLexer = KotlinLexer()

    var currentRawBuffer: CharSequence = ""
    var currentBuffer: String = ""
    var currentTokens: List<LexedToken> = listOf()
    var currentTokenIterator: ListIterator<LexedToken>? = null
    var currentToken: LexedToken? = null
    var currentEndOffset = 0
    var currentState = 0

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        currentRawBuffer = buffer
        currentBuffer = buffer.toString()
        currentTokens = lexTokens(currentBuffer).flatMap(this::mapToKotlinTokens)
        currentTokenIterator = currentTokens.takeIf { it.size > 0 }?.listIterator()
        currentToken = null
        while (true) {
            currentToken = currentTokenIterator?.takeIf { it.hasNext() }?.next() ?: break
            if(currentToken!!.offsetEnd > startOffset) break
        }
        currentState = initialState
        currentEndOffset = endOffset
    }

    private fun lexTokens(input: String): List<LexedToken> {
        val nextTokens = mutableListOf<LexedToken>()
        var latestEndOffset = 0

        koreanderLexer.lexString(input).filter { it.content.length > 0 }.forEach { token ->
            val len = token.offset - latestEndOffset
            if(len > 0) {
                val extraToken = LexedToken(
                        latestEndOffset,
                        latestEndOffset + len,
                        Token.Type.WHITE_SPACE,
                        TokenType.NEW_LINE_INDENT
                )
                nextTokens.add(extraToken)
            }

            nextTokens.add(LexedToken(
                    token.offset,
                    token.offset + token.content.length,
                    token.type,
                    types.getValue(token.type)
            ))

            latestEndOffset = token.offset + token.content.length
        }

        if(latestEndOffset < input.length) {
            val extraToken = LexedToken(
                    latestEndOffset,
                    input.length,
                    Token.Type.WHITE_SPACE,
                    TokenType.NEW_LINE_INDENT
            )
            nextTokens.add(extraToken)
        }

        return nextTokens
    }

    private fun mapToKotlinTokens(token: LexedToken): List<LexedToken> {
        if(listOf(Token.Type.STRING, Token.Type.TEXT).contains(token.originalType)) {
            return parseKotlin(token, -3, { "\"\"\"${it}\"\"\"" }, { it.removeAt(0); it.removeAt(it.size - 1)})
        }

        if(token.originalType == Token.Type.QUOTED_STRING) {
            return parseKotlin(token, -2, { "\"\"${it}\"\"" }, {
                it.removeAt(0);
                it.removeAt(it.size - 1)
                it.add(0, LexedToken(token.offsetStart, token.offsetStart + 1, token.originalType, types.getValue(Token.Type.QUOTED_STRING)))
                it.add(it.size, LexedToken(token.offsetEnd - 1, token.offsetEnd, token.originalType, types.getValue(Token.Type.QUOTED_STRING)))
            })
        }

        if(token.originalType == Token.Type.EXPRESSION) {
            return parseKotlin(token)
        }

        if(token.originalType == Token.Type.BRACKET_EXPRESSION) {
            return parseKotlin(token, 1, { it.drop(1).dropLast(1)}, {
                it.add(0, LexedToken(token.offsetStart, token.offsetStart + 1, token.originalType, types.getValue(Token.Type.BRACKET_EXPRESSION)))
                it.add(it.size, LexedToken(token.offsetEnd - 1, token.offsetEnd, token.originalType, types.getValue(Token.Type.BRACKET_EXPRESSION)))
            })
        }

        return listOf(token)
    }

    private fun parseKotlin(token: LexedToken, d: Int = 0, preProcess: (String) -> String = { it }, postProcess: (MutableList<LexedToken>) -> Unit = { }): List<LexedToken> {
        val input = preProcess(currentBuffer.subSequence(token.offsetStart, token.offsetEnd).toString())
        val tokens = mutableListOf<LexedToken>()
        kotlinLexer.start(input)
        while(kotlinLexer.tokenType != null) {
            val kotlinToken = LexedToken(
                    kotlinLexer.tokenStart + token.offsetStart + d,
                    kotlinLexer.tokenEnd + token.offsetStart + d,
                    token.originalType,
                    kotlinLexer.tokenType!!
            )
            tokens.add(kotlinToken)
            kotlinLexer.advance()
        }

        postProcess(tokens)

        while(tokens.last().offsetEnd > token.offsetEnd) {
            val badToken = tokens.pop()
            if(badToken.offsetStart < token.offsetEnd) {
                tokens.push(badToken.copy(offsetEnd = token.offsetEnd))
            }
        }

        return tokens
    }

    override fun getState() = currentState
    override fun getTokenStart() = currentToken?.offsetStart ?: 0
    override fun getTokenEnd() = currentToken?.offsetEnd ?: 0

    override fun getCurrentPosition(): LexerPosition = object : LexerPosition {
        override fun getOffset() = currentToken?.offsetStart ?: 0
        override fun getState() = currentState
    }

    override fun getBufferSequence() = currentRawBuffer
    override fun getBufferEnd() = currentEndOffset

    override fun getTokenType(): IElementType? = currentToken?.type


    override fun advance() {
        currentToken = currentTokenIterator?.takeIf { it.hasNext() }?.next()
    }

    override fun restore(lp: LexerPosition) {
        if(currentTokens.size > 0) return
        currentTokenIterator = currentTokens.listIterator()
        currentToken = currentTokenIterator?.next()
        while(currentToken?.offsetStart != lp.offset) currentToken = currentTokenIterator?.next()
    }

    companion object {
        val types = mapOf(
                Token.Type.DOC_TYPE_IDENTIFIER to IElementType("DOC_TYPE_IDENTIFIER", KoreanderLanguage.INSTANCE),
                Token.Type.DOC_TYPE to IElementType("DOC_TYPE", KoreanderLanguage.INSTANCE),
                Token.Type.WHITE_SPACE to TokenType.NEW_LINE_INDENT,
                Token.Type.ELEMENT_IDENTIFIER to IElementType("ELEMENT_IDENTIFIER", KoreanderLanguage.INSTANCE),
                Token.Type.ELEMENT_ID_IDENTIFIER to IElementType("ELEMENT_ID_IDENTIFIER", KoreanderLanguage.INSTANCE),
                Token.Type.ELEMENT_CLASS_IDENTIFIER to IElementType("ELEMENT_CLASS_IDENTIFIER", KoreanderLanguage.INSTANCE),
                Token.Type.ATTRIBUTE_KEY to IElementType("ATTRIBUTE_KEY", KoreanderLanguage.INSTANCE),
                Token.Type.ATTRIBUTE_CONNECTOR to IElementType("ATTRIBUTE_CONNECTOR", KoreanderLanguage.INSTANCE),
                Token.Type.FILTER_IDENTIFIER to IElementType("FILTER_IDENTIFIER", KoreanderLanguage.INSTANCE),
                Token.Type.STRING to IElementType("STRING", KoreanderLanguage.INSTANCE),
                Token.Type.TEXT to IElementType("TEXT", KoreanderLanguage.INSTANCE),
                Token.Type.QUOTED_STRING to IElementType("QUOTED_STRING", KoreanderLanguage.INSTANCE),
                Token.Type.EXPRESSION to IElementType("EXPRESSION", KoreanderLanguage.INSTANCE),
                Token.Type.BRACKET_EXPRESSION to IElementType("BRACKET_EXPRESSION", KoreanderLanguage.INSTANCE),
                Token.Type.CODE_IDENTIFIER to IElementType("CODE_IDENTIFIER", KoreanderLanguage.INSTANCE),
                Token.Type.LAMBDA_VARIABLES_IDENTIFIER to IElementType("LAMBDA_VARIABLES_IDENTIFIER", KoreanderLanguage.INSTANCE),
                Token.Type.LAMBDA_VARIABLES to IElementType("LAMBDA_VARIABLES", KoreanderLanguage.INSTANCE),
                Token.Type.SILENT_CODE_IDENTIFIER to IElementType("SILENT_CODE_IDENTIFIER", KoreanderLanguage.INSTANCE),
                Token.Type.COMMENT_IDENTIFIER to IElementType("COMMENT_IDENTIFIER", KoreanderLanguage.INSTANCE),
                Token.Type.COMMENT to IElementType("COMMENT", KoreanderLanguage.INSTANCE)
        )
    }
}