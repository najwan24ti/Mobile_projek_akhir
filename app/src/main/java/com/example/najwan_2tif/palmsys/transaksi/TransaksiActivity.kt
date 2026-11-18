package com.example.najwan_2tif.palmsys.transaksi

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.najwan_2tif.R
import com.example.najwan_2tif.databinding.ActivityTransaksiBinding
import com.example.najwan_2tif.palmsys.beranda.Transaksi
import com.example.najwan_2tif.palmsys.util.BaseActivity
import com.example.najwan_2tif.palmsys.util.formatRupiah
import com.example.najwan_2tif.palmsys.util.formatRupiahBertanda

class TransaksiActivity : BaseActivity() {
    private lateinit var binding: ActivityTransaksiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        binding = ActivityTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, 0)
            binding.daftarTransaksi.updatePadding(bottom = bars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        @Suppress("UNCHECKED_CAST")
        val daftar = intent.extras
            ?.let { BundleCompat.getSerializable(it, EXTRA_TRANSAKSI, ArrayList::class.java) }
            as? ArrayList<Transaksi> ?: arrayListOf()

        tampilkanRingkasan(daftar)

        // ListView + CustomAdapter
        binding.daftarTransaksi.adapter = TransaksiAdapter(this, daftar)
        binding.daftarTransaksi.emptyView = binding.txtKosong
        binding.daftarTransaksi.setOnItemClickListener { _, _, posisi, _ ->
            val transaksi = daftar[posisi]
            Toast.makeText(
                this, "${transaksi.judul}: ${formatRupiahBertanda(transaksi.nominal)}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun tampilkanRingkasan(daftar: List<Transaksi>) {
        val pemasukan = daftar.filter { it.pemasukan }.sumOf { it.nominal }
        val pengeluaran = -daftar.filter { !it.pemasukan }.sumOf { it.nominal }
        binding.txtPemasukan.text = formatRupiah(pemasukan)
        binding.txtPengeluaran.text = formatRupiah(pengeluaran)
        binding.txtJumlah.text = getString(R.string.transaksi_jumlah, daftar.size)
    }

    companion object {
        const val EXTRA_TRANSAKSI = "extra_transaksi"
    }
}
