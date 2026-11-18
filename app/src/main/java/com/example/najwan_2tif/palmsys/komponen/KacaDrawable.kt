package com.example.najwan_2tif.palmsys.komponen

import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import com.example.najwan_2tif.R
import org.xmlpull.v1.XmlPullParser
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Background kaca ("liquid glass") untuk card. Dipakai langsung dari XML, contoh res/drawable/bg_kartu.xml:
 *
 *     <drawable class="com.example.najwan_2tif.palmsys.komponen.KacaDrawable"
 *         app:kacaRadius="14dp" app:kacaWarnaAwal="#FFFFFFFF" app:kacaPekat="0.8" ... />
 *
 * Cara menggambarnya:
 * 1. Background halaman tepat di belakang card "difoto" kecil-kecil lalu diperbesar → jadi buram.
 * 2. Ditimpa lapisan warna kaca yang agak pekat (kacaPekat) supaya tidak terlalu tembus pandang.
 * 3. Ditambah kilau di bagian atas dan garis tepi terang seperti pinggiran kaca.
 * Saat halaman digulir, foto latarnya diperbarui sehingga card terlihat seperti kaca yang bergerak.
 */
class KacaDrawable : Drawable() {

    private var radius = 0f
    private var warnaAwal = Color.WHITE
    private var warnaAkhir = Color.WHITE
    private var pekat = 0.8f
    private var pakaiLatar = true
    private var alphaDrawable = 255
    private var attrWarnaAwal = 0
    private var attrWarnaAkhir = 0

    private val kotak = RectF()
    private val jalur = Path()
    private val catLapisan = Paint(Paint.ANTI_ALIAS_FLAG)
    private val catKilau = Paint(Paint.ANTI_ALIAS_FLAG)
    private val catTepi = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val catGarisLuar = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val catFoto = Paint(Paint.FILTER_BITMAP_FLAG)

    // ---------- Foto latar (background halaman di belakang card) ----------
    private var fotoLatar: Bitmap? = null
    private var kunciFoto = ""
    private val lokasiCard = IntArray(2)
    private val lokasiLatar = IntArray(2)
    private var sudahDengarGulir = false

    override fun inflate(
        r: Resources, parser: XmlPullParser, attrs: AttributeSet, theme: Resources.Theme?
    ) {
        super.inflate(r, parser, attrs, theme)
        val a = theme?.obtainStyledAttributes(attrs, R.styleable.KacaDrawable, 0, 0)
            ?: r.obtainAttributes(attrs, R.styleable.KacaDrawable)
        radius = a.getDimension(R.styleable.KacaDrawable_kacaRadius, 14f * r.displayMetrics.density)
        // Warna bisa berupa ?attr/warnaUtama (warna tema). Kalau tema belum ada saat dimuat,
        // id atributnya disimpan dulu lalu dibaca nanti di applyTheme().
        attrWarnaAwal = idAtributTema(a, R.styleable.KacaDrawable_kacaWarnaAwal)
        attrWarnaAkhir = idAtributTema(a, R.styleable.KacaDrawable_kacaWarnaAkhir)
        if (attrWarnaAwal == 0) warnaAwal = a.getColor(R.styleable.KacaDrawable_kacaWarnaAwal, Color.WHITE)
        if (!a.hasValue(R.styleable.KacaDrawable_kacaWarnaAkhir)) {
            // Tidak diisi → sama dengan warna awal
            warnaAkhir = warnaAwal
            attrWarnaAkhir = attrWarnaAwal
        } else if (attrWarnaAkhir == 0) {
            warnaAkhir = a.getColor(R.styleable.KacaDrawable_kacaWarnaAkhir, warnaAwal)
        }
        pekat = a.getFloat(R.styleable.KacaDrawable_kacaPekat, 0.8f)
        pakaiLatar = a.getBoolean(R.styleable.KacaDrawable_kacaPakaiLatar, true)
        a.recycle()

        val density = r.displayMetrics.density
        catTepi.strokeWidth = 1.2f * density
        catGarisLuar.strokeWidth = 0.8f * density
    }

    /** Kalau nilai atribut berupa ?attr/... yang belum terbaca, kembalikan id atributnya; selain itu 0. */
    private fun idAtributTema(a: TypedArray, index: Int): Int {
        val nilai = a.peekValue(index) ?: return 0
        return if (nilai.type == TypedValue.TYPE_ATTRIBUTE) nilai.data else 0
    }

    override fun canApplyTheme(): Boolean = attrWarnaAwal != 0 || attrWarnaAkhir != 0

