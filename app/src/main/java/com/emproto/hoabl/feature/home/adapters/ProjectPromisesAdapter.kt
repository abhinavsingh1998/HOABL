package com.emproto.hoabl.feature.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emproto.hoabl.databinding.ItemProjectPromisesBinding

class ProjectPromisesAdapter(private val list: List<String>) :
    RecyclerView.Adapter<ProjectPromisesAdapter.MyViewHolder>() {

    inner class MyViewHolder(var binding: ItemProjectPromisesBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            ItemProjectPromisesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

    }

    override fun getItemCount(): Int = list.size

}