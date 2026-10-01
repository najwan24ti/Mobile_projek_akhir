package com.example.najwan_2tif.Pertemuan_3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.najwan_2tif.R
import com.example.najwan_2tif.databinding.ActivityThreeBinding

class ThirdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThreeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityThreeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //val btnKirim = findViewById<Button>(R.id.btnKirim)
        //val inputNoTujuan = findViewById<EditText>(R.id.inputNoTujuan)
        binding.btnKirim.setOnClickListener {
            val intent = Intent(this, ThirdResultActivity::class.java)
            startActivity(intent)

            //val noTujuan = binding.inputNoTujuan.text
            //Toast.makeText(this, "Pesan berhasil dikirim ke $noTujuan", Toast.LENGTH_SHORT).show()

        }


        //btnKirim.setOnClickListener {
            //val noTujuan = inputNoTujuan.text.toString()
            //Toast.makeText(this, "Pesan berhasil dikirim ke $noTujuan", Toast.LENGTH_SHORT).show()
        }
    }


