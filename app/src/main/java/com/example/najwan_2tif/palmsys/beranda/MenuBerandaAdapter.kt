package com.example.najwan_2tif.palmsys.beranda

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.najwan_2tif.databinding.ItemMenuBerandaBinding

/**
 * Adapter RecyclerView untuk grid menu di Beranda.
 * Tiap item memakai layout item_menu_beranda.xml (ikon berwarna + label).
 */
class MenuBerandaAdapter(
    private val daftarMenu: List<MenuBeranda>
) : RecyclerView.Adapter<MenuBerandaAdapter.MenuViewHolder>() {

    class MenuViewHolder(val binding: ItemMenuBerandaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBerandaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = daftarMenu[position]
        val context = holder.itemView.context
        holder.binding.imgIkon.setImageResource(menu.ikon)
        holder.binding.imgIkon.imageTintList = ColorStateList.valueOf(context.getColor(menu.warna))
        holder.binding.imgIkon.backgroundTintList = ColorStateList.valueOf(context.getColor(menu.warnaLatar))
        holder.binding.txtLabel.setText(menu.label)
        holder.itemView.setOnClickListener { menu.onClick() }
    }

    override fun getItemCount(): Int = daftarMenu.size
}
