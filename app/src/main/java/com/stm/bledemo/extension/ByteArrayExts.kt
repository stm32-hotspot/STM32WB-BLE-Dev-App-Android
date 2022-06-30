package com.stm.bledemo.extension

// Converts Byte Array to Hexadecimal String
fun ByteArray.toHexString(): String =
    joinToString (separator = " ", prefix = "0x") { String.format("%02x", it) }