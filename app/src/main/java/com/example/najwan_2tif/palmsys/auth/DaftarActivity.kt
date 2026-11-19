package com.example.najwan_2tif.palmsys.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.example.najwan_2tif.R
import com.example.najwan_2tif.databinding.ActivityDaftarBinding
import com.example.najwan_2tif.palmsys.util.BaseActivity
import com.google.android.material.textfield.TextInputLayout

/** Halaman registrasi akun baru. Setelah berhasil, kembali ke Login dengan akun sudah terisi. */
class DaftarActivity : BaseActivity() {
    private lateinit var binding: ActivityDaftarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        binding = ActivityDaftarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val headerPaddingTop = binding.header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, 0, bars.right, bars.bottom)
            binding.header.setPadding(
                binding.header.paddingLeft,
                headerPaddingTop + bars.top,
                binding.header.paddingRight,
                binding.header.paddingBottom
            )
            insets
        }

        // Pesan error hilang begitu user mulai mengetik ulang
        listOf(
            binding.layoutNama, binding.layoutAkun, binding.layoutLokasi,
            binding.layoutLuas, binding.layoutSandi, binding.layoutUlangSandi
        ).forEach { layout -> layout.editText?.doAfterTextChanged { layout.error = null } }

        binding.inputUlangSandi.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                daftar()
                true
            } else {
                false
            }
        }

        binding.btnKembali.setOnClickListener { finish() }
        binding.txtMasuk.setOnClickListener { finish() }
        binding.btnDaftar.setOnClickListener { daftar() }
    }

    private fun daftar() {
        val nama = binding.inputNama.text.toString().trim()
        val akun = binding.inputAkun.text.toString().trim()
        val lokasi = binding.inputLokasi.text.toString().trim()
        val luas = binding.inputLuas.text.toString().trim().replace(',', '.')
        val sandi = binding.inputSandi.text.toString()
        val ulangSandi = binding.inputUlangSandi.text.toString()

        // Periksa semua isian; setiap kolom yang salah diberi pesan error masing-masing
        var valid = true
        fun cek(layout: TextInputLayout, benar: Boolean, pesan: Int) {
            layout.error = if (benar) null else getString(pesan)
            if (!benar) valid = false
        }
        cek(binding.layoutNama, nama.length >= 3, R.string.daftar_error_nama)
        cek(binding.layoutAkun, akunValid(akun), R.string.daftar_error_akun)
        if (akunValid(akun)) {
            cek(binding.layoutAkun, !PenyimpananAkun.sudahTerdaftar(this, akun), R.string.daftar_error_akun_terdaftar)
        }
        cek(binding.layoutLokasi, lokasi.isNotEmpty(), R.string.daftar_error_lokasi)
        cek(binding.layoutLuas, (luas.toDoubleOrNull() ?: 0.0) > 0.0, R.string.daftar_error_luas)
        cek(binding.layoutSandi, sandi.length >= 6, R.string.daftar_error_sandi)
        cek(binding.layoutUlangSandi, ulangSandi == sandi && ulangSandi.isNotEmpty(), R.string.daftar_error_ulang_sandi)
        if (!valid) return

        if (!binding.cbSetuju.isChecked) {
            Toast.makeText(this, R.string.daftar_error_setuju, Toast.LENGTH_SHORT).show()
            return
        }

        PenyimpananAkun.daftar(
            this,
            Akun(nama, akun, lokasi, luas, PenyimpananAkun.hash(sandi))
        )
        Toast.makeText(this, R.string.daftar_berhasil, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK, Intent().putExtra(EXTRA_AKUN, akun))
        finish()
    }

    /** Email yang benar, atau nomor HP Indonesia (08… / 628… / +628…, 10–14 digit). */
    private fun akunValid(akun: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(akun).matches() ||
            Regex("^(\\+62|62|0)8[0-9]{8,12}$").matches(akun)

    companion object {
        const val EXTRA_AKUN = "extra_akun"
    }
}
