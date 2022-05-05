package com.emproto.hoabl.feature.portfolio.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emproto.hoabl.databinding.ItemCompletedInvestmentsBinding
import com.emproto.networklayer.response.portfolio.Project

class CompletedInvestmentAdapter(
    val context: Context,
    val list: List<Project>,
    val onCLickInterface: ExistingUsersPortfolioAdapter.ExistingUserInterface,
    val type: Int
) :
    RecyclerView.Adapter<CompletedInvestmentAdapter.MyViewHolder>() {

    val COMPLETED = 0
    val ONGOING = 1

    inner class MyViewHolder(var binding: ItemCompletedInvestmentsBinding) :
        RecyclerView.ViewHolder(binding.root)

    private lateinit var onItemClickListener: View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCompletedInvestmentsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //set data to view
        val project = list[position]
        Glide.with(context)
            .load(project.projectIcon.value.url)
            .into(holder.binding.ivCompletedInvestmentImage)

        holder.binding.tvCompletedInvestmentName.text = project.projectName
        holder.binding.tvCompletedInvestmentProjectText.text = project.skuType
        holder.binding.tvCompletedInvestmentLocation.text =
            project.projectAddress.city + "," + project.projectAddress.state
        holder.binding.tvCompletedInvestmentPrice.text = project.priceRange.to
        holder.binding.tvCompletedInvestmentArea.text = project.areaRange.to

        holder.binding.ivCompletedInvestmentDropArrow.setOnClickListener {
            holder.binding.cvCompletedInvestmentGraphCard.visibility = View.VISIBLE
            holder.binding.ivCompletedInvestmentUpwardArrow.visibility = View.VISIBLE
            holder.binding.ivCompletedInvestmentDropArrow.visibility = View.GONE
        }

        holder.binding.ivCompletedInvestmentUpwardArrow.setOnClickListener {
            holder.binding.cvCompletedInvestmentGraphCard.visibility = View.GONE
            holder.binding.ivCompletedInvestmentUpwardArrow.visibility = View.GONE
            holder.binding.ivCompletedInvestmentDropArrow.visibility = View.VISIBLE
        }
        holder.binding.tvManageProjects.setOnClickListener {
            onCLickInterface.manageProject(position)
        }
        holder.binding.tvAppreciationRating.text =
            "" + project.projectGraph.estimatedAppreciation + "%"
        if (type == COMPLETED) {
            holder.binding.cvProjectEstimatedAppreciation.visibility = View.VISIBLE
        } else {
            holder.binding.cvProjectEstimatedAppreciation.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int = list.size

}