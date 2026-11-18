package com.example.najwan_2tif.palmsys.onboarding

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.najwan_2tif.R
import com.example.najwan_2tif.databinding.ActivityOnboardingBinding
import com.example.najwan_2tif.palmsys.auth.LoginActivity
import com.example.najwan_2tif.palmsys.util.BaseActivity

/**
 * Layar pengenalan PalmSys yang muncul saat aplikasi pertama kali dibuka.
 * Setelah selesai/dilewati, berikutnya aplikasi langsung membuka Login.
 */
class OnboardingActivity : BaseActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    private val daftarHalaman = listOf(
        HalamanOnboarding(R.drawable.ic_eco, R.string.onboarding_1_judul, R.string.onboarding_1_deskripsi),
        HalamanOnboarding(R.drawable.ic_camera, R.string.onboarding_2_judul, R.string.onboarding_2_deskripsi),
        HalamanOnboarding(R.drawable.ic_bar_chart, R.string.onboarding_3_judul, R.string.onboarding_3_deskripsi)
    )

    /** true = dibuka lagi dari Pengaturan, jadi setelah selesai cukup kembali ke Pengaturan. */
    private val modeLihatLagi by lazy { intent.getBooleanExtra(EXTRA_LIHAT_LAGI, false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!modeLihatLagi && sudahDilihat(this)) {
            bukaLogin()
            return
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.pager.adapter = OnboardingAdapter(daftarHalaman)
        buatIndikator()
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = perbaruiHalaman(position)
        })

        binding.btnLewati.setOnClickListener { selesai() }
        binding.btnLanjut.setOnClickListener {
            val berikutnya = binding.pager.currentItem + 1
            if (berikutnya < daftarHalaman.size) binding.pager.currentItem = berikutnya else selesai()
        }
    }

    private fun buatIndikator() {
        repeat(daftarHalaman.size) {
            val titik = View(this).apply {
                setBackgroundResource(R.drawable.bg_titik_indikator)
                layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
            }
            binding.indikator.addView(titik)
        }
        perbaruiHalaman(0)
    }

    /** Titik halaman aktif dibuat panjang & berwarna tema; tombol terakhir jadi "Mulai". */
    private fun perbaruiHalaman(posisi: Int) {
        val warnaAktif = warnaTema(R.attr.warnaUtamaGelap)
        val warnaPasif = getColor(R.color.palm_line)
        for (i in 0 until binding.indikator.childCount) {
            val titik = binding.indikator.getChildAt(i)
            titik.layoutParams = (titik.layoutParams as LinearLayout.LayoutParams).apply {
                width = if (i == posisi) dp(24) else dp(8)
            }
            titik.backgroundTintList = ColorStateList.valueOf(if (i == posisi) warnaAktif else warnaPasif)
        }
        val halamanTerakhir = posisi == daftarHalaman.lastIndex
        binding.btnLanjut.setText(if (halamanTerakhir) R.string.onboarding_mulai else R.string.onboarding_lanjut)
        binding.btnLewati.visibility = if (halamanTerakhir) View.INVISIBLE else View.VISIBLE
    }

    private fun selesai() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit { putBoolean(KEY_SUDAH_DILIHAT, true) }
        if (modeLihatLagi) finish() else bukaLogin()
    }

    private fun bukaLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun warnaTema(attr: Int): Int {
        val nilai = TypedValue()
        theme.resolveAttribute(attr, nilai, true)
        return nilai.data
    }

    private fun dp(nilai: Int): Int = (nilai * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_LIHAT_LAGI = "extra_lihat_lagi"
        private const val PREFS = "palmsys_onboarding"
        private const val KEY_SUDAH_DILIHAT = "sudah_dilihat"

        fun sudahDilihat(context: Context): Boolean =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_SUDAH_DILIHAT, false)
    }
}
