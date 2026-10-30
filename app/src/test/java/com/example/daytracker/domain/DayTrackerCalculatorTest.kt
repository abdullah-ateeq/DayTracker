package com.example.daytracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DayTrackerCalculatorTest {

    private val referenceDate = LocalDate.of(2026, 10, 30)

    @Test
    fun `event created today returns 0 days passed`() {
        val eventDate = LocalDate.of(2026, 10, 30)
        val calc = DayTrackerCalculator.calculate(eventDate, referenceDate)

        assertEquals(0L, calc.daysPassed)
        assertTrue(calc.isToday)
        assertFalse(calc.isFuture)
        assertEquals(0L, calc.totalWeeks)
        assertEquals(0L, calc.remainingDaysInWeek)
        assertEquals(0, calc.exactPeriod.years)
        assertEquals(0, calc.exactPeriod.months)
        assertEquals(0, calc.exactPeriod.days)
        // First anniversary is 1 year from now
        assertEquals(LocalDate.of(2027, 10, 30), calc.nextAnniversaryDate)
        assertEquals(365L, calc.daysUntilAnniversary)
        assertEquals(1, calc.nextAnniversaryCount)
    }

    @Test
    fun `event from yesterday returns 1 day passed`() {
        val eventDate = LocalDate.of(2026, 10, 29)
        val calc = DayTrackerCalculator.calculate(eventDate, referenceDate)

        assertEquals(1L, calc.daysPassed)
        assertFalse(calc.isToday)
        assertFalse(calc.isFuture)
        assertEquals(0L, calc.totalWeeks)
        assertEquals(1L, calc.remainingDaysInWeek)
        assertEquals("1 day", calc.formattedExactDuration)
        assertEquals("1 day", calc.formattedWeeksAndDays)
        // Next anniversary is next year
        assertEquals(LocalDate.of(2027, 10, 29), calc.nextAnniversaryDate)
        assertEquals(1, calc.nextAnniversaryCount)
    }

    @Test
    fun `event several weeks and months ago`() {
        // 2026-08-15 to 2026-10-30 = 76 days
        val eventDate = LocalDate.of(2026, 8, 15)
        val calc = DayTrackerCalculator.calculate(eventDate, referenceDate)

        assertEquals(76L, calc.daysPassed)
        assertEquals(10L, calc.totalWeeks) // 76 / 7 = 10 weeks
        assertEquals(6L, calc.remainingDaysInWeek) // 76 % 7 = 6 days
        assertEquals(2L, calc.totalMonths)
        assertEquals("10 weeks, 6 days", calc.formattedWeeksAndDays)
    }

    @Test
    fun `event several years ago with exact period breakdown`() {
        // Event on Oct 15, 2020 relative to Oct 30, 2026
        // Period: 6 years, 0 months, 15 days
        val eventDate = LocalDate.of(2020, 10, 15)
        val calc = DayTrackerCalculator.calculate(eventDate, referenceDate)

        assertEquals(6, calc.exactPeriod.years)
        assertEquals(0, calc.exactPeriod.months)
        assertEquals(15, calc.exactPeriod.days)
        assertEquals("6 years, 15 days", calc.formattedExactDuration)

        // Oct 15, 2026 anniversary has already passed on Oct 15, so next is Oct 15, 2027 (7th anniversary)
        assertEquals(LocalDate.of(2027, 10, 15), calc.nextAnniversaryDate)
        assertEquals(7, calc.nextAnniversaryCount)
        assertEquals("7th", calc.nextAnniversaryOrdinal)
    }

    @Test
    fun `anniversary occurring today`() {
        // Event was exactly 2 years ago today: Oct 30, 2024
        val eventDate = LocalDate.of(2024, 10, 30)
        val calc = DayTrackerCalculator.calculate(eventDate, referenceDate)

        assertTrue(calc.isAnniversaryToday)
        assertEquals(0L, calc.daysUntilAnniversary)
        assertEquals(2, calc.nextAnniversaryCount)
        assertEquals("2nd", calc.nextAnniversaryOrdinal)
    }

    @Test
    fun `leap year event on Feb 29`() {
        // Leap year date: Feb 29, 2024
        // In 2026 (non-leap year), anniversary falls on Feb 28
        // Reference is 2026-10-30, so 2026-02-28 passed, next is 2027-02-28
        val eventDate = LocalDate.of(2024, 2, 29)
        val calc = DayTrackerCalculator.calculate(eventDate, referenceDate)

        assertFalse(calc.isToday)
        assertFalse(calc.isFuture)
        assertEquals(LocalDate.of(2027, 2, 28), calc.nextAnniversaryDate)
        assertEquals(3, calc.nextAnniversaryCount)
        assertEquals("3rd", calc.nextAnniversaryOrdinal)
    }

    @Test
    fun `month-end and year-end boundaries`() {
        // Dec 31, 2025 to Jan 1, 2026
        val dec31 = LocalDate.of(2025, 12, 31)
        val jan1 = LocalDate.of(2026, 1, 1)
        val calcNewYear = DayTrackerCalculator.calculate(dec31, jan1)
        assertEquals(1L, calcNewYear.daysPassed)

        // Feb 28, 2024 (leap year) to Mar 1, 2024 = 2 days (leap day Feb 29 in between)
        val feb28Leap = LocalDate.of(2024, 2, 28)
        val mar1Leap = LocalDate.of(2024, 3, 1)
        val calcLeap = DayTrackerCalculator.calculate(feb28Leap, mar1Leap)
        assertEquals(2L, calcLeap.daysPassed)

        // Feb 28, 2025 (non leap) to Mar 1, 2025 = 1 day
        val feb28NonLeap = LocalDate.of(2025, 2, 28)
        val mar1NonLeap = LocalDate.of(2025, 3, 1)
        val calcNonLeap = DayTrackerCalculator.calculate(feb28NonLeap, mar1NonLeap)
        assertEquals(1L, calcNonLeap.daysPassed)
    }

    @Test
    fun `future date event handling`() {
        // Event date is 10 days in the future
        val futureDate = referenceDate.plusDays(10)
        val calc = DayTrackerCalculator.calculate(futureDate, referenceDate)

        assertTrue(calc.isFuture)
        assertEquals(-10L, calc.daysPassed)
        assertEquals(10L, calc.daysUntilAnniversary)
    }

    @Test
    fun `format statistics summary generates valid text`() {
        val eventDate = LocalDate.of(2020, 1, 1)
        val calc = DayTrackerCalculator.calculate(eventDate, referenceDate)
        val summary = DayTrackerCalculator.formatStatisticsSummary(
            eventName = "Started Learning Android",
            category = "Education",
            description = "My first Android app project",
            calculation = calc,
            dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        )

        assertTrue(summary.contains("Started Learning Android"))
        assertTrue(summary.contains("Education"))
        assertTrue(summary.contains("Jan 01, 2020"))
        assertTrue(summary.contains("My first Android app project"))
    }
}
