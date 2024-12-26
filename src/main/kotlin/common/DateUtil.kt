package ru.bashcony.common

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 2016-12-27T08:15:05.674+01:00
fun String.toDateTime() =
    OffsetDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)

fun OffsetDateTime.toDateString() =
    DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this)

fun currentDateTime(): OffsetDateTime =
    OffsetDateTime.now(ZoneId.of("Europe/Moscow"))