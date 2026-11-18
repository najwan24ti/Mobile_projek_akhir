# Struktur Proyek PalmSys

Alur aplikasi: **Onboarding → Login → Beranda → Catat Penjualan TBS / Semua Transaksi / Profil → Pengaturan**

## Kode Kotlin
`app/src/main/java/com/example/najwan_2tif/palmsys/`

| Folder | File | Isi |
|---|---|---|
| `onboarding/` | `OnboardingActivity.kt` | Layar pengenalan 3 halaman (ViewPager2), muncul sekali saat pertama buka |
| `onboarding/` | `OnboardingAdapter.kt`, `HalamanOnboarding.kt` | Adapter + data tiap halaman onboarding |
| `auth/` | `LoginActivity.kt` | Halaman login |
| `beranda/` | `BerandaActivity.kt` | Halaman beranda (saldo, menu, transaksi terakhir) |
| `beranda/` | `MenuBerandaAdapter.kt`, `MenuBeranda.kt` | **RecyclerView** grid menu 4 kolom di Beranda |
| `beranda/` | `Transaksi.kt` | Model data satu transaksi |
| `penjualan/` | `CatatPenjualanActivity.kt` | Form catat penjualan TBS + rumus hitungnya |
| `penjualan/` | `PembacaNota.kt` | Fitur scan nota: baca berat bruto dari teks OCR ML Kit (tes: `app/src/test/.../PembacaNotaTest.kt`) |
| `transaksi/` | `TransaksiActivity.kt` | Halaman Semua Transaksi (ListView) |
| `transaksi/` | `TransaksiAdapter.kt` | **CustomAdapter** (turunan `BaseAdapter`) untuk ListView transaksi |
| `profil/` | `ProfilActivity.kt` | Halaman profil (identitas, ringkasan kebun, pengaturan, keluar) |
| `pengaturan/` | `PengaturanActivity.kt` | Halaman pengaturan: pilih warna tema, notifikasi, sinkron |
| `pengaturan/` | `Tema.kt` | Daftar warna tema (Oranye, Merah, Ungu, Hijau, Biru) + simpan pilihan user |
| `komponen/` | `LiquidGlassNav.kt` | Navigasi bawah bergaya Liquid Glass (custom View: digambar di `onDraw`, gerak pakai simulasi pegas) |
| `util/` | `BaseActivity.kt` | Induk semua halaman: memasang warna tema pilihan user |
| `util/` | `BarisMenu.kt` | Fungsi `atur()` untuk mengisi satu baris menu (`item_menu_profil.xml`) |
| `util/` | `Format.kt` | Format angka & rupiah (`formatRupiah`, `formatAngka`) |

Fitur baru → buat folder baru di `palmsys/` (misal `pengeluaran/`, `kebun/`, `laporan/`).

## Resource
`app/src/main/res/`

| Folder | Aturan nama | Contoh |
|---|---|---|
| `layout/` | `activity_<halaman>.xml` untuk halaman, `item_<nama>.xml` untuk potongan berulang | `activity_beranda.xml`, `item_transaksi.xml` |
| `drawable/` | `ic_` = ikon, `bg_` = latar/bentuk | `ic_calendar.xml`, `bg_saldo_card.xml` |
| `menu/` | `menu_<tempat>.xml` | `menu_catat_penjualan.xml` |
| `color/` | warna yang berubah saat dipilih | `chip_bg.xml`, `chip_text.xml` |
| `values/strings.xml` | dikelompokkan per halaman: `login_`, `beranda_`, `penjualan_` | `penjualan_simpan` |
| `values/colors.xml` | `palm_` = warna utama, `tile_` = warna ikon menu | `palm_orange_dark` |
| `values/themes.xml` | `Theme.PalmSys` = tema oranye Beranda & Penjualan | |

## Warna tema (bisa diganti user)
- Jangan tulis `@color/palm_orange...` langsung di layout. Pakai warna tema:
  `?attr/warnaUtama`, `?attr/warnaUtamaGelap`, `?attr/warnaUtamaLembut` (didefinisikan di `values/attrs.xml`).
- Tiap warna punya style di `values/themes.xml`: `Theme.PalmSys` (oranye), `Theme.PalmSys.Merah`, `.Ungu`, `.Hijau`, `.Biru`.
- Halaman baru wajib `extends BaseActivity` (bukan `AppCompatActivity`) supaya ikut berganti warna.
- Tambah warna baru: tambah 3 warna di `colors.xml` → 1 style di `themes.xml` → 1 baris di `Tema.kt`.
