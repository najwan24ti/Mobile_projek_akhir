package com.example.najwan_2tif.palmsys.profil

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.najwan_2tif.R
import com.example.najwan_2tif.databinding.ActivityProfilBinding
import com.example.najwan_2tif.palmsys.auth.LoginActivity
import com.example.najwan_2tif.palmsys.auth.PenyimpananAkun
import com.example.najwan_2tif.palmsys.pengaturan.PengaturanActivity
import com.example.najwan_2tif.palmsys.pengaturan.Tema
import com.example.najwan_2tif.palmsys.util.BaseActivity
import com.example.najwan_2tif.palmsys.util.atur
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfilActivity : BaseActivity() {
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

        // Kalau akun ini dibuat lewat halaman Daftar, tampilkan lokasi & luas kebun dari data pendaftaran
        PenyimpananAkun.cari(this, akun)?.let { terdaftar ->
            binding.txtLokasi.text = terdaftar.lokasi
            binding.txtLuas.text = getString(R.string.profil_luas_format, terdaftar.luasKebun.replace('.', ','))
        }
    }

    private fun tampilkanMenu() {
        // Akun
        binding.menuEditProfil.atur(R.drawable.ic_edit, R.string.profil_edit,
            R.color.tile_peach, R.color.tile_peach_bg) { segeraHadir(R.string.profil_edit) }
        binding.menuKataSandi.atur(R.drawable.ic_lock, R.string.profil_kata_sandi,
            R.color.tile_purple, R.color.tile_purple_bg) { segeraHadir(R.string.profil_kata_sandi) }
        binding.menuDataKebun.atur(R.drawable.ic_park, R.string.profil_data_kebun,
            R.color.tile_green, R.color.tile_green_bg) { segeraHadir(R.string.profil_data_kebun) }

        // Pengaturan: keduanya membuka halaman Pengaturan
        val tema = Tema.aktif(this)
        binding.menuWarna.atur(R.drawable.ic_palette, R.string.profil_warna_tema,
            tema.warna, R.color.palm_surface) { bukaPengaturan() }
        binding.menuWarna.txtNilai.setText(tema.nama)
        binding.menuWarna.txtNilai.isVisible = true
        binding.menuPengaturan.atur(R.drawable.ic_settings, R.string.profil_pengaturan,
            R.color.palm_text_muted, R.color.palm_surface) { bukaPengaturan() }

        // Lainnya
        binding.menuBantuan.atur(R.drawable.ic_help, R.string.profil_bantuan,
            R.color.tile_blue, R.color.tile_blue_bg) { segeraHadir(R.string.profil_bantuan) }
        binding.menuTentang.atur(R.drawable.ic_info, R.string.profil_tentang,
            R.color.palm_text_muted, R.color.palm_surface) {}
        binding.menuTentang.txtNilai.text = getString(R.string.profil_versi, versiAplikasi())
        binding.menuTentang.txtNilai.isVisible = true
        binding.menuTentang.imgPanah.isVisible = false
        binding.menuTentang.root.isClickable = false
    }

    private fun bukaPengaturan() {
        startActivity(Intent(this, PengaturanActivity::class.java))
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
    }
}
