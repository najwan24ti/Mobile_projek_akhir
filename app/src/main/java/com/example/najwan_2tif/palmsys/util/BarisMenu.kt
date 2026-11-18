package com.example.najwan_2tif.palmsys.util

import android.content.res.ColorStateList
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.najwan_2tif.databinding.ItemMenuProfilBinding

/** Mengisi satu baris menu (layout item_menu_profil): ikon berwarna, label, dan aksi saat ditekan. */
fun ItemMenuProfilBinding.atur(
    @DrawableRes ikon: Int,
    @StringRes label: Int,
    @ColorRes warna: Int,
    @ColorRes warnaLatar: Int,
    onClick: () -> Unit
) {
    val context = root.context
    imgIkon.setImageResource(ikon)
    imgIkon.imageTintList = ColorStateList.valueOf(context.getColor(warna))
    imgIkon.backgroundTintList = ColorStateList.valueOf(context.getColor(warnaLatar))
    txtLabel.setText(label)
    root.setOnClickListener { onClick() }
}
