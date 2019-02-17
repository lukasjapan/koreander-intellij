import de.cvguy.kotlin.koreander.intellij.KoreanderLexer
import org.junit.Test

class KoreanderLexerTest {
    @Test
    fun test() {
        val lexer = KoreanderLexer()
        val fileContent = KoreanderLexerTest::class.java.getResource("/test/example.kor").readText()

        lexer.start(fileContent)
        println(lexer)
    }
}