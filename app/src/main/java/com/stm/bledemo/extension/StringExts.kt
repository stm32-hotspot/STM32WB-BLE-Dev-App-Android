package com.stm.bledemo.extension

// Converts Hexadecimal to ASCII
fun String.hexToASCII(): String {
    val charList = chunked(2).map { it.toInt(16).toChar() }
    return String(charList.toCharArray())
}

// Converts Hex String to ByteArray
// https://stackoverflow.com/questions/66613717/kotlin-convert-hex-string-to-bytearray
fun String.hexToByteArray(): ByteArray {
    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

// Removes White Space from String
fun String.removeWhiteSpace(): String {
    return filter { !it.isWhitespace() }
}