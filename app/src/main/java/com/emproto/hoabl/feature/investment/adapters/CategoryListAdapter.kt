package com.emproto.hoabl.feature.investment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emproto.hoabl.databinding.ItemCategoryListBinding
import com.emproto.hoabl.utils.ItemClickListener
import com.emproto.networklayer.response.investment.PageManagementsOrCollectionOneModel
import com.emproto.networklayer.response.investment.PageManagementsOrCollectionTwoModel
import java.util.ArrayList

class CategoryListAdapter(private val context: Context,val list:List<PageManagementsOrCollectionOneModel> = mutableListOf(), private val itemClickListener:ItemClickListener):RecyclerView.Adapter<CategoryListAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(var binding: ItemCategoryListBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(view: View, position:Int, item:PageManagementsOrCollectionOneModel, clickListener: ItemClickListener){
            val element = list[position]
            itemView.setOnClickListener{
                clickListener.onItemClicked(view,position,element.toString())
            }
            binding.apply {
                tvProjectName.text = element.launchName
                tvCategoryPrice.text = element.priceRange.from + " Onwards"
                tvCategoryArea.text = element.areaRange.from + " Onwards"
                tvCategoryItemInfo.text = element.shortDescription
                Glide.with(context)
                    .load(element.mediaGalleries[0].coverImage[0].mediaContent.value.url)
                    .into(ivCategoryImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = ItemCategoryListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val list = list[position]
        holder.bind(holder.itemView,position,list,itemClickListener)

    }

    override fun getItemCount(): Int = list.size

}