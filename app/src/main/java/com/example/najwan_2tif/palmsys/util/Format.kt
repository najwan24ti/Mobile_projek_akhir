package com.example.najwan_2tif.palmsys.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

val LOCALE_ID: Locale = Locale.forLanguageTag("id-ID")

private val angkaFormat = NumberFormat.getIntegerInstance(LOCALE_ID)
private val desimalFormat = NumberFormat.getNumberInstance(LOCALE_ID).apply {
    maximumFractionDigits = 1
}

/** 2587 -> "2.587" */
fun formatAngka(nilai: Long): String = angkaFormat.format(nilai)

/** 2.5 -> "2,5" */
fun formatDesimal(nilai: Double): String = desimalFormat.format(nilai)

/** 8407750 -> "Rp 8.407.750" */
fun formatRupiah(nilai: Long): String = "Rp " + angkaFormat.format(nilai)

/** -945000 -> "-Rp 945.000", 11588000 -> "+Rp 11.588.000" */
fun formatRupiahBertanda(nilai: Long): String =
    (if (nilai < 0) "-" else "+") + formatRupiah(abs(nilai))
