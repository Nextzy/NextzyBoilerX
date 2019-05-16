package com.nextzy.library.boilerx.utility.datetime

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeParseException

class DateTimeUtil {

    fun getCurrentZonedDateTime(): ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())

    fun getNextDayOf(epochSecond: Long): Long =
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault()).plusDays(1).toEpochSecond()

    fun getLocalDateTimeInCustomFormat(dateString: String, formatString: String): ZonedDateTime {
        val format = org.threeten.bp.format.DateTimeFormatter.ofPattern(formatString).atBangkok()
        return org.threeten.bp.LocalDate.parse(dateString, format).atStartOfDay().atZone(ZoneId.systemDefault())
    }

    fun getThaiDateMessage(millis: Long): String {
        return getDateTimeMessageByFormatEpochMilli(millis, "dd MMM yyyy")
    }

    fun getDateTimeMessageByFormatEpochMilli(millis: Long, format: String): String {
        return try {
            val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
            val formatter = org.threeten.bp.format.DateTimeFormatter.ofPattern(format).atBangkok()
            dateTime.format(formatter)
        } catch (exception: DateTimeParseException) {
            ""
        }
    }

}