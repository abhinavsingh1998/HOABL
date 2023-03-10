package com.emproto.hoabl.feature.portfolio.adapters

import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emproto.core.Constants
import com.emproto.core.Utility
import com.emproto.hoabl.databinding.ItemSmartDealsBinding
import com.emproto.networklayer.response.watchlist.Data
import java.text.DecimalFormat

class WatchlistAdapter(
    val context: Context,
    val list: List<Data>,
    val onItemClickListener: ExistingUsersPortfolioAdapter.ExistingUserInterface
) :
    RecyclerView.Adapter<WatchlistAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemSmartDealsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val element = list[position]
        holder.binding.apply {
            if(element.project.isSoldOut){
                cvMainOuterCard.setCardBackgroundColor(Color.parseColor("#8b8b8b"))
                clTopImageView.setBackgroundColor(Color.parseColor("#99000000"))
                tvItemLocationInfo.setTextColor(Color.parseColor("#ffffff"))
                holder.binding.tvApplyNow.visibility=View.GONE
                holder.binding.ivBottomOuterArrow.visibility = View.GONE
                holder.binding.tvSoldOut.visibility=View.VISIBLE
                holder.binding.tvSoldOut.isClickable=false
                holder.binding.tvSoldOut.isEnabled=false
            }
            if (element.project != null) {
                tvItemLocationName.text = element.project.launchName
                (element.project.address.city + " " + element.project.address.state).also {
                    tvItemLocation.text = it
                }

                val price = element.project.priceStartingFrom.toDouble()
                val value = Utility.currencyConversion(price)
                tvItemAmount.text = SpannableStringBuilder()
                    .bold { append(value) }
                    .append(Constants.ONWARDS)
                tvItemArea.text = SpannableStringBuilder()
                    .bold { append("${element.project.areaStartingFrom} Sqft") }
                    .append(Constants.ONWARDS)
                tvNoViews.text = element.project.fomoContent.noOfViews.toString()
                tvItemLocationInfo.text = element.project.shortDescription
                "${Utility.convertTo(element.project.estimatedAppreciation)}%".also {
                    tvRating.text = it
                }

                holder.binding.timerView.visibility =
                    if (element.project.fomoContent.isTargetTimeActive) {
                        val timeCounter = object : CountDownTimer(
                            Utility.conversionForTimer(
                                element.project.fomoContent.targetTime.hours.toString(),
                                element.project.fomoContent.targetTime.minutes.toString(),
                                element.project.fomoContent.targetTime.seconds.toString()
                            ), 1000
                        ) {
                            override fun onTick(millisUntilFinished: Long) {
                                val f = DecimalFormat("00")
                                val fh = DecimalFormat("0")
                                val hour = millisUntilFinished / 3600000 % 24
                                val min = millisUntilFinished / 60000 % 60
                                val sec = millisUntilFinished / 1000 % 60
                                "${
                                    fh.format(hour)
                                        .toString() + ":" + f.format(min) + ":" + f.format(sec)
                                } Hrs Left".also { holder.binding.tvDuration.text = it }
                            }

                            override fun onFinish() {

                            }

                        }
                        timeCounter.start()
                        View.VISIBLE
                    } else View.GONE

                when (element.project.fomoContent.isNoOfViewsActive) {
                    true -> holder.binding.cvView.visibility = View.VISIBLE
                    false -> holder.binding.cvView.visibility = View.GONE
                }

                Glide.with(context)
                    .load(element.project.projectCoverImages.homePageMedia.value.url)
                    .into(ivItemImage)
            }
        }
        holder.binding.cvMainOuterCard.setOnClickListener {
            onItemClickListener.onClickOfWatchlist(element.project.id)
        }
        holder.binding.tvApplyNow.setOnClickListener {
            onItemClickListener.onClickApplyNow(element.project.id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(var binding: ItemSmartDealsBinding) :
        RecyclerView.ViewHolder(binding.root)

}
