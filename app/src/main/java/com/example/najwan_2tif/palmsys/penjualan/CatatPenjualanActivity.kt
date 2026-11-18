package com.example.najwan_2tif.palmsys.penjualan

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.najwan_2tif.R
import com.example.najwan_2tif.palmsys.util.BaseActivity
import com.example.najwan_2tif.databinding.ActivityCatatPenjualanBinding
import com.example.najwan_2tif.palmsys.util.LOCALE_ID
import com.example.najwan_2tif.palmsys.util.formatAngka
import com.example.najwan_2tif.palmsys.util.formatDesimal
import com.example.najwan_2tif.palmsys.util.formatRupiah
import com.google.android.material.chip.Chip
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.math.roundToLong

class CatatPenjualanActivity : BaseActivity() {
    private lateinit var binding: ActivityCatatPenjualanBinding

    /** Tanggal panen dalam milidetik UTC (format bawaan MaterialDatePicker). */
    private var tanggalPanen = MaterialDatePicker.todayInUtcMilliseconds()

    /** ML Kit Document Scanner: kamera + luruskan & potong nota otomatis (butuh Google Play services). */
    private val pemindaiNota = GmsDocumentScanning.getClient(
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    )

    /** ML Kit Text Recognition: membaca tulisan di foto nota, berjalan offline di HP. */
    private val pengenalTeks = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val scanNota =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { hasil ->
            if (hasil.resultCode != RESULT_OK) return@registerForActivityResult
            val halaman = GmsDocumentScanningResult.fromActivityResultIntent(hasil.data)
                ?.pages?.firstOrNull() ?: return@registerForActivityResult
            binding.imgNota.setImageURI(halaman.imageUri)
            binding.imgNota.isVisible = true
            bacaNota(InputImage.fromFilePath(this, halaman.imageUri))
        }

