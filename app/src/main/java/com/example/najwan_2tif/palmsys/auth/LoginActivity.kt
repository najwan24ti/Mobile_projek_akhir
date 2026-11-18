package com.example.najwan_2tif.palmsys.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.example.najwan_2tif.R
import com.example.najwan_2tif.palmsys.util.BaseActivity
import com.example.najwan_2tif.databinding.ActivityLoginBinding
import com.example.najwan_2tif.palmsys.beranda.BerandaActivity

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        binding = ActivityLoginBinding.inflate(layoutInflater)
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

        val akunTersimpan = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_AKUN, null)
        if (akunTersimpan != null) {
            binding.inputAkun.setText(akunTersimpan)
            binding.cbIngat.isChecked = true
        }

        binding.inputAkun.doAfterTextChanged { binding.layoutAkun.error = null }
        binding.inputPassword.doAfterTextChanged { binding.layoutPassword.error = null }

        binding.inputPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login()
                true
            } else {
                false
            }
        }

        binding.btnMasuk.setOnClickListener { login() }
        binding.txtLupa.setOnClickListener { tampilkanPesan(getString(R.string.login_forgot_info)) }
        binding.btnSidikJari.setOnClickListener {
            tampilkanPesan(getString(R.string.segera_hadir, getString(R.string.login_sidik_jari)))
        }
        binding.txtDaftar.setOnClickListener {
            tampilkanPesan(getString(R.string.segera_hadir, getString(R.string.login_daftar)))
        }
    }

    private fun login() {
        val akun = binding.inputAkun.text.toString().trim()
        val password = binding.inputPassword.text.toString()

        binding.layoutAkun.error =
            if (akun.isEmpty()) getString(R.string.login_error_akun) else null
        binding.layoutPassword.error =
            if (password.isEmpty()) getString(R.string.login_error_password) else null
        if (akun.isEmpty() || password.isEmpty()) return

        getSharedPreferences(PREFS, MODE_PRIVATE).edit {
            if (binding.cbIngat.isChecked) putString(KEY_AKUN, akun) else remove(KEY_AKUN)
        }

        val nama = namaTampilan(akun)
        tampilkanPesan(getString(R.string.login_success, nama))
        startActivity(
            Intent(this, BerandaActivity::class.java)
                .putExtra(BerandaActivity.EXTRA_USERNAME, nama)
                .putExtra(BerandaActivity.EXTRA_AKUN, akun)
        )
        finish()
    }

    /** Email "tarmizi@mail.com" -> "Tarmizi". Nomor HP belum punya nama, jadi pakai "Petani". */
    private fun namaTampilan(akun: String): String =
        if (akun.contains('@')) {
            akun.substringBefore('@').replaceFirstChar { it.uppercase() }
        } else {
            NAMA_DEFAULT
        }

    private fun tampilkanPesan(pesan: String) {
        Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PREFS = "palmsys_login"
        private const val KEY_AKUN = "akun"
        private const val NAMA_DEFAULT = "Petani"
    }
}
