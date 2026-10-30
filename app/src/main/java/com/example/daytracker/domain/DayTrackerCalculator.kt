package com.example.daytracker.domain

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * Reusable and pure calculator engine for day tracking and anniversary calculations.
 */
object DayTrackerCalculator {

    /**
     * Calculates all metrics for an event date relative to referenceDate (defaults to today).
     */
    fun calculate(eventDate: LocalDate, referenceDate: LocalDate = LocalDate.now()): EventCalculation {
        val daysPassed = ChronoUnit.DAYS.between(eventDate, referenceDate)
        val isToday = eventDate.isEqual(referenceDate)
        val isFuture = eventDate.isAfter(referenceDate)

        val totalWeeks = if (daysPassed >= 0) daysPassed / 7 else -(abs(daysPassed) / 7)
        val remainingDaysInWeek = if (daysPassed >= 0) daysPassed % 7 else -(abs(daysPassed) % 7)

        val totalMonths = ChronoUnit.MONTHS.between(eventDate, referenceDate)
        val totalYears = ChronoUnit.YEARS.between(eventDate, referenceDate)

        val exactPeriod = if (isFuture) {
            Period.between(referenceDate, eventDate)
        } else {
            Period.between(eventDate, referenceDate)
        }

        // Anniversary calculation
        val nextAnniversaryInfo = calculateNextAnniversary(eventDate, referenceDate)

        return EventCalculation(
            eventDate = eventDate,
            referenceDate = referenceDate,
            daysPassed = daysPassed,
            isToday = isToday,
            isFuture = isFuture,
            totalWeeks = totalWeeks,
            remainingDaysInWeek = remainingDaysInWeek,
            totalMonths = totalMonths,
            totalYears = totalYears,
            exactPeriod = exactPeriod,
            nextAnniversaryDate = nextAnniversaryInfo.date,
            daysUntilAnniversary = nextAnniversaryInfo.daysUntil,
            nextAnniversaryCount = nextAnniversaryInfo.count,
            isAnniversaryToday = nextAnniversaryInfo.isToday
        )
    }

    private data class AnniversaryInfo(
        val date: LocalDate,
        val daysUntil: Long,
        val count: Int,
        val isToday: Boolean
    )

    private fun calculateNextAnniversary(eventDate: LocalDate, referenceDate: LocalDate): AnniversaryInfo {
        if (eventDate.isAfter(referenceDate)) {
            // Future event: next milestone is the event itself
            val daysUntil = ChronoUnit.DAYS.between(referenceDate, eventDate)
            return AnniversaryInfo(
                date = eventDate,
                daysUntil = daysUntil,
                count = 0,
                isToday = false
            )
        }

        if (eventDate.isEqual(referenceDate)) {
            // Event is today: 1st anniversary is next year
            val nextDate = eventDate.plusYears(1)
            val daysUntil = ChronoUnit.DAYS.between(referenceDate, nextDate)
            return AnniversaryInfo(
                date = nextDate,
                daysUntil = daysUntil,
                count = 1,
                isToday = false
            )
        }

        val yearsPassed = referenceDate.year - eventDate.year
        // plusYears automatically handles Feb 29 by setting to Feb 28 on non-leap years
        val anniversaryThisYear = eventDate.plusYears(yearsPassed.toLong())

        return when {
            anniversaryThisYear.isEqual(referenceDate) -> {
                AnniversaryInfo(
                    date = anniversaryThisYear,
                    daysUntil = 0,
                    count = yearsPassed,
                    isToday = true
                )
            }
            anniversaryThisYear.isAfter(referenceDate) -> {
                val daysUntil = ChronoUnit.DAYS.between(referenceDate, anniversaryThisYear)
                AnniversaryInfo(
                    date = anniversaryThisYear,
                    daysUntil = daysUntil,
                    count = yearsPassed,
                    isToday = false
                )
            }
            else -> {
                val nextAnniversary = eventDate.plusYears((yearsPassed + 1).toLong())
                val daysUntil = ChronoUnit.DAYS.between(referenceDate, nextAnniversary)
                AnniversaryInfo(
                    date = nextAnniversary,
                    daysUntil = daysUntil,
                    count = yearsPassed + 1,
                    isToday = false
                )
            }
        }
    }

    /**
     * Formats statistics as clean shareable / copyable text.
     */
    fun formatStatisticsSummary(
        eventName: String,
        category: String,
        description: String?,
        calculation: EventCalculation,
        dateFormatter: DateTimeFormatter
    ): String {
        val sb = StringBuilder()
        sb.appendLine("🗓️ DayTracker: $eventName")
        sb.appendLine("📁 Category: $category")
        sb.appendLine("📅 Event Date: ${calculation.eventDate.format(dateFormatter)}")
        sb.appendLine("─".repeat(24))

        if (calculation.isToday) {
            sb.appendLine("⏱️ Status: Occurring today (0 days passed)")
        } else if (calculation.isFuture) {
            sb.appendLine("⏳ Status: In the future (${abs(calculation.daysPassed)} days remaining)")
        } else {
            sb.appendLine("⏱️ Days Passed: ${calculation.daysPassed} days")
            sb.appendLine("📊 Weeks & Days: ${calculation.formattedWeeksAndDays}")
            sb.appendLine("⌛ Exact Duration: ${calculation.formattedExactDuration}")
            if (calculation.totalMonths > 0) {
                sb.appendLine("🗓️ Total Months: ${calculation.totalMonths} months")
            }
        }

        if (calculation.isAnniversaryToday) {
            sb.appendLine("🎉 Today is the ${calculation.nextAnniversaryOrdinal} Anniversary!")
        } else if (!calculation.isFuture) {
            val countStr = if (calculation.nextAnniversaryCount > 0) " (${calculation.nextAnniversaryOrdinal})" else ""
            sb.appendLine("🎂 Next Anniversary$countStr: ${calculation.nextAnniversaryDate.format(dateFormatter)} (in ${calculation.daysUntilAnniversary} days)")
        }

        if (!description.isNullOrBlank()) {
            sb.appendLine("─".repeat(24))
            sb.appendLine("📝 Notes: $description")
        }

        sb.appendLine("\nTracked with DayTracker (Offline Event Counter)")
        return sb.toString().trimEnd()
    }
}
