package com.example.najwan_2tif.palmsys.beranda

import androidx.annotation.DrawableRes
import java.io.Serializable

/** Satu baris di daftar "Transaksi terakhir". Nominal positif = pemasukan, negatif = pengeluaran. */
data class Transaksi(
    val judul: String,
    val keterangan: String,
    val nominal: Long,
    @param:DrawableRes val ikon: Int
) : Serializable {
    val pemasukan: Boolean get() = nominal >= 0
}
