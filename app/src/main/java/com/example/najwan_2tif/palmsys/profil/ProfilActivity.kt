package com.example.najwan_2tif.palmsys.profil

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.najwan_2tif.R
import com.example.najwan_2tif.databinding.ActivityProfilBinding
import com.example.najwan_2tif.databinding.ItemMenuProfilBinding
import com.example.najwan_2tif.palmsys.auth.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        tampilkanIdentitas()
        tampilkanMenu()

        binding.btnKeluar.setOnClickListener { konfirmasiKeluar() }
    }

    private fun tampilkanIdentitas() {
        val nama = intent.getStringExtra(EXTRA_NAMA).orEmpty().ifBlank { "Petani" }
        binding.txtNama.text = nama
        binding.txtAvatar.text = nama.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

        val akun = intent.getStringExtra(EXTRA_AKUN).orEmpty()
        binding.txtAkun.text = akun
        binding.txtAkun.isVisible = akun.isNotBlank()
    }

    private fun tampilkanMenu() {
        // Akun
        aturMenu(binding.menuEditProfil, R.drawable.ic_edit, R.string.profil_edit,
            R.color.tile_peach, R.color.tile_peach_bg) { segeraHadir(R.string.profil_edit) }
        aturMenu(binding.menuKataSandi, R.drawable.ic_lock, R.string.profil_kata_sandi,
            R.color.tile_purple, R.color.tile_purple_bg) { segeraHadir(R.string.profil_kata_sandi) }
        aturMenu(binding.menuDataKebun, R.drawable.ic_park, R.string.profil_data_kebun,
            R.color.tile_green, R.color.tile_green_bg) { segeraHadir(R.string.profil_data_kebun) }

        // Pengaturan: pilihan disimpan di HP supaya tetap sama saat aplikasi dibuka lagi
        aturSaklar(binding.menuNotifikasi, R.drawable.ic_notifications, R.string.profil_notifikasi,
            R.color.palm_yellow, R.color.palm_yellow_soft, KEY_NOTIFIKASI)
        aturSaklar(binding.menuSinkron, R.drawable.ic_sync, R.string.profil_sinkron,
            R.color.tile_blue, R.color.tile_blue_bg, KEY_SINKRON)

        // Lainnya
        aturMenu(binding.menuBantuan, R.drawable.ic_help, R.string.profil_bantuan,
            R.color.tile_blue, R.color.tile_blue_bg) { segeraHadir(R.string.profil_bantuan) }
        aturMenu(binding.menuTentang, R.drawable.ic_info, R.string.profil_tentang,
            R.color.palm_text_muted, R.color.palm_surface) {}
        binding.menuTentang.txtNilai.text = getString(R.string.profil_versi, versiAplikasi())
        binding.menuTentang.txtNilai.isVisible = true
        binding.menuTentang.imgPanah.isVisible = false
        binding.menuTentang.root.isClickable = false
    }

    private fun aturMenu(
        menu: ItemMenuProfilBinding,
        @DrawableRes ikon: Int,
        @StringRes label: Int,
        @ColorRes warna: Int,
        @ColorRes warnaLatar: Int,
        onClick: () -> Unit
    ) {
        menu.imgIkon.setImageResource(ikon)
        menu.imgIkon.imageTintList = ColorStateList.valueOf(getColor(warna))
        menu.imgIkon.backgroundTintList = ColorStateList.valueOf(getColor(warnaLatar))
        menu.txtLabel.setText(label)
        menu.root.setOnClickListener { onClick() }
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
        aturMenu(menu, ikon, label, warna, warnaLatar) {
            menu.saklar.isChecked = !menu.saklar.isChecked
            prefs.edit { putBoolean(kunci, menu.saklar.isChecked) }
        }
        menu.imgPanah.isVisible = false
        menu.saklar.isVisible = true
        menu.saklar.isChecked = prefs.getBoolean(kunci, true)
    }

    private fun versiAplikasi(): String =
        packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()

    private fun konfirmasiKeluar() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.profil_keluar_judul)
            .setMessage(R.string.profil_keluar_pesan)
            .setNegativeButton(R.string.batal, null)
            .setPositiveButton(R.string.profil_keluar) { _, _ ->
                startActivity(
                    Intent(this, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
            .show()
    }

    private fun segeraHadir(@StringRes fitur: Int) {
        Toast.makeText(this, getString(R.string.segera_hadir, getString(fitur)), Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_NAMA = "extra_nama"
        const val EXTRA_AKUN = "extra_akun"
        private const val PREFS = "palmsys_profil"
        private const val KEY_NOTIFIKASI = "notifikasi"
        private const val KEY_SINKRON = "sinkron"
    }
}
