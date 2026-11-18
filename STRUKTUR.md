# Struktur Proyek PalmSys

Alur aplikasi: **Login → Beranda → Catat Penjualan TBS / Profil**

## Kode Kotlin
`app/src/main/java/com/example/najwan_2tif/palmsys/`

| Folder | File | Isi |
|---|---|---|
| `auth/` | `LoginActivity.kt` | Halaman login |
| `beranda/` | `BerandaActivity.kt` | Halaman beranda (saldo, menu, transaksi terakhir) |
| `beranda/` | `Transaksi.kt` | Model data satu transaksi |
| `penjualan/` | `CatatPenjualanActivity.kt` | Form catat penjualan TBS + rumus hitungnya |
| `penjualan/` | `PembacaNota.kt` | Fitur scan nota: baca berat bruto dari teks OCR ML Kit (tes: `app/src/test/.../PembacaNotaTest.kt`) |
| `profil/` | `ProfilActivity.kt` | Halaman profil (identitas, ringkasan kebun, pengaturan, keluar) |
| `util/` | `Format.kt` | Format angka & rupiah (`formatRupiah`, `formatAngka`) |

Fitur baru → buat folder baru di `palmsys/` (misal `pengeluaran/`, `kebun/`, `laporan/`).

## Resource
`app/src/main/res/`

| Folder | Aturan nama | Contoh |
|---|---|---|
| `layout/` | `activity_<halaman>.xml` untuk halaman, `item_<nama>.xml` untuk potongan berulang | `activity_beranda.xml`, `item_transaksi.xml` |
| `drawable/` | `ic_` = ikon, `bg_` = latar/bentuk | `ic_calendar.xml`, `bg_saldo_card.xml` |
| `menu/` | `menu_<tempat>.xml` | `menu_navigasi_bawah.xml` |
| `color/` | warna yang berubah saat dipilih | `chip_bg.xml`, `nav_item.xml` |
| `values/strings.xml` | dikelompokkan per halaman: `login_`, `beranda_`, `penjualan_` | `penjualan_simpan` |
| `values/colors.xml` | `palm_` = warna utama, `tile_` = warna ikon menu | `palm_orange_dark` |
| `values/themes.xml` | `Theme.PalmSys` = tema oranye Beranda & Penjualan | |
