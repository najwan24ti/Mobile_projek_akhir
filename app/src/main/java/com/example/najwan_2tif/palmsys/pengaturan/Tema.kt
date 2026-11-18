package com.example.najwan_2tif.palmsys.pengaturan

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.content.edit
import com.example.najwan_2tif.R

/** Pilihan warna tema. Tiap warna punya style sendiri di res/values/themes.xml. */
enum class Tema(@StyleRes val style: Int, @StringRes val nama: Int, @ColorRes val warna: Int) {
    ORANYE(R.style.Theme_PalmSys, R.string.tema_oranye, R.color.palm_orange),
    MERAH(R.style.Theme_PalmSys_Merah, R.string.tema_merah, R.color.tema_merah),
    UNGU(R.style.Theme_PalmSys_Ungu, R.string.tema_ungu, R.color.tema_ungu),
    HIJAU(R.style.Theme_PalmSys_Hijau, R.string.tema_hijau, R.color.tema_hijau),
    BIRU(R.style.Theme_PalmSys_Biru, R.string.tema_biru, R.color.tema_biru);

    companion object {
        private const val PREFS = "palmsys_pengaturan"
        private const val KEY_TEMA = "tema"

        /** Tema yang sedang dipilih user (bawaan: oranye). */
        fun aktif(context: Context): Tema {
            val nama = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_TEMA, null)
            return entries.firstOrNull { it.name == nama } ?: ORANYE
        }

        fun simpan(context: Context, tema: Tema) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
                putString(KEY_TEMA, tema.name)
            }
        }
    }
}
