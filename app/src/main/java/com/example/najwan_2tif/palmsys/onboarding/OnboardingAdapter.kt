package com.example.najwan_2tif.palmsys.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.najwan_2tif.databinding.ItemOnboardingBinding

/** Adapter ViewPager2: tiap halaman onboarding memakai layout item_onboarding.xml. */
class OnboardingAdapter(
    private val daftarHalaman: List<HalamanOnboarding>
) : RecyclerView.Adapter<OnboardingAdapter.HalamanViewHolder>() {

    class HalamanViewHolder(val binding: ItemOnboardingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HalamanViewHolder {
        val binding = ItemOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HalamanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HalamanViewHolder, position: Int) {
        val halaman = daftarHalaman[position]
        holder.binding.imgIlustrasi.setImageResource(halaman.ilustrasi)
        holder.binding.txtJudul.setText(halaman.judul)
        holder.binding.txtDeskripsi.setText(halaman.deskripsi)
    }

    override fun getItemCount(): Int = daftarHalaman.size
}
