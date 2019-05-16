package com.nextzy.library.boilerx.utility.datetime

import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import java.util.*


fun LocalDateTime.dateOnly(): LocalDateTime {
    val year = year.toString().padStart(4, '0')
    val month = month.value.toString().padStart(2, '0')
    val day = dayOfMonth.toString().padStart(2, '0')
    return LocalDateTime.ofInstant(Instant.parse("$year-$month-${day}T00:00:00.00Z"), ZoneOffset.UTC)
}

fun LocalDateTime.isToday(): Boolean {
    val currentDate = this.dateOnly()
    val today = Instant.now().dateOnly()
    return currentDate.isEqual(today)
}

fun LocalDateTime.isTomorrow(): Boolean {
    val currentDate = this.dateOnly()
    val tomorrow = Instant.now().dateOnly().plusDays(1)
    return currentDate.isEqual(tomorrow)
}

fun LocalDateTime.isPast(): Boolean {
    val currentDate = this.dateOnly()
    val today = Instant.now().dateOnly()
    return currentDate.isBefore(today)
}

fun LocalDateTime.atBangkok(): ZonedDateTime {
    return atZone(ZoneId.of("Asia/Bangkok"))
}

fun ZonedDateTime.dateOnly(): ZonedDateTime {
    val year = year.toString().padStart(4, '0')
    val month = month.value.toString().padStart(2, '0')
    val day = dayOfMonth.toString().padStart(2, '0')
    return LocalDateTime.ofInstant(Instant.parse("$year-$month-${day}T00:00:00.00Z"), ZoneOffset.UTC).atZone(zone)
}

fun ZonedDateTime.toEpochMilli(): Long {
    return toInstant().toEpochMilli()
}

fun Instant.dateOnly(): LocalDateTime {
    return LocalDateTime.ofInstant(this, ZoneId.systemDefault()).dateOnly()
}

fun Instant.isToday(): Boolean {
    val currentDate = this.dateOnly()
    val today = Instant.now().dateOnly()
    return currentDate.isEqual(today)
}

fun Instant.isTomorrow(): Boolean {
    val currentDate = this.dateOnly()
    val tomorrow = Instant.now().dateOnly().plusDays(1)
    return currentDate.isEqual(tomorrow)
}

fun Instant.isPast(): Boolean {
    val currentDate = this.dateOnly()
    val today = Instant.now().dateOnly()
    return currentDate.isBefore(today)
}

fun DateTimeFormatter.atBangkok(): DateTimeFormatter {
    return withLocale(Locale("th")).withZone(ZoneId.of("Asia/Bangkok"))
}
