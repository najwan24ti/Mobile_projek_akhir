package com.example.najwan_2tif.palmsys.beranda

import androidx.annotation.DrawableRes

/** Satu baris di daftar "Transaksi terakhir". Nominal positif = pemasukan, negatif = pengeluaran. */
data class Transaksi(
    val judul: String,
    val keterangan: String,
    val nominal: Long,
    @param:DrawableRes val ikon: Int
) {
    val pemasukan: Boolean get() = nominal >= 0
}
