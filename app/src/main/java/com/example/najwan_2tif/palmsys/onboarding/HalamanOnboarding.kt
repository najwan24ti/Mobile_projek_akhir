package com.example.najwan_2tif.palmsys.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/** Isi satu halaman onboarding. */
data class HalamanOnboarding(
    @param:DrawableRes val ilustrasi: Int,
    @param:StringRes val judul: Int,
    @param:StringRes val deskripsi: Int
)
