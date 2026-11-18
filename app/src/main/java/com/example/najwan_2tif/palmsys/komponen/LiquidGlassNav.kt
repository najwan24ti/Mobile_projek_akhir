package com.example.najwan_2tif.palmsys.komponen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.example.najwan_2tif.R
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Navigasi bawah bergaya "Liquid Glass" (seperti iPhone).
 *
 * Cara kerjanya:
 * 1. Isi halaman di belakang bar "difoto" ke sebuah Bitmap ([sumberLatar] digambar ulang ke Bitmap).
 * 2. Bar digambar dari foto itu yang diburamkan → kaca buram (frosted glass).
 * 3. Gelembung pilihan digambar dengan shader refraksi (AGSL, Android 13+): isi di bawahnya
 *    membesar seperti lensa, melengkung di pinggir, ada pelangi tipis & pinggiran yang lebih terang.
 *    Di Android lama, gelembung memakai lensa pembesar biasa.
 * 4. Gerakan gelembung memakai simulasi pegas (kelas [Pegas]) supaya memantul & bergoyang seperti air.
 */
class LiquidGlassNav @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    /** Satu tab: ikon + label. */
    data class Tab(@param:DrawableRes val ikon: Int, @param:StringRes val label: Int)

    /**
     * Dipanggil saat user memilih tab.
     * Kembalikan true kalau tab itu menjadi tab aktif, false kalau gelembung harus kembali ke tab semula
     * (misalnya tab itu membuka halaman lain).
     */
    var onTabDipilih: ((Int) -> Boolean)? = null

    /** View yang ada di belakang bar (biasanya root layout halaman). Dipakai sebagai bahan kaca. */
    var sumberLatar: View? = null
        set(value) {
            field = value
            invalidate()
        }

    var tabTerpilih = 0
        private set

    private var ikon = emptyList<Drawable>()
    private var label = emptyList<String>()

    // ---------- Warna (ikut warna tema pilihan user) ----------
    private val warnaAktif = warnaTema(R.attr.warnaUtamaGelap)
    private val warnaLembut = warnaTema(R.attr.warnaUtamaLembut)
    private val warnaPasif = ContextCompat.getColor(context, R.color.palm_text_muted)

    // ---------- Ukuran ----------
    /** View sedikit lebih tinggi dari bar supaya gelembung bisa "terangkat" keluar dari bar saat ditahan. */
    private val tinggiView = dp(86f)
    private val tinggiBar = dp(66f)
    private val jarakDalam = dp(6f)
    private val ukuranIkon = dp(24f)
    private val ukuranTeks = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, resources.displayMetrics)

    private val kotakBar = RectF()
    private val kotakGelembung = RectF()
    private val jalurGelembung = Path()
    private val jalurBar = Path()

    // ---------- Foto latar ----------
    private var fotoHalaman: Bitmap? = null   // isi halaman saja
    private var fotoKomposit: Bitmap? = null  // isi halaman + bar + ikon (dilihat lewat gelembung)
    private var sedangMemotret = false

    /** true = halaman di belakang berubah (digulir/diatur ulang), jadi perlu dipotret ulang. */
    private var latarKotor = true
    private var fotoSiap = false
    private var shaderFoto: BitmapShader? = null
    private val lokasiSaya = IntArray(2)
    private val lokasiLatar = IntArray(2)
    private val catFoto = Paint(Paint.FILTER_BITMAP_FLAG)
    private val kotakView = Rect()
    private var fotoBuram: Bitmap? = null     // foto halaman yang sudah diburamkan (untuk bar)

    // ---------- Cat (Paint) ----------
    private val catTintKaca = Paint(Paint.ANTI_ALIAS_FLAG)
    private val catTepiBar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.2f)
    }
    private val catBayangan = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
    private val catLensa = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val catWarnaGelembung = Paint(Paint.ANTI_ALIAS_FLAG)
    private val catCahayaDalam = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val catPelangi = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2.4f)
        shader = SweepGradient(
            0f, 0f,
            intArrayOf(
                Color.rgb(255, 170, 90), Color.TRANSPARENT, Color.rgb(90, 210, 255),
                Color.TRANSPARENT, Color.rgb(255, 170, 90)
            ),
            floatArrayOf(0f, 0.22f, 0.5f, 0.75f, 1f)
        )
    }
    private val matriksPelangi = Matrix()
    private val catTepiGelembung = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.3f)
    }
    /** Kilau: gradasi bundar putih → transparan (lebih ringan daripada efek blur). */
    private val catKilau = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = RadialGradient(0f, 0f, 1f, Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP)
    }
    private val matriksKilau = Matrix()
    private val kotakKilau = RectF()
    private val catTeks = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = ukuranTeks
    }

    // ---------- Shader refraksi (Android 13+) ----------
    private val shaderKaca: RuntimeShader? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) RuntimeShader(KODE_SHADER) else null
    private val matriksFoto = Matrix()

    // ---------- Fisika ----------
    /** Posisi tengah gelembung (pixel). Redaman kecil supaya memantul. */
    private val posisi = Pegas(kekakuan = 260f, rasioRedaman = 0.62f, batasDiam = 0.5f)

    /** 0 = normal, 1 = sedang ditahan (gelembung terangkat jadi kaca bening). */
    private val tekanan = Pegas(kekakuan = 380f, rasioRedaman = 0.72f, batasDiam = 0.002f)

    /** Regangan gelembung: memanjang saat bergerak cepat, lalu bergoyang seperti jeli. */
    private val regangan = Pegas(kekakuan = 520f, rasioRedaman = 0.3f, batasDiam = 0.002f)

    private var sedangBeranimasi = false
    private var waktuFrameTerakhir = 0L
    private var tabDiBawahJari = -1

    /** Saat halaman di belakang digulir atau berubah susunan, kaca harus dipotret & digambar ulang. */
    private val pendengarGulir = ViewTreeObserver.OnScrollChangedListener { tandaiLatarBerubah() }
    private val pendengarTataLetak = ViewTreeObserver.OnGlobalLayoutListener { tandaiLatarBerubah() }

    private fun tandaiLatarBerubah() {
        latarKotor = true
        invalidate()
    }

    init {
        elevation = dp(12f)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val atas = ((tinggiView - tinggiBar) / 2f).toInt()
                outline.setRoundRect(0, atas, view.width, atas + tinggiBar.toInt(), tinggiBar / 2f)
                outline.alpha = 0.3f
            }
        }
        isClickable = true
        isHapticFeedbackEnabled = true
    }

    /** Isi daftar tab. */
    fun aturTab(daftar: List<Tab>, terpilih: Int = 0) {
        ikon = daftar.map { ContextCompat.getDrawable(context, it.ikon)!!.mutate() }
        label = daftar.map { context.getString(it.label) }
        contentDescription = label.joinToString()
        tabTerpilih = terpilih.coerceIn(0, daftar.lastIndex)
        if (width > 0) posisi.lompatKe(pusatTab(tabTerpilih))
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnScrollChangedListener(pendengarGulir)
        viewTreeObserver.addOnGlobalLayoutListener(pendengarTataLetak)
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnScrollChangedListener(pendengarGulir)
        viewTreeObserver.removeOnGlobalLayoutListener(pendengarTataLetak)
        super.onDetachedFromWindow()
    }

    // ================= Ukuran =================

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), tinggiView.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val atas = (h - tinggiBar) / 2f
        kotakBar.set(0f, atas, w.toFloat(), atas + tinggiBar)
        jalurBar.reset()
        jalurBar.addRoundRect(kotakBar, tinggiBar / 2f, tinggiBar / 2f, Path.Direction.CW)
        kotakView.set(0, 0, w, h)
        // Tepi bar: terang di atas, memudar ke bawah (pantulan cahaya)
        catTepiBar.shader = LinearGradient(
            0f, kotakBar.top, 0f, kotakBar.bottom,
            Color.argb(255, 255, 255, 255), Color.argb(60, 255, 255, 255), Shader.TileMode.CLAMP
        )
        val lebarFoto = (w * SKALA_FOTO).toInt().coerceAtLeast(1)
        val tinggiFoto = (h * SKALA_FOTO).toInt().coerceAtLeast(1)
        fotoHalaman = createBitmap(lebarFoto, tinggiFoto)
        fotoKomposit = createBitmap(lebarFoto, tinggiFoto)
        latarKotor = true
        posisi.lompatKe(pusatTab(tabTerpilih))
    }

    private val jumlahTab get() = ikon.size.coerceAtLeast(1)
    private val lebarTab get() = (width - jarakDalam * 2) / jumlahTab

    private fun pusatTab(i: Int) = jarakDalam + lebarTab * (i + 0.5f)

    private fun tabDi(x: Float) = ((x - jarakDalam) / lebarTab).toInt().coerceIn(0, jumlahTab - 1)

    private fun batasiX(x: Float) =
        x.coerceIn(pusatTab(0) - lebarTab * 0.3f, pusatTab(jumlahTab - 1) + lebarTab * 0.3f)

    // ================= Sentuhan =================

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (ikon.isEmpty()) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                tekanan.target = 1f
                posisi.target = batasiX(event.x)
                tabDiBawahJari = tabDi(event.x)
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                mulaiAnimasi()
            }

            MotionEvent.ACTION_MOVE -> {
                posisi.target = batasiX(event.x)
                val tab = tabDi(event.x)
                if (tab != tabDiBawahJari) {
                    tabDiBawahJari = tab
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
                mulaiAnimasi()
            }

            MotionEvent.ACTION_UP -> {
                val tab = tabDi(event.x)
                tekanan.target = 0f
                regangan.kecepatan += 7f // "cipratan" kecil saat dilepas → gelembung bergoyang
                val diterima = tab == tabTerpilih || (onTabDipilih?.invoke(tab) ?: true)
                if (diterima) tabTerpilih = tab
                posisi.target = pusatTab(tabTerpilih)
                performClick()
                mulaiAnimasi()
            }

            MotionEvent.ACTION_CANCEL -> {
                tekanan.target = 0f
                posisi.target = pusatTab(tabTerpilih)
                mulaiAnimasi()
            }
        }
        return true
    }

    override fun performClick(): Boolean = super.performClick()

    // ================= Animasi =================

    private fun mulaiAnimasi() {
        if (!sedangBeranimasi) {
            sedangBeranimasi = true
            waktuFrameTerakhir = 0L
        }
        postInvalidateOnAnimation()
    }

    /** Majukan simulasi pegas sesuai waktu yang berlalu sejak frame sebelumnya. */
    private fun langkahFisika() {
        val sekarang = System.nanoTime()
        val dt = if (waktuFrameTerakhir == 0L) 1f / 60f
        else ((sekarang - waktuFrameTerakhir) / 1e9f).coerceAtMost(1f / 30f)
        waktuFrameTerakhir = sekarang

        // Dipecah jadi langkah kecil supaya simulasi stabil
        var sisa = dt
        var bergerak = false
        while (sisa > 0f) {
            val langkah = min(sisa, 1f / 240f)
            bergerak = posisi.langkah(langkah) or bergerak
            bergerak = tekanan.langkah(langkah) or bergerak
            regangan.target = min(abs(posisi.kecepatan) / dp(1600f), 0.45f)
            bergerak = regangan.langkah(langkah) or bergerak
            sisa -= langkah
        }
        sedangBeranimasi = bergerak
    }

    // ================= Menggambar =================

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Saat sedang memotret halaman, bar ini tidak ikut digambar (supaya tidak memotret dirinya sendiri)
        if (sedangMemotret || ikon.isEmpty()) return
        if (sedangBeranimasi) langkahFisika()

        val t = tekanan.nilai.coerceIn(0f, 1f)
        hitungGelembung()

        // Halaman hanya dipotret ulang kalau berubah, bukan tiap frame animasi (supaya tetap mulus)
        if (latarKotor) {
            fotoSiap = potretLatar()
            if (fotoSiap) siapkanBahanKaca()
            latarKotor = false
        }

        // 1) Bar kaca buram
        gambarBar(canvas, fotoSiap)
        gambarIkon(canvas)

        // 2) Gelembung kaca
        gambarGelembung(canvas, t, fotoSiap)

        if (sedangBeranimasi) postInvalidateOnAnimation()
    }

    private fun hitungGelembung() {
        val t = tekanan.nilai.coerceIn(0f, 1.2f)
        val r = regangan.nilai
        val lebarDasar = lebarTab - dp(2f)
        val tinggiDasar = tinggiBar - jarakDalam * 2
        val lebarG = lebarDasar * (1f + 0.3f * t) * (1f + r)
        val tinggiG = (tinggiDasar * (1f + 0.42f * t) * (1f - 0.3f * r)).coerceAtMost(height - dp(2f))
        // Jangan sampai gelembung keluar dari layar di tab paling kiri/kanan
        val cx = if (lebarG < width) posisi.nilai.coerceIn(lebarG / 2 + dp(1f), width - lebarG / 2 - dp(1f))
        else width / 2f
        val cy = height / 2f
        kotakGelembung.set(cx - lebarG / 2, cy - tinggiG / 2, cx + lebarG / 2, cy + tinggiG / 2)
        jalurGelembung.reset()
        jalurGelembung.addRoundRect(kotakGelembung, tinggiG / 2f, tinggiG / 2f, Path.Direction.CW)
    }

    /** "Memotret" isi halaman di belakang bar ke [fotoHalaman]. */
    private fun potretLatar(): Boolean {
        val latar = sumberLatar ?: return false
        val foto = fotoHalaman ?: return false
        getLocationInWindow(lokasiSaya)
        latar.getLocationInWindow(lokasiLatar)
        foto.eraseColor(Color.TRANSPARENT)
        val c = Canvas(foto)
        c.scale(SKALA_FOTO, SKALA_FOTO)
        c.translate(
            (lokasiLatar[0] - lokasiSaya[0]).toFloat(),
            (lokasiLatar[1] - lokasiSaya[1]).toFloat()
        )
        sedangMemotret = true
        try {
            latar.draw(c)
        } finally {
            sedangMemotret = false
        }
        return true
    }

    /**
     * Disiapkan sekali setiap halaman berubah:
     * - foto buram untuk bar,
     * - komposit (halaman + lapisan bar) + BitmapShader untuk lensa gelembung.
     */
    private fun siapkanBahanKaca() {
        val halaman = fotoHalaman ?: return
        val komposit = fotoKomposit ?: return

        // Kaca buram: foto diperkecil bertahap lalu nanti diperbesar lagi dengan filter → hasilnya buram halus
        var kecil = halaman
        repeat(3) {
            kecil = kecil.scale((kecil.width / 3).coerceAtLeast(1), (kecil.height / 3).coerceAtLeast(1))
        }
        fotoBuram = kecil

        komposit.eraseColor(Color.TRANSPARENT)
        val c = Canvas(komposit)
        c.drawBitmap(halaman, 0f, 0f, null)
        c.scale(SKALA_FOTO, SKALA_FOTO)
        catTintKaca.color = Color.argb(150, 255, 255, 255)
        c.drawRoundRect(kotakBar, tinggiBar / 2f, tinggiBar / 2f, catTintKaca)

        shaderFoto = BitmapShader(komposit, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
            matriksFoto.setScale(1f / SKALA_FOTO, 1f / SKALA_FOTO)
            setLocalMatrix(matriksFoto)
        }
    }

    private fun gambarBar(canvas: Canvas, adaFoto: Boolean) {
        canvas.save()
        canvas.clipPath(jalurBar)
        val buram = fotoBuram
        if (adaFoto && buram != null) {
            canvas.drawBitmap(buram, null, kotakView, catFoto)
            catTintKaca.color = Color.argb(185, 255, 255, 255)
        } else {
            catTintKaca.color = Color.argb(236, 255, 255, 255)
        }
        canvas.drawRect(kotakBar, catTintKaca)
        canvas.restore()
        canvas.drawRoundRect(kotakBar, tinggiBar / 2f, tinggiBar / 2f, catTepiBar)
    }

    private fun gambarIkon(canvas: Canvas) {
        val cx = posisi.nilai
        val cy = kotakBar.centerY()
        for (i in ikon.indices) {
            val x = pusatTab(i)
            // Seberapa dekat tab ini dengan gelembung (1 = tepat di bawahnya)
            val dekat = (1f - abs(x - cx) / lebarTab).coerceIn(0f, 1f)
            val warna = ColorUtils.blendARGB(warnaPasif, warnaAktif, dekat)
            val setengah = ukuranIkon / 2f
            val yIkon = cy - dp(9f)
            ikon[i].setBounds(
                (x - setengah).toInt(), (yIkon - setengah).toInt(),
                (x + setengah).toInt(), (yIkon + setengah).toInt()
            )
            ikon[i].setTint(warna)
            ikon[i].draw(canvas)
            catTeks.color = warna
            canvas.drawText(label[i], x, cy + dp(16f), catTeks)
        }
    }

    private fun gambarGelembung(canvas: Canvas, t: Float, adaFoto: Boolean) {
        val radius = kotakGelembung.height() / 2f
        val cx = kotakGelembung.centerX()
        val cy = kotakGelembung.centerY()
        /** Seberapa "kaca" gelembungnya: tetap sedikit kaca walau tidak ditahan. */
        val kaca = 0.35f + 0.65f * t

        // Bayangan lembut di bawah gelembung saat terangkat
        // (beberapa lapis tipis yang makin lebar → terlihat seperti bayangan buram)
        if (t > 0.01f) {
            catBayangan.alpha = (12 * t).toInt()
            for (k in 1..5) {
                val melebar = dp(1.5f) * k
                canvas.drawRoundRect(
                    kotakGelembung.left - melebar, kotakGelembung.top - melebar + dp(5f) * t,
                    kotakGelembung.right + melebar, kotakGelembung.bottom + melebar + dp(5f) * t,
                    radius + melebar, radius + melebar, catBayangan
                )
            }
        }

        // Isi gelembung: lensa refraksi
        val komposit = fotoKomposit
        val fotoShader = shaderFoto
        val lensa = 1f + 0.28f * t
        if (adaFoto && komposit != null && fotoShader != null) {
            val shader = shaderKaca
            if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                aturShader(shader, fotoShader, cx, cy, radius, kaca, lensa)
                catLensa.shader = shader
                canvas.drawRoundRect(kotakGelembung, radius, radius, catLensa)
            } else {
                // Cadangan Android lama: lensa pembesar biasa
                canvas.save()
                canvas.clipPath(jalurGelembung)
                canvas.scale(lensa, lensa, cx, cy)
                canvas.drawBitmap(komposit, null, kotakView, catFoto)
                canvas.restore()
            }
        } else {
            catWarnaGelembung.color = Color.argb(140, 255, 255, 255)
            canvas.drawRoundRect(kotakGelembung, radius, radius, catWarnaGelembung)
        }

        // Saat tidak ditahan, gelembung diberi warna tema (penanda tab aktif)
        catWarnaGelembung.color = ColorUtils.setAlphaComponent(warnaLembut, (175 * (1f - t)).toInt())
        canvas.drawRoundRect(kotakGelembung, radius, radius, catWarnaGelembung)

        canvas.save()
        canvas.clipPath(jalurGelembung)

        // Ikon & label di dalam gelembung digambar ulang tajam dan diperbesar (efek lensa)
        canvas.save()
        canvas.scale(lensa, lensa, cx, kotakBar.centerY())
        gambarIkon(canvas)
        canvas.restore()

        // Cahaya dalam: pinggiran kaca tebal lebih terang
        for (k in 0 until 6) {
            val inset = dp(1f) + k * dp(1.6f)
            catCahayaDalam.strokeWidth = dp(1.6f)
            catCahayaDalam.color = Color.argb(((70 - k * 12) * kaca).toInt().coerceAtLeast(0), 255, 255, 255)
            canvas.drawRoundRect(
                kotakGelembung.left + inset, kotakGelembung.top + inset,
                kotakGelembung.right - inset, kotakGelembung.bottom - inset,
                radius - inset, radius - inset, catCahayaDalam
            )
        }

        // Pelangi tipis di tepi (dispersi cahaya seperti gelembung sabun)
        matriksPelangi.setRotate(-35f)
        matriksPelangi.postTranslate(cx, cy)
        catPelangi.shader.setLocalMatrix(matriksPelangi)
        catPelangi.alpha = (170 * kaca).toInt()
        val insetPelangi = dp(1.6f)
        canvas.drawRoundRect(
            kotakGelembung.left + insetPelangi, kotakGelembung.top + insetPelangi,
            kotakGelembung.right - insetPelangi, kotakGelembung.bottom - insetPelangi,
            radius - insetPelangi, radius - insetPelangi, catPelangi
        )

        // Kilau spekular: pantulan cahaya di kiri atas (besar) dan kanan bawah (kecil)
        val w = kotakGelembung.width()
        val h = kotakGelembung.height()
        kotakKilau.set(
            kotakGelembung.left + w * 0.1f, kotakGelembung.top + h * 0.05f,
            kotakGelembung.left + w * 0.55f, kotakGelembung.top + h * 0.26f
        )
        gambarKilau(canvas, (240 * kaca).toInt())
        kotakKilau.set(
            kotakGelembung.right - w * 0.34f, kotakGelembung.bottom - h * 0.22f,
            kotakGelembung.right - w * 0.08f, kotakGelembung.bottom - h * 0.08f
        )
        gambarKilau(canvas, (160 * kaca).toInt())
        canvas.restore()

        // Garis tepi luar yang tegas tapi tipis
        catTepiGelembung.color = Color.argb((230 * kaca).toInt(), 255, 255, 255)
        canvas.drawRoundRect(kotakGelembung, radius, radius, catTepiGelembung)
    }

    /** Oval kilau di [kotakKilau]: gradasi bundar diregangkan mengikuti ukuran oval. */
    private fun gambarKilau(canvas: Canvas, alpha: Int) {
        matriksKilau.setScale(kotakKilau.width() / 2f, kotakKilau.height() / 2f)
        matriksKilau.postTranslate(kotakKilau.centerX(), kotakKilau.centerY())
        catKilau.shader.setLocalMatrix(matriksKilau)
        catKilau.alpha = alpha
        canvas.drawOval(kotakKilau, catKilau)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun aturShader(
        shader: RuntimeShader,
        fotoShader: BitmapShader,
        cx: Float, cy: Float, radius: Float,
        kekuatan: Float, lensa: Float
    ) {
        shader.setInputShader("latar", fotoShader)
        shader.setFloatUniform("pusat", cx, cy)
        shader.setFloatUniform("setengah", kotakGelembung.width() / 2f, kotakGelembung.height() / 2f)
        shader.setFloatUniform("radius", radius)
        shader.setFloatUniform("kekuatan", kekuatan)
        shader.setFloatUniform("lensa", lensa)
    }

    // ================= Bantuan =================

    private fun dp(nilai: Float) = nilai * resources.displayMetrics.density

    private fun warnaTema(@AttrRes attr: Int): Int {
        val nilai = TypedValue()
        context.theme.resolveAttribute(attr, nilai, true)
        return nilai.data
    }

    /**
     * Pegas sederhana (hukum Hooke + redaman): nilai ditarik menuju target.
     * rasioRedaman < 1 → memantul/bergoyang sebelum diam, seperti air atau jeli.
     */
    private class Pegas(
        private val kekakuan: Float,
        rasioRedaman: Float,
        private val batasDiam: Float
    ) {
        private val redaman = 2f * sqrt(kekakuan) * rasioRedaman
        var nilai = 0f
        var target = 0f
        var kecepatan = 0f

        fun lompatKe(x: Float) {
            nilai = x
            target = x
            kecepatan = 0f
        }

        /** @return true kalau masih bergerak. */
        fun langkah(dt: Float): Boolean {
            val percepatan = kekakuan * (target - nilai) - redaman * kecepatan
            kecepatan += percepatan * dt
            nilai += kecepatan * dt
            val diam = abs(target - nilai) < batasDiam && abs(kecepatan) < batasDiam * 10f
            if (diam) {
                nilai = target
                kecepatan = 0f
            }
            return !diam
        }
    }

    private companion object {
        /** Foto latar dibuat setengah resolusi supaya ringan. */
        const val SKALA_FOTO = 0.5f

        /**
         * Shader AGSL untuk efek kaca gelembung:
         * - isi di dalam diperbesar (lensa),
         * - makin ke pinggir, isi makin "dibengkokkan" (refraksi),
         * - merah/hijau/biru diambil dari posisi sedikit berbeda di pinggir (pelangi kromatik),
         * - pinggiran dibuat lebih terang (efek Fresnel kaca tebal).
         */
        const val KODE_SHADER = """
            uniform shader latar;
            uniform float2 pusat;
            uniform float2 setengah;
            uniform float radius;
            uniform float kekuatan;
            uniform float lensa;

            float sdKapsul(float2 p, float2 b, float r) {
                float2 q = abs(p) - b + r;
                return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
            }

            half4 main(float2 xy) {
                float2 p = xy - pusat;
                float d = sdKapsul(p, setengah, radius);
                float dalam = clamp(-d / (radius * 0.95), 0.0, 1.0);
                float tepi = 1.0 - dalam;

                float e = 1.0;
                float2 n = float2(
                    sdKapsul(p + float2(e, 0.0), setengah, radius) - sdKapsul(p - float2(e, 0.0), setengah, radius),
                    sdKapsul(p + float2(0.0, e), setengah, radius) - sdKapsul(p - float2(0.0, e), setengah, radius));
                n = n / max(length(n), 0.0001);

                float bengkok = pow(tepi, 2.2) * radius * 0.55 * kekuatan;
                float2 dasar = pusat + p / lensa + n * bengkok;
                float pisah = pow(tepi, 3.0) * 5.0 * kekuatan;

                half4 c = latar.eval(dasar);
                half r = latar.eval(dasar + n * pisah).r;
                half b = latar.eval(dasar - n * pisah).b;
                half4 warna = half4(r, c.g, b, 1.0);

                float fresnel = pow(tepi, 3.0);
                warna.rgb = mix(warna.rgb, half3(1.0), fresnel * 0.5 * kekuatan + 0.06);
                return warna;
            }
        """
    }
}
