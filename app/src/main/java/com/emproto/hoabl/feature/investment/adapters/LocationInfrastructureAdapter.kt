package com.emproto.hoabl.feature.investment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.ItemLocationInfrastructureBinding
import com.emproto.hoabl.utils.MapItemClickListener
import com.emproto.networklayer.preferences.AppPreference
import com.emproto.networklayer.response.investment.ValueXXX
import javax.inject.Inject

class LocationInfrastructureAdapter(
    private val context: Context,
    private val list: List<ValueXXX>,
    private val itemClickListener: MapItemClickListener,
    private var isDistanceAvl: Boolean = false,
    private var distanceList: ArrayList<String>,
    private var selectedItemPos: Int = -1,
    private var durationList : List<String> = mutableListOf()
) : RecyclerView.Adapter<LocationInfrastructureAdapter.MyViewHolder>() {

    @Inject
    lateinit var appPreference: AppPreference

    var lastItemSelectedPos = -1

    inner class MyViewHolder(var binding: ItemLocationInfrastructureBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            position: Int
        ) {
            val element = list[position]
//            when (distanceList.size) {
//                0 -> {
//
//                }
//                else -> {
//                    val distanceItem = distanceList[position]
//                    binding.tvLocationDistance.text = distanceItem
//                }
//            }


            binding.apply {
//                eventTrackingMapSearchDistancefrom()
                tvLocationName.text = element.name
//                val locationDuration = "${element.minutes}mins"
//                val tvlocationDuration = "${element.hours}hr ${element.minutes}mins"
//                val tvlocationMins="${element.hours}hr"
//                if (element.hours=="00") {
//                     tvLocationDuration.text = locationDuration
//                }else if(element.minutes=="00"){
//                    tvLocationDuration.text=tvlocationMins
//                }else{
//                    tvLocationDuration.text = tvlocationDuration
//
//                }
                tvLocationDistance.text = element.distance
                element.duration?.let {
                    val duration = element.duration.replace("hour","hr",ignoreCase = true)
                    tvLocationDuration.text = duration
                }

                Glide
                    .with(context)
                    .load(element.icon.value.url)
                    .into(ivLocationImage)
                when (isDistanceAvl) {
                    true -> tvLocationDistance.visibility = View.VISIBLE
                    else -> {}
                }
            }
            binding.cvLocationInfrastructureCard.setOnClickListener {
                lastItemSelectedPos = selectedItemPos
                selectedItemPos = adapterPosition
                lastItemSelectedPos = if (lastItemSelectedPos == -1)
                    selectedItemPos
                else {
                    notifyItemChanged(lastItemSelectedPos)
                    selectedItemPos
                }
                notifyItemChanged(selectedItemPos)
                itemClickListener.onItemClicked(it, position, element.latitude, element.longitude)
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemLocationInfrastructureBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position == selectedItemPos) {
            holder.binding.cvLocationInfrastructureCard.strokeWidth = 2
            holder.binding.cvLocationInfrastructureCard.strokeColor =
                ContextCompat.getColor(context, R.color.text_blue_color)
        } else {
            holder.binding.cvLocationInfrastructureCard.strokeWidth = 0
            holder.binding.cvLocationInfrastructureCard.strokeColor =
                ContextCompat.getColor(context, R.color.white)
        }
        holder.bind(position)
    }

    override fun getItemCount(): Int = list.size

}