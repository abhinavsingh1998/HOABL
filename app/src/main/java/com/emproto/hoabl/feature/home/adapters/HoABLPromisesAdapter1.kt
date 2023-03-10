package com.emproto.hoabl.feature.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emproto.hoabl.databinding.ItemHoablPromisesBinding
import com.emproto.hoabl.utils.ItemClickListener
import com.emproto.networklayer.preferences.AppPreference
import com.emproto.networklayer.response.home.Data
import com.emproto.networklayer.response.home.HomePagesOrPromise
import javax.inject.Inject

class HoABLPromisesAdapter1(
    val context: Context,
    val itemCount: Data,
    val list: List<HomePagesOrPromise>,
    private val itemInterface: ItemClickListener
) : RecyclerView.Adapter<HoABLPromisesAdapter1.MyViewHolder>() {

    @Inject
    lateinit var appPreference: AppPreference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            ItemHoablPromisesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[holder.adapterPosition]
        holder.binding.title.text = item.name
        holder.binding.desc.text = item.shortDescription
        holder.binding.homePromisesItem.setOnClickListener {
            itemInterface.onItemClicked(
                it,
                position,
                item.id.toString())
        }
        if (item.displayMedia != null) {
            Glide.with(context)
                .load(item.displayMedia!!.value.url)
                .dontAnimate()
                .into(holder.binding.image)
        }
    }

    override fun getItemCount(): Int {
        var itemList = 0
        if (itemCount.page.totalPromisesOnHomeScreen < list.size) {
            itemList = itemCount.page.totalPromisesOnHomeScreen
        } else {
            itemList = list.size
        }
        return itemList
    }

    inner class MyViewHolder(val binding: ItemHoablPromisesBinding) :
        RecyclerView.ViewHolder(binding.root)


}
