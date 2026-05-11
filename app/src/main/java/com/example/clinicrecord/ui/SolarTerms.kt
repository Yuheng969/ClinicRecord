package com.example.clinicrecord.ui

import java.time.LocalDate
import java.time.LocalDateTime

private data class SolarTermRule(
    val name: String,
    val month: Int,
    val coefficient: Double
)

private val solarTermRules = listOf(
    SolarTermRule("小寒", 1, 5.4055),
    SolarTermRule("大寒", 1, 20.12),
    SolarTermRule("立春", 2, 3.87),
    SolarTermRule("雨水", 2, 18.74),
    SolarTermRule("惊蛰", 3, 5.63),
    SolarTermRule("春分", 3, 20.646),
    SolarTermRule("清明", 4, 4.81),
    SolarTermRule("谷雨", 4, 20.1),
    SolarTermRule("立夏", 5, 5.52),
    SolarTermRule("小满", 5, 21.04),
    SolarTermRule("芒种", 6, 5.678),
    SolarTermRule("夏至", 6, 21.37),
    SolarTermRule("小暑", 7, 7.108),
    SolarTermRule("大暑", 7, 22.83),
    SolarTermRule("立秋", 8, 7.5),
    SolarTermRule("处暑", 8, 23.13),
    SolarTermRule("白露", 9, 7.646),
    SolarTermRule("秋分", 9, 23.042),
    SolarTermRule("寒露", 10, 8.318),
    SolarTermRule("霜降", 10, 23.438),
    SolarTermRule("立冬", 11, 7.438),
    SolarTermRule("小雪", 11, 22.36),
    SolarTermRule("大雪", 12, 7.18),
    SolarTermRule("冬至", 12, 21.94)
)

fun solarTermForVisitDate(dateTime: LocalDateTime): String {
    val date = dateTime.toLocalDate()
    return solarTermDates(date.year)
        .lastOrNull { it.first <= date }
        ?.second
        ?: "冬至"
}

private fun solarTermDates(year: Int): List<Pair<LocalDate, String>> {
    val shortYear = year % 100
    return solarTermRules.map { rule ->
        val day = (shortYear * 0.2422 + rule.coefficient).toInt() - ((shortYear - 1) / 4)
        LocalDate.of(year, rule.month, day.coerceIn(1, 28)) to rule.name
    }.sortedBy { it.first }
}
