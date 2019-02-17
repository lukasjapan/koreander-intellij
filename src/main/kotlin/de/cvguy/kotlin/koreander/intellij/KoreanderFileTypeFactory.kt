package de.cvguy.kotlin.koreander.intellij

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory


class KoreanderFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(ftc: FileTypeConsumer) {
        ftc.consume(FILE_TYPE_INSTANCE)
    }

    companion object {
        val FILE_TYPE_INSTANCE = KoreanderFileType()
    }
}