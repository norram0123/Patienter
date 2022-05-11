package com.norram.patienter

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateManager {
    fun getDiffHours(startTime: LocalDateTime, goalTime: LocalDateTime): Int {
        var diffDays = compareLocalDate(startTime.toLocalDate(), goalTime.toLocalDate())

        val diffSeconds: Long
        if(startTime.toLocalTime().isBefore(goalTime.toLocalTime())) {
            diffSeconds = compareLocalTime(startTime.toLocalTime(), goalTime.toLocalTime())
        } else {
            diffDays--
            diffSeconds = compareLocalTime(startTime.toLocalTime(), goalTime.toLocalTime()) + 24*3600
        }
        val half = 1800

        return (diffDays*24 + ((diffSeconds+half) / 3600)).toInt()
    }

    fun compareLocalDate(dateSmall: LocalDate, dateBig: LocalDate): Long {
        var diffDays: Long = 0
        var d1 = dateSmall
        while (d1 != dateBig) {
            d1 = d1.plusDays(1L)
            diffDays++
        }
        return diffDays
    }
    fun compareLocalTime(timeSmall: LocalTime, timeBig: LocalTime): Long {
        val s1 = timeSmall.hour * 3600 + timeSmall.minute * 60 + timeSmall.second
        val s2 = timeBig.hour * 3600 + timeBig.minute * 60 + timeBig.second
        return (s2 -s1).toLong()
    }

    fun getDiffDs(goalTime: LocalDateTime, now: LocalDateTime): Pair<Long, Long> {
        var diffDays = compareLocalDate(now.toLocalDate(), goalTime.toLocalDate())
        var diffSeconds = compareLocalTime(now.toLocalTime(), goalTime.toLocalTime())
        if(goalTime.toLocalTime().isBefore(now.toLocalTime())) {
            diffDays -= 1
            diffSeconds += 3600 * 24
        }
        return Pair(diffDays, diffSeconds)
    }
}

//fun LocalDateTime.isFutureTime(context: Context?, comparedLocalDateTime: LocalDateTime): Boolean {
//    if(this <= comparedLocalDateTime) {
//        Toast.makeText(context, R.string.alert2, Toast.LENGTH_LONG).show()
//        return false
//    }
//    return true
//}
