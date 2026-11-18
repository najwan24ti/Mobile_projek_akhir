package com.example.najwan_2tif.palmsys.penjualan

import com.google.mlkit.vision.text.Text
import kotlin.math.abs

/**
 * Membaca berat bruto dari teks nota timbangan hasil OCR (ML Kit Text Recognition).
 *
 * Contoh isi nota:  "BRUTO : 2.640 kg"  /  "Timbang 1   2,640"  /  "Gross 2640 KG"
 */
object PembacaNota {
    private val KATA_BRUTO = listOf("bruto", "brutto", "gross", "kotor", "timbang 1", "timbangan 1")

    /** Angka dengan pemisah ribuan (2.640 / 2,640) atau angka biasa (2640). */
    private const val POLA_ANGKA = """\d{1,3}(?:[.,]\d{3})+|\d+"""
    private val ANGKA = Regex(POLA_ANGKA)
    private val ANGKA_KG = Regex("""($POLA_ANGKA)\s*kg""", RegexOption.IGNORE_CASE)

    /** Berat TBS yang masuk akal untuk satu kali jual (kg). */
    private val BATAS_BERAT = 10L..100_000L

    fun cariBeratBruto(teks: String): Long? {
        val baris = teks.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // 1. Cari baris berlabel "bruto", ambil angka di baris itu atau baris sesudahnya
        baris.forEachIndexed { i, isi ->
            if (KATA_BRUTO.any { isi.contains(it, ignoreCase = true) }) {
                angkaBerat(isi.hapusKataBruto())?.let { return it }
                baris.getOrNull(i + 1)?.let { angkaBerat(it) }?.let { return it }
            }
        }

        // 2. Tidak ada label: pakai angka "... kg" terbesar (bruto selalu paling berat)
        return ANGKA_KG.findAll(teks)
            .mapNotNull { keAngka(it.groupValues[1]) }
            .filter { it in BATAS_BERAT }
            .maxOrNull()
    }

    /**
     * ML Kit kadang memisah label & nilai jadi blok berbeda ("Bruto" kiri, "2.640 kg" kanan).
     * Fungsi ini menyusun ulang baris berdasarkan posisi vertikal supaya label & nilai satu baris lagi.
     */
    fun susunBaris(hasil: Text): String {
        val semuaBaris = hasil.textBlocks.flatMap { it.lines }
            .filter { it.boundingBox != null }
            .sortedBy { it.boundingBox!!.centerY() }

        val kelompok = mutableListOf<MutableList<Text.Line>>()
        for (line in semuaBaris) {
            val kotak = line.boundingBox!!
            val terakhir = kelompok.lastOrNull()?.last()?.boundingBox
            if (terakhir != null && abs(kotak.centerY() - terakhir.centerY()) < kotak.height() * 0.6) {
                kelompok.last().add(line)
            } else {
                kelompok.add(mutableListOf(line))
            }
        }
        return kelompok.joinToString("\n") { baris ->
            baris.sortedBy { it.boundingBox!!.left }.joinToString("  ") { it.text }
        }
    }

    private fun angkaBerat(isi: String): Long? =
        ANGKA.findAll(isi).mapNotNull { keAngka(it.value) }.firstOrNull { it in BATAS_BERAT }

    private fun keAngka(teks: String): Long? = teks.replace(".", "").replace(",", "").toLongOrNull()

    /** Buang "timbang 1" dsb. supaya angka "1" di labelnya tidak ikut terbaca. */
    private fun String.hapusKataBruto(): String =
        KATA_BRUTO.fold(this) { isi, kata -> isi.replace(kata, " ", ignoreCase = true) }
}
