package com.example.najwan_2tif.palmsys.auth

import android.content.Context
import androidx.core.content.edit
import org.json.JSONObject
import java.security.MessageDigest

/** Data satu akun petani yang didaftarkan lewat halaman Daftar. */
data class Akun(
    val nama: String,
    val akun: String,        // nomor HP atau email (dipakai untuk login)
    val lokasi: String,
    val luasKebun: String,
    val hashSandi: String
)

/**
 * Menyimpan akun hasil registrasi di HP (SharedPreferences).
 * Kata sandi tidak disimpan apa adanya, tapi diubah dulu jadi hash SHA-256.
 */
object PenyimpananAkun {
    private const val PREFS = "palmsys_akun"

    fun daftar(context: Context, akun: Akun) {
        val json = JSONObject()
            .put("nama", akun.nama)
            .put("akun", akun.akun)
            .put("lokasi", akun.lokasi)
            .put("luas", akun.luasKebun)
            .put("sandi", akun.hashSandi)
        prefs(context).edit { putString(kunci(akun.akun), json.toString()) }
    }

    fun cari(context: Context, akun: String): Akun? {
        val teks = prefs(context).getString(kunci(akun), null) ?: return null
        val json = JSONObject(teks)
        return Akun(
            nama = json.optString("nama"),
            akun = json.optString("akun"),
            lokasi = json.optString("lokasi"),
            luasKebun = json.optString("luas"),
            hashSandi = json.optString("sandi")
        )
    }

    fun sudahTerdaftar(context: Context, akun: String) = prefs(context).contains(kunci(akun))

    fun hash(sandi: String): String =
        MessageDigest.getInstance("SHA-256").digest(sandi.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun kunci(akun: String) = "akun_" + akun.trim().lowercase()

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
