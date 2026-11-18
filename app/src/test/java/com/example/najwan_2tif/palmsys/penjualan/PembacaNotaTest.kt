package com.example.najwan_2tif.palmsys.penjualan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PembacaNotaTest {
    @Test
    fun labelBrutoDenganTitikRibuan() {
        val nota = """
            RAM API-API
            Tgl 03/10/2026
            BRUTO : 2.640 kg
            TARA  : 40 kg
            NETTO : 2.600 kg
        """.trimIndent()
        assertEquals(2640L, PembacaNota.cariBeratBruto(nota))
    }

    @Test
    fun nilaiDiBarisSesudahLabel() {
        assertEquals(2640L, PembacaNota.cariBeratBruto("Berat Bruto\n2,640\nNetto\n2,600"))
    }

    @Test
    fun angkaPadaLabelTimbangTidakTerbaca() {
        assertEquals(1850L, PembacaNota.cariBeratBruto("Timbang 1   1850\nTimbang 2   30"))
    }

    @Test
    fun tanpaLabelPakaiKgTerbesar() {
        assertEquals(2640L, PembacaNota.cariBeratBruto("2640 KG\n40 KG\n2600 KG"))
    }

    @Test
    fun tidakAdaBerat() {
        assertNull(PembacaNota.cariBeratBruto("Terima kasih"))
    }
}
