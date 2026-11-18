package com.example.najwan_2tif.palmsys.beranda

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import com.example.najwan_2tif.R
import com.example.najwan_2tif.databinding.ActivityBerandaBinding
import com.example.najwan_2tif.databinding.ItemTransaksiBinding
import com.example.najwan_2tif.palmsys.komponen.LiquidGlassNav
import com.example.najwan_2tif.palmsys.penjualan.CatatPenjualanActivity
import com.example.najwan_2tif.palmsys.profil.ProfilActivity
import com.example.najwan_2tif.palmsys.transaksi.TransaksiActivity
import com.example.najwan_2tif.palmsys.util.BaseActivity
import com.example.najwan_2tif.palmsys.util.formatRupiah
import com.example.najwan_2tif.palmsys.util.formatRupiahBertanda
import java.util.Calendar

class BerandaActivity : BaseActivity() {
    private lateinit var binding: ActivityBerandaBinding

    private var saldo = 18_740_500L
    private val daftarTransaksi = mutableListOf(
        Transaksi("Penjualan TBS · Blok A", "29 Sep · 3.780 kg", 11_588_000, R.drawable.ic_eco),
        Transaksi("Upah dodos & langsir", "28 Sep · 2 orang", -945_000, R.drawable.ic_groups),
        Transaksi("Beli pupuk NPK", "25 Sep · 6 karung", -1_850_000, R.drawable.ic_receipt),
        Transaksi("Penjualan TBS · Blok C", "22 Sep · 2.940 kg", 9_114_000, R.drawable.ic_eco),
        Transaksi("Solar mesin & angkut", "20 Sep · 40 liter", -540_000, R.drawable.ic_receipt),
        Transaksi("Upah semprot gulma", "18 Sep · 3 orang", -1_200_000, R.drawable.ic_groups),
        Transaksi("Penjualan TBS · Blok B", "15 Sep · 3.120 kg", 9_984_000, R.drawable.ic_eco)
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
        // Data dipulihkan kalau halaman digambar ulang (misalnya setelah ganti warna tema)
        if (savedInstanceState != null) {
            saldo = savedInstanceState.getLong(STATE_SALDO, saldo)
            @Suppress("UNCHECKED_CAST")
            (BundleCompat.getSerializable(savedInstanceState, STATE_TRANSAKSI, ArrayList::class.java) as? ArrayList<Transaksi>)
                ?.let { daftarTransaksi.clear(); daftarTransaksi.addAll(it) }
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        binding = ActivityBerandaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jarakNavBawah = (binding.navigasiBawah.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, 0)
            // Nav kaca melayang di atas garis navigasi HP
            binding.navigasiBawah.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = jarakNavBawah + bars.bottom
            }
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
        binding.txtLihatSemua.setOnClickListener { bukaSemuaTransaksi() }

        tampilkanNavigasiBawah()
    }

    /** Navigasi bawah Liquid Glass. Tab selain Beranda membuka halaman lain, jadi gelembung kembali ke Beranda. */
    private fun tampilkanNavigasiBawah() {
        binding.navigasiBawah.aturTab(
            listOf(
                LiquidGlassNav.Tab(R.drawable.ic_home, R.string.nav_beranda),
                LiquidGlassNav.Tab(R.drawable.ic_swap, R.string.nav_transaksi),
                LiquidGlassNav.Tab(R.drawable.ic_park, R.string.nav_kebun),
                LiquidGlassNav.Tab(R.drawable.ic_bar_chart, R.string.nav_laporan),
                LiquidGlassNav.Tab(R.drawable.ic_person, R.string.nav_profil)
            ),
            terpilih = TAB_BERANDA
        )
        // Isi halaman di belakang bar dipakai sebagai "bahan" kaca
        binding.navigasiBawah.sumberLatar = binding.main
        binding.navigasiBawah.onTabDipilih = { tab ->
            when (tab) {
                TAB_BERANDA -> true
                TAB_TRANSAKSI -> {
                    bukaSemuaTransaksi()
                    false
                }
                TAB_PROFIL -> {
                    bukaProfil()
                    false
                }
                TAB_KEBUN -> {
                    segeraHadir(getString(R.string.nav_kebun))
                    false
                }
                else -> {
                    segeraHadir(getString(R.string.nav_laporan))
                    false
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(STATE_SALDO, saldo)
        outState.putSerializable(STATE_TRANSAKSI, ArrayList(daftarTransaksi))
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

    /** Grid menu 4 kolom: RecyclerView + GridLayoutManager + MenuBerandaAdapter. */
    private fun tampilkanMenu() {
        val daftarMenu = listOf(
            MenuBeranda(R.drawable.ic_eco, R.string.beranda_menu_catat_panen,
                R.color.tile_green, R.color.tile_green_bg) {
                catatPenjualan.launch(Intent(this, CatatPenjualanActivity::class.java))
            },
            MenuBeranda(R.drawable.ic_receipt, R.string.beranda_menu_pengeluaran,
                R.color.tile_peach, R.color.tile_peach_bg) { segeraHadir(getString(R.string.beranda_menu_pengeluaran)) },
            MenuBeranda(R.drawable.ic_park, R.string.beranda_menu_blok_kebun,
                R.color.tile_blue, R.color.tile_blue_bg) { segeraHadir(getString(R.string.beranda_menu_blok_kebun)) },
            MenuBeranda(R.drawable.ic_calculator, R.string.beranda_menu_kalkulator,
                R.color.tile_purple, R.color.tile_purple_bg) { segeraHadir(getString(R.string.beranda_menu_kalkulator)) },
            MenuBeranda(R.drawable.ic_calendar, R.string.beranda_menu_jadwal_pupuk,
                R.color.palm_yellow, R.color.palm_yellow_soft) { segeraHadir(getString(R.string.beranda_menu_jadwal_pupuk)) },
            MenuBeranda(R.drawable.ic_groups, R.string.beranda_menu_pekerja,
                R.color.tile_blue, R.color.tile_blue_bg) { segeraHadir(getString(R.string.beranda_menu_pekerja)) },
            MenuBeranda(R.drawable.ic_bar_chart, R.string.beranda_menu_laporan,
                R.color.tile_purple, R.color.tile_purple_bg) { segeraHadir(getString(R.string.beranda_menu_laporan)) },
            MenuBeranda(R.drawable.ic_swap, R.string.beranda_menu_riwayat,
                R.color.tile_green, R.color.tile_green_bg) { bukaSemuaTransaksi() }
        )
        binding.daftarMenu.layoutManager = GridLayoutManager(this, 4)
        binding.daftarMenu.adapter = MenuBerandaAdapter(daftarMenu)
    }

    private fun bukaSemuaTransaksi() {
        startActivity(
            Intent(this, TransaksiActivity::class.java)
                .putExtra(TransaksiActivity.EXTRA_TRANSAKSI, ArrayList(daftarTransaksi))
        )
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
        private const val STATE_SALDO = "state_saldo"
        private const val TAB_BERANDA = 0
        private const val TAB_TRANSAKSI = 1
        private const val TAB_KEBUN = 2
        private const val TAB_PROFIL = 4
        private const val STATE_TRANSAKSI = "state_transaksi"
        private const val MAKS_TRANSAKSI = 4
    }
}
