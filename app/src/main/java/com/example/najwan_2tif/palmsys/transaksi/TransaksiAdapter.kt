package com.example.najwan_2tif.palmsys.transaksi

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.najwan_2tif.R
import com.example.najwan_2tif.databinding.ItemTransaksiBinding
import com.example.najwan_2tif.palmsys.beranda.Transaksi
import com.example.najwan_2tif.palmsys.util.formatRupiahBertanda

/**
 * CustomAdapter (turunan BaseAdapter) untuk ListView di halaman Semua Transaksi.
 * Tiap baris memakai layout item_transaksi.xml.
 * Baris yang sudah dibuat dipakai ulang lewat convertView + ViewHolder supaya hemat memori.
 */
class TransaksiAdapter(
    private val context: Context,
    private val daftarTransaksi: List<Transaksi>
) : BaseAdapter() {

    override fun getCount(): Int = daftarTransaksi.size

    override fun getItem(position: Int): Transaksi = daftarTransaksi[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            ItemTransaksiBinding.inflate(LayoutInflater.from(context), parent, false).also {
                it.root.tag = it
            }
        } else {
            convertView.tag as ItemTransaksiBinding
        }

        val transaksi = getItem(position)
        val warna = context.getColor(if (transaksi.pemasukan) R.color.tile_green else R.color.palm_red)
        val warnaLatar = context.getColor(if (transaksi.pemasukan) R.color.tile_green_bg else R.color.tile_peach_bg)

        binding.imgIkon.setImageResource(transaksi.ikon)
        binding.imgIkon.imageTintList = ColorStateList.valueOf(warna)
        binding.imgIkon.backgroundTintList = ColorStateList.valueOf(warnaLatar)
        binding.txtJudul.text = transaksi.judul
        binding.txtKeterangan.text = transaksi.keterangan
        binding.txtNominal.text = formatRupiahBertanda(transaksi.nominal)
        binding.txtNominal.setTextColor(warna)

        return binding.root
    }
}
