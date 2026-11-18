package com.example.najwan_2tif.palmsys.util

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.najwan_2tif.palmsys.pengaturan.Tema

/**
 * Induk semua halaman PalmSys: memasang warna tema pilihan user.
 * Kalau tema diganti di Pengaturan, halaman yang masih terbuka di belakang
 * akan digambar ulang saat dibuka kembali supaya warnanya ikut berubah.
 */
open class BaseActivity : AppCompatActivity() {
    private lateinit var temaTerpasang: Tema

    override fun onCreate(savedInstanceState: Bundle?) {
        temaTerpasang = Tema.aktif(this)
        setTheme(temaTerpasang.style)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (Tema.aktif(this) != temaTerpasang) recreate()
    }
}
