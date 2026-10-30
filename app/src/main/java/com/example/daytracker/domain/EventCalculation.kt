package com.example.daytracker.domain

import java.time.LocalDate
import java.time.Period

/**
 * Encapsulates all calculated metrics for an event relative to a target reference date (typically today).
 */
data class EventCalculation(
    val eventDate: LocalDate,
    val referenceDate: LocalDate,
    val daysPassed: Long,
    val isToday: Boolean,
    val isFuture: Boolean,
    val totalWeeks: Long,
    val remainingDaysInWeek: Long,
    val totalMonths: Long,
    val totalYears: Long,
    val exactPeriod: Period, // years, months, days
    val nextAnniversaryDate: LocalDate,
    val daysUntilAnniversary: Long,
    val nextAnniversaryCount: Int,
    val isAnniversaryToday: Boolean
) {
    /**
     * Readable duration text e.g., "2 years, 3 months, 12 days"
     */
    val formattedExactDuration: String
        get() {
            if (isToday) return "Today"
            if (isFuture) return "In ${kotlin.math.abs(daysPassed)} day(s)"

            val parts = mutableListOf<String>()
            if (exactPeriod.years > 0) {
                parts.add("${exactPeriod.years} ${if (exactPeriod.years == 1) "year" else "years"}")
            }
            if (exactPeriod.months > 0) {
                parts.add("${exactPeriod.months} ${if (exactPeriod.months == 1) "month" else "months"}")
            }
            if (exactPeriod.days > 0 || parts.isEmpty()) {
                parts.add("${exactPeriod.days} ${if (exactPeriod.days == 1) "day" else "days"}")
            }
            return parts.joinToString(", ")
        }

    /**
     * Weeks and days formatted: e.g. "12 weeks, 3 days"
     */
    val formattedWeeksAndDays: String
        get() {
            if (isToday) return "0 weeks, 0 days"
            val weeksPart = "$totalWeeks ${if (totalWeeks == 1L) "week" else "weeks"}"
            val daysPart = "$remainingDaysInWeek ${if (remainingDaysInWeek == 1L) "day" else "days"}"
            return if (totalWeeks > 0) "$weeksPart, $daysPart" else daysPart
        }

    /**
     * Suffix for ordinal numbers (1st, 2nd, 3rd, 4th, etc.)
     */
    val nextAnniversaryOrdinal: String
        get() = getOrdinal(nextAnniversaryCount)

    companion object {
        fun getOrdinal(n: Int): String {
            if (n <= 0) return ""
            val remainder100 = n % 100
            val remainder10 = n % 10
            val suffix = if (remainder100 in 11..13) {
                "th"
            } else {
                when (remainder10) {
                    1 -> "st"
                    2 -> "nd"
                    3 -> "rd"
                    else -> "th"
                }
            }
            return "$n$suffix"
        }
    }
}