    /** Cadangan bila Document Scanner tidak tersedia: foto biasa lalu tetap dibaca OCR. */
    private val ambilFotoNota =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto ->
            if (foto != null) {
                binding.imgNota.setImageBitmap(foto)
                binding.imgNota.isVisible = true
                bacaNota(InputImage.fromBitmap(foto, 0))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        binding = ActivityCatatPenjualanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_kosongkan) {
                isiNilaiAwal()
                true
            } else {
                false
            }
        }

        binding.bannerOffline.isVisible = !adaInternet()
        binding.inputTanggal.setOnClickListener { pilihTanggal() }

        binding.inputBruto.doAfterTextChanged { binding.layoutBruto.error = null; hitung() }
        binding.inputSortasi.doAfterTextChanged { hitung() }
        binding.inputHarga.doAfterTextChanged { binding.layoutHarga.error = null; hitung() }
        binding.inputPembeli.doAfterTextChanged { binding.layoutPembeli.error = null }

        binding.btnTambahFoto.setOnClickListener { mulaiScanNota() }
        binding.btnSimpan.setOnClickListener { simpan() }

        isiNilaiAwal()
    }

    private fun isiNilaiAwal() {
        tanggalPanen = MaterialDatePicker.todayInUtcMilliseconds()
        binding.inputTanggal.setText(formatTanggal(POLA_TANGGAL_PANJANG))
        binding.chipBlok.clearCheck()
        binding.inputBruto.text = null
        binding.layoutBruto.helperText = null
        binding.inputSortasi.setText(SORTASI_DEFAULT)
        binding.inputHarga.setText(HARGA_HARI_INI.toString())
        binding.inputPembeli.setText(PEMBELI_DEFAULT)
        binding.imgNota.setImageDrawable(null)
        binding.imgNota.isVisible = false
        hitung()
    }

    private fun pilihTanggal() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.penjualan_tanggal)
            .setSelection(tanggalPanen)
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .build()
            )
            .build()
        picker.addOnPositiveButtonClickListener { pilihan ->
            tanggalPanen = pilihan
            binding.inputTanggal.setText(formatTanggal(POLA_TANGGAL_PANJANG))
        }
        picker.show(supportFragmentManager, "tanggal_panen")
    }

    private fun mulaiScanNota() {
        pemindaiNota.getStartScanIntent(this)
            .addOnSuccessListener { intentSender ->
                scanNota.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { fotoBiasa() }
    }

    private fun fotoBiasa() {
        try {
            ambilFotoNota.launch(null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.penjualan_foto_gagal, Toast.LENGTH_SHORT).show()
        }
    }

    private fun bacaNota(gambar: InputImage) {
        binding.layoutBruto.helperText = getString(R.string.penjualan_scan_membaca)
        pengenalTeks.process(gambar)
            .addOnSuccessListener { hasil ->
                val berat = PembacaNota.cariBeratBruto(PembacaNota.susunBaris(hasil))
                if (berat == null) {
                    binding.layoutBruto.helperText = null
                    Toast.makeText(this, R.string.penjualan_scan_tidak_terbaca, Toast.LENGTH_LONG).show()
                } else {
                    binding.inputBruto.setText(berat.toString())
                    binding.layoutBruto.helperText = getString(R.string.penjualan_scan_terisi)
                }
            }
            .addOnFailureListener {
                binding.layoutBruto.helperText = null
                Toast.makeText(this, R.string.penjualan_scan_tidak_terbaca, Toast.LENGTH_LONG).show()
            }
    }

    private fun bacaInput() = HitunganPenjualan(
        bruto = binding.inputBruto.text.toString().toLongOrNull() ?: 0,
        sortasiPersen = binding.inputSortasi.text.toString().replace(',', '.')
            .toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0,
        hargaPerKg = binding.inputHarga.text.toString().toLongOrNull() ?: 0
    )

    private fun hitung() {
        val h = bacaInput()
        binding.txtBeratBersihLabel.text =
            getString(R.string.penjualan_berat_bersih, formatDesimal(h.sortasiPersen))
        binding.txtBeratBersih.text = "${formatAngka(h.beratBersih)} kg"
        binding.txtRumus.text =
            getString(R.string.penjualan_rumus, formatAngka(h.beratBersih), formatRupiah(h.hargaPerKg))
        binding.txtSubtotal.text = formatRupiah(h.total)
        binding.txtTotal.text = formatRupiah(h.total)
    }

    private fun simpan() {
        val h = bacaInput()
        val pembeli = binding.inputPembeli.text.toString().trim()
        val blok = binding.chipBlok.checkedChipIds.map { findViewById<Chip>(it).text.toString() }

        var valid = true
        if (h.bruto <= 0) {
            binding.layoutBruto.error = getString(R.string.penjualan_error_bruto)
            valid = false
        }
        if (h.hargaPerKg <= 0) {
            binding.layoutHarga.error = getString(R.string.penjualan_error_harga)
            valid = false
        }
        if (pembeli.isEmpty()) {
            binding.layoutPembeli.error = getString(R.string.penjualan_error_pembeli)
            valid = false
        }
        if (blok.isEmpty()) {
            Toast.makeText(this, R.string.penjualan_error_blok, Toast.LENGTH_SHORT).show()
            valid = false
        }
        if (!valid) return

        val hasil = Intent()
            .putExtra(EXTRA_JUDUL, getString(R.string.penjualan_judul_transaksi, blok.joinToString(", ")))
            .putExtra(EXTRA_KETERANGAN, "${formatTanggal(POLA_TANGGAL_PENDEK)} · ${formatAngka(h.beratBersih)} kg")
            .putExtra(EXTRA_TOTAL, h.total)
        setResult(RESULT_OK, hasil)
        Toast.makeText(this, R.string.penjualan_tersimpan, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun formatTanggal(pola: String): String =
        SimpleDateFormat(pola, LOCALE_ID)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(tanggalPanen))

    override fun onDestroy() {
        pengenalTeks.close()
        super.onDestroy()
    }

    private fun adaInternet(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java)
        val kemampuan = cm.getNetworkCapabilities(cm.activeNetwork)
        return kemampuan?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    companion object {
        const val EXTRA_JUDUL = "extra_judul"
        const val EXTRA_KETERANGAN = "extra_keterangan"
        const val EXTRA_TOTAL = "extra_total"

        const val HARGA_HARI_INI = 3_250L
        const val PEMBELI_DEFAULT = "RAM Api-Api"
        private const val SORTASI_DEFAULT = "2"

        private const val POLA_TANGGAL_PANJANG = "EEEE, d MMMM yyyy"
        private const val POLA_TANGGAL_PENDEK = "d MMM"
    }
}

/** Rumus penjualan: berat bersih = bruto − sortasi, total = berat bersih × harga per kg. */
data class HitunganPenjualan(
    val bruto: Long,
    val sortasiPersen: Double,
    val hargaPerKg: Long
) {
    val beratBersih: Long get() = (bruto * (1 - sortasiPersen / 100)).roundToLong()
    val total: Long get() = beratBersih * hargaPerKg
}
