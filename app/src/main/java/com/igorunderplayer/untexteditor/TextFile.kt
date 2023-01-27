package com.igorunderplayer.untexteditor

import android.net.Uri
import androidx.core.net.toFile
import java.io.*

class TextFile {
    public var fileUri: Uri
    public var inputStream: InputStream

    constructor(documentUri: Uri, inputStream: InputStream) {
        this.fileUri = documentUri

        this.inputStream = inputStream
    }

    public fun readAllText(): String {
        val reader = InputStreamReader(inputStream)
        val text = reader.readText()
        reader.close()
        return text
    }

    public fun updateText(text: String) {
        inputStream = text.byteInputStream()
    }

    public fun closeFile() {
        inputStream.close()
    }
}