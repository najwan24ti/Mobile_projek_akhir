package com.example.najwan_2tif.palmsys.pengaturan

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.najwan_2tif.R
import com.example.najwan_2tif.databinding.ActivityPengaturanBinding
import com.example.najwan_2tif.databinding.ItemMenuProfilBinding
import com.example.najwan_2tif.databinding.ItemPilihanWarnaBinding
import com.example.najwan_2tif.palmsys.util.BaseActivity
import com.example.najwan_2tif.palmsys.util.atur

class PengaturanActivity : BaseActivity() {
    private lateinit var binding: ActivityPengaturanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        binding = ActivityPengaturanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        tampilkanPilihanWarna()

        // Pilihan disimpan di HP supaya tetap sama saat aplikasi dibuka lagi
        aturSaklar(binding.menuNotifikasi, R.drawable.ic_notifications, R.string.pengaturan_notifikasi,
            R.color.palm_yellow, R.color.palm_yellow_soft, KEY_NOTIFIKASI)
        aturSaklar(binding.menuSinkron, R.drawable.ic_sync, R.string.pengaturan_sinkron,
            R.color.tile_blue, R.color.tile_blue_bg, KEY_SINKRON)
    }

    private fun tampilkanPilihanWarna() {
        val temaAktif = Tema.aktif(this)
        binding.txtTemaAktif.setText(temaAktif.nama)

        Tema.entries.forEach { tema ->
            val item = ItemPilihanWarnaBinding.inflate(layoutInflater, binding.daftarWarna, true)
            val terpilih = tema == temaAktif

            item.lingkaran.backgroundTintList = ColorStateList.valueOf(getColor(tema.warna))
            item.cincin.setBackgroundResource(if (terpilih) R.drawable.bg_lingkaran_terpilih else 0)
            item.imgCentang.isVisible = terpilih
            item.txtNama.setText(tema.nama)
            item.txtNama.setTextColor(getColor(if (terpilih) R.color.palm_text else R.color.palm_text_muted))
            item.txtNama.setTypeface(null, if (terpilih) Typeface.BOLD else Typeface.NORMAL)
            item.root.contentDescription = getString(tema.nama)

            item.root.setOnClickListener {
                if (terpilih) return@setOnClickListener
                Tema.simpan(this, tema)
                Toast.makeText(
                    this, getString(R.string.pengaturan_tema_diganti, getString(tema.nama)),
                    Toast.LENGTH_SHORT
                ).show()
                // Gambar ulang halaman ini dengan tema baru; halaman lain menyusul saat dibuka lagi
                recreate()
            }
        }
    }

    private fun aturSaklar(
        menu: ItemMenuProfilBinding,
        @DrawableRes ikon: Int,
        @StringRes label: Int,
        @ColorRes warna: Int,
        @ColorRes warnaLatar: Int,
        kunci: String
    ) {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        menu.atur(ikon, label, warna, warnaLatar) {
            menu.saklar.isChecked = !menu.saklar.isChecked
            prefs.edit { putBoolean(kunci, menu.saklar.isChecked) }
        }
        menu.imgPanah.isVisible = false
        menu.saklar.isVisible = true
        menu.saklar.isChecked = prefs.getBoolean(kunci, true)
    }

    companion object {
        private const val PREFS = "palmsys_pengaturan"
        private const val KEY_NOTIFIKASI = "notifikasi"
        private const val KEY_SINKRON = "sinkron"
    }
}
