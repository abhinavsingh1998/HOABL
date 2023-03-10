package com.emproto.hoabl.feature.investment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emproto.hoabl.databinding.LandSkusAppliedLayoutBinding
import com.emproto.hoabl.databinding.LandSkusTopLayoutBinding
import com.emproto.hoabl.databinding.NotConvincedLayoutBlackBinding
import com.emproto.hoabl.feature.investment.views.LandSkusFragment
import com.emproto.hoabl.model.RecyclerViewItem
import com.emproto.hoabl.utils.ItemClickListener
import com.emproto.networklayer.response.investment.Inventory

class LandSkusAdapter(
    val context:Context?,
    private val fragment: LandSkusFragment,
    val list: ArrayList<RecyclerViewItem>,
    val appliedList: List<Inventory>,
    val notAppliedList: List<Inventory>,
    val itemClickListener: ItemClickListener,
    val title: String,
    val subtitle: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val LAND_SKUS_VIEW_TYPE_ONE = 1
        const val LAND_SKUS_VIEW_TYPE_TWO = 2
        const val LAND_SKUS_VIEW_TYPE_THREE = 3
    }

    private lateinit var skusListAdapter: SkusListAdapter
    private lateinit var skusListAppliedAdapter: SkusListAppliedAdapter
    private lateinit var onItemClickListener: View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            LAND_SKUS_VIEW_TYPE_ONE -> LandSkusAvailableViewHolder(
                LandSkusTopLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            LAND_SKUS_VIEW_TYPE_TWO -> LandSkusAppliedViewHolder(
                LandSkusAppliedLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> LandSkusNotConvincedViewHolder(
                NotConvincedLayoutBlackBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (list[position].viewType) {
            LAND_SKUS_VIEW_TYPE_ONE -> {
                (holder as LandSkusAvailableViewHolder).bind()
            }
            LAND_SKUS_VIEW_TYPE_TWO -> {
                (holder as LandSkusAppliedViewHolder).bind()
            }
            LAND_SKUS_VIEW_TYPE_THREE -> {
                (holder as LandSkusNotConvincedViewHolder).bind()
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return list[position].viewType
    }

    private inner class LandSkusAvailableViewHolder(val binding: LandSkusTopLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val landSkus = "$title (${notAppliedList.size})"
            binding.tvLandSkusTitle.text = landSkus
            binding.tvLandSkusSubtitle.text = subtitle
            skusListAdapter = SkusListAdapter(context!!,fragment, notAppliedList, itemClickListener)
            binding.rvLandSkusItems.adapter = skusListAdapter
            skusListAdapter.setSkusListItemClickListener(fragment.onLandSkusItemClickListener)
        }
    }

    private inner class LandSkusAppliedViewHolder(val binding: LandSkusAppliedLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            arrayListOf("1", "2")
            skusListAppliedAdapter =
                SkusListAppliedAdapter(appliedList, itemClickListener)
            binding.rvLandSkusItemsApplied.adapter = skusListAppliedAdapter
        }
    }

    private inner class LandSkusNotConvincedViewHolder(val binding: NotConvincedLayoutBlackBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.clNotConvinced.setOnClickListener(onItemClickListener)
        }
    }

    fun setItemClickListener(clickListener: View.OnClickListener) {
        onItemClickListener = clickListener
    }

}