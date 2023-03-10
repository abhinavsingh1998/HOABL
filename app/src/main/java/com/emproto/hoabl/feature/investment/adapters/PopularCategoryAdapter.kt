package com.emproto.hoabl.feature.investment.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emproto.hoabl.databinding.ItemPopularCategoryBinding
import com.emproto.hoabl.utils.ItemClickListener


class PopularCategoryAdapter(
    private val list: List<String>,
    private val itemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<PopularCategoryAdapter.MyViewHolder>() {

    inner class MyViewHolder(var binding: ItemPopularCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            ItemPopularCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val element = list[position]
        holder.binding.apply {

            tvCategoryName.text = element
            cvCategoryName.setOnClickListener {
                itemClickListener.onItemClicked(
                    it,
                    position,
                    element)
            }
        }
    }

    override fun getItemCount(): Int = list.size
}