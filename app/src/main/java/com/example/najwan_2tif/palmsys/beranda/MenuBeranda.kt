package com.example.najwan_2tif.palmsys.beranda

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/** Satu kotak menu di grid Beranda. */
data class MenuBeranda(
    @param:DrawableRes val ikon: Int,
    @param:StringRes val label: Int,
    @param:ColorRes val warna: Int,
    @param:ColorRes val warnaLatar: Int,
    val onClick: () -> Unit
)
