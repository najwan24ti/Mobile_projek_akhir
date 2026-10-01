package com.example.najwan_2tif.palmsys

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.example.najwan_2tif.Pertemuan_3.ThirdActivity
import com.example.najwan_2tif.R
import com.example.najwan_2tif.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.inputUsername.doAfterTextChanged { binding.layoutUsername.error = null }
        binding.inputPassword.doAfterTextChanged { binding.layoutPassword.error = null }

        binding.inputPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login()
                true
            } else {
                false
            }
        }

        binding.btnLogin.setOnClickListener { login() }

        binding.txtForgot.setOnClickListener {
            Toast.makeText(this, R.string.login_forgot_info, Toast.LENGTH_SHORT).show()
        }
    }

    private fun login() {
        val username = binding.inputUsername.text.toString().trim()
        val password = binding.inputPassword.text.toString()

        binding.layoutUsername.error =
            if (username.isEmpty()) getString(R.string.login_error_username) else null
        binding.layoutPassword.error =
            if (password.isEmpty()) getString(R.string.login_error_password) else null
        if (username.isEmpty() || password.isEmpty()) return

        Toast.makeText(this, getString(R.string.login_success, username), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ThirdActivity::class.java))
    }
}