    override fun applyTheme(t: Resources.Theme) {
        super.applyTheme(t)
        val nilai = TypedValue()
        if (attrWarnaAwal != 0 && t.resolveAttribute(attrWarnaAwal, nilai, true)) warnaAwal = nilai.data
        if (attrWarnaAkhir != 0 && t.resolveAttribute(attrWarnaAkhir, nilai, true)) warnaAkhir = nilai.data
        attrWarnaAwal = 0
        attrWarnaAkhir = 0
        onBoundsChange(bounds)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        kotak.set(bounds)
        jalur.reset()
        jalur.addRoundRect(kotak, radius, radius, Path.Direction.CW)
        val l = kotak.left
        val t = kotak.top

        // Lapisan kaca: gradasi diagonal, dibuat agak tembus (pekat)
        catLapisan.shader = LinearGradient(
            l, t, kotak.right, kotak.bottom,
            pakaiPekat(warnaAwal), pakaiPekat(warnaAkhir), Shader.TileMode.CLAMP
        )
        // Kilau lembut di bagian atas card
        catKilau.shader = LinearGradient(
            0f, t, 0f, t + kotak.height() * 0.5f,
            Color.argb(80, 255, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        // Tepi kaca: terang di kiri-atas, redup di tengah, agak terang lagi di kanan-bawah
        catTepi.shader = LinearGradient(
            l, t, kotak.right, kotak.bottom,
            intArrayOf(
                Color.argb(240, 255, 255, 255), Color.argb(60, 255, 255, 255), Color.argb(170, 255, 255, 255)
            ),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP
        )
        catGarisLuar.color = Color.argb(22, 0, 0, 0)
        kunciFoto = ""
    }

    override fun draw(canvas: Canvas) {
        if (kotak.isEmpty) return
        canvas.save()
        canvas.clipPath(jalur)

        if (pakaiLatar) gambarLatarBuram(canvas)
        canvas.drawRect(kotak, catLapisan)
        canvas.drawRect(kotak, catKilau)

        canvas.restore()

        // Garis luar tipis (supaya card tetap terlihat batasnya) + tepi kaca yang berkilau
        val setengah = catTepi.strokeWidth / 2f
        canvas.drawRoundRect(
            kotak.left + setengah, kotak.top + setengah, kotak.right - setengah, kotak.bottom - setengah,
            radius, radius, catGarisLuar
        )
        canvas.drawRoundRect(
            kotak.left + setengah, kotak.top + setengah, kotak.right - setengah, kotak.bottom - setengah,
            radius - setengah, radius - setengah, catTepi
        )
    }

    /** Gambar background halaman (yang ada di belakang card ini) dalam keadaan buram. */
    private fun gambarLatarBuram(canvas: Canvas) {
        val card = callback as? View ?: return
        val latar = cariLatar(card) ?: return
        val bgLatar = latar.background ?: return

        if (!sudahDengarGulir) {
            // Saat halaman digulir, posisi card terhadap background berubah → gambar ulang
            card.viewTreeObserver.addOnScrollChangedListener(ViewTreeObserver.OnScrollChangedListener {
                invalidateSelf()
            })
            sudahDengarGulir = true
        }

        card.getLocationInWindow(lokasiCard)
        latar.getLocationInWindow(lokasiLatar)
        val dx = lokasiCard[0] - lokasiLatar[0]
        val dy = lokasiCard[1] - lokasiLatar[1]
        val kunci = "$dx,$dy,${bounds.width()},${bounds.height()}"

        if (kunci != kunciFoto || fotoLatar == null) {
            // Foto dibuat sangat kecil (1/14 ukuran). Saat diperbesar lagi dengan filter → terlihat buram.
            val lebar = max(1, (bounds.width() * SKALA).roundToInt())
            val tinggi = max(1, (bounds.height() * SKALA).roundToInt())
            val foto = fotoLatar?.takeIf { it.width == lebar && it.height == tinggi }
                ?: createBitmap(lebar, tinggi).also { fotoLatar = it }
            foto.eraseColor(Color.TRANSPARENT)
            val c = Canvas(foto)
            c.scale(SKALA, SKALA)
            c.translate(-(dx + bounds.left).toFloat(), -(dy + bounds.top).toFloat())
            bgLatar.draw(c)
            kunciFoto = kunci
        }
        fotoLatar?.let { canvas.drawBitmap(it, null, bounds, catFoto) }
    }

    /** Cari view induk terdekat yang punya background halaman (bukan kaca lain / efek sentuh). */
    private fun cariLatar(card: View): View? {
        var induk = card.parent
        while (induk is View) {
            val bg = induk.background
            if (bg != null && bg !is KacaDrawable && bg !is RippleDrawable) return induk
            induk = induk.parent
        }
        return null
    }

    private fun pakaiPekat(warna: Int): Int =
        ColorUtils.setAlphaComponent(warna, (Color.alpha(warna) * pekat * alphaDrawable / 255f).roundToInt())

    override fun getOutline(outline: Outline) {
        outline.setRoundRect(bounds, radius)
        outline.alpha = 0.25f
    }

    override fun setAlpha(alpha: Int) {
        alphaDrawable = alpha
        onBoundsChange(bounds)
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        catLapisan.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    private companion object {
        /** Ukuran foto latar dibanding card. Makin kecil → makin buram. */
        const val SKALA = 1f / 14f
    }
}
