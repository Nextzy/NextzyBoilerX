package com.nextzy.library.boilerx.utility.number

import java.text.DecimalFormat

object NumberConverter {
    fun getIntegerFormatInCustomFormat(number : Long,pattern : String): String? {
       val format = DecimalFormat(pattern)
        return format.format(number)
    }

    fun getDecimalFormatInCustomFormat(number : Double,pattern : String): String? {
        val format = DecimalFormat(pattern)
        return format.format(number)
    }

    fun getStringFormatInCustomFormat(text : String,pattern : String): String? {
        val format = DecimalFormat(pattern)
        return format.format(text)
    }
}