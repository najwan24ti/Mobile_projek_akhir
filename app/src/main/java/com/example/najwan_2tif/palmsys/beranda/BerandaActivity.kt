package com.example.najwan_2tif.palmsys.beranda

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.najwan_2tif.R
import com.example.najwan_2tif.databinding.ActivityBerandaBinding
import com.example.najwan_2tif.databinding.ItemMenuBerandaBinding
import com.example.najwan_2tif.databinding.ItemTransaksiBinding
import com.example.najwan_2tif.palmsys.penjualan.CatatPenjualanActivity
import com.example.najwan_2tif.palmsys.profil.ProfilActivity
import com.example.najwan_2tif.palmsys.util.formatRupiah
import com.example.najwan_2tif.palmsys.util.formatRupiahBertanda
import java.util.Calendar

class BerandaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBerandaBinding

    private var saldo = 18_740_500L
    private val daftarTransaksi = mutableListOf(
        Transaksi("Penjualan TBS · Blok A", "29 Sep · 3.780 kg", 11_588_000, R.drawable.ic_eco),
        Transaksi("Upah dodos & langsir", "28 Sep · 2 orang", -945_000, R.drawable.ic_groups),
        Transaksi("Beli pupuk NPK", "25 Sep · 6 karung", -1_850_000, R.drawable.ic_receipt)
    )

    private val catatPenjualan =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode != RESULT_OK || data == null) return@registerForActivityResult

            val total = data.getLongExtra(CatatPenjualanActivity.EXTRA_TOTAL, 0)
            daftarTransaksi.add(
                0,
                Transaksi(
                    data.getStringExtra(CatatPenjualanActivity.EXTRA_JUDUL).orEmpty(),
                    data.getStringExtra(CatatPenjualanActivity.EXTRA_KETERANGAN).orEmpty(),
                    total,
                    R.drawable.ic_eco
                )
            )
            saldo += total
            tampilkanSaldo()
            tampilkanTransaksi()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        binding = ActivityBerandaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }

        tampilkanHeader()
        tampilkanSaldo()
        tampilkanMenu()
        tampilkanTransaksi()

        binding.txtHarga.text = formatRupiah(CatatPenjualanActivity.HARGA_HARI_INI)
        binding.txtHargaJudul.text =
            getString(R.string.beranda_harga_judul, CatatPenjualanActivity.PEMBELI_DEFAULT)

        binding.btnNotifikasi.setOnClickListener { segeraHadir(getString(R.string.beranda_notifikasi)) }
        binding.kartuPengingat.setOnClickListener { segeraHadir(getString(R.string.beranda_pengingat_judul)) }
        binding.txtAvatar.setOnClickListener { bukaProfil() }
        binding.txtLihatSemua.setOnClickListener { segeraHadir(getString(R.string.nav_transaksi)) }

        binding.navigasiBawah.selectedItemId = R.id.nav_beranda
        binding.navigasiBawah.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_beranda -> true
                R.id.nav_profil -> {
                    bukaProfil()
                    false
                }
                else -> {
                    segeraHadir(item.title.toString())
                    false
                }
            }
        }
    }

    private val nama by lazy { intent.getStringExtra(EXTRA_USERNAME).orEmpty().ifBlank { "Petani" } }

    private fun tampilkanHeader() {
        binding.txtNama.text = nama
        binding.txtAvatar.text = nama.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

        val jam = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.txtSapaan.setText(
            when (jam) {
                in 4..10 -> R.string.beranda_pagi
                in 11..14 -> R.string.beranda_siang
                in 15..17 -> R.string.beranda_sore
                else -> R.string.beranda_malam
            }
        )
    }

    private fun tampilkanSaldo() {
        binding.txtSaldo.text = formatRupiah(saldo)
    }

    private fun tampilkanMenu() {
        aturMenu(binding.menuCatatPanen, R.drawable.ic_eco, R.string.beranda_menu_catat_panen,
            R.color.tile_green, R.color.tile_green_bg) {
            catatPenjualan.launch(Intent(this, CatatPenjualanActivity::class.java))
        }
        aturMenu(binding.menuPengeluaran, R.drawable.ic_receipt, R.string.beranda_menu_pengeluaran,
            R.color.tile_peach, R.color.tile_peach_bg) {
            segeraHadir(getString(R.string.beranda_menu_pengeluaran))
        }
        aturMenu(binding.menuBlokKebun, R.drawable.ic_park, R.string.beranda_menu_blok_kebun,
            R.color.tile_blue, R.color.tile_blue_bg) {
            segeraHadir(getString(R.string.beranda_menu_blok_kebun))
        }
        aturMenu(binding.menuKalkulator, R.drawable.ic_calculator, R.string.beranda_menu_kalkulator,
            R.color.tile_purple, R.color.tile_purple_bg) {
            segeraHadir(getString(R.string.beranda_menu_kalkulator))
        }
    }

    private fun aturMenu(
        menu: ItemMenuBerandaBinding,
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

    private fun tampilkanTransaksi() {
        binding.daftarTransaksi.removeAllViews()
        daftarTransaksi.take(MAKS_TRANSAKSI).forEach { transaksi ->
            val item = ItemTransaksiBinding.inflate(layoutInflater, binding.daftarTransaksi, true)
            val warna = getColor(if (transaksi.pemasukan) R.color.tile_green else R.color.palm_red)
            val warnaLatar = getColor(if (transaksi.pemasukan) R.color.tile_green_bg else R.color.tile_peach_bg)

            item.imgIkon.setImageResource(transaksi.ikon)
            item.imgIkon.imageTintList = ColorStateList.valueOf(warna)
            item.imgIkon.backgroundTintList = ColorStateList.valueOf(warnaLatar)
            item.txtJudul.text = transaksi.judul
            item.txtKeterangan.text = transaksi.keterangan
            item.txtNominal.text = formatRupiahBertanda(transaksi.nominal)
            item.txtNominal.setTextColor(warna)
        }
    }

    private fun bukaProfil() {
        startActivity(
            Intent(this, ProfilActivity::class.java)
                .putExtra(ProfilActivity.EXTRA_NAMA, nama)
                .putExtra(ProfilActivity.EXTRA_AKUN, intent.getStringExtra(EXTRA_AKUN))
        )
    }

    private fun segeraHadir(fitur: String) {
        Toast.makeText(this, getString(R.string.segera_hadir, fitur), Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_AKUN = "extra_akun"
        private const val MAKS_TRANSAKSI = 4
    }
}
