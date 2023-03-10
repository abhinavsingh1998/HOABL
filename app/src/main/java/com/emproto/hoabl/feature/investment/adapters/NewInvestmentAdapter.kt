package com.emproto.hoabl.feature.investment.adapters

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emproto.core.Utility
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.LastFewPlotsLayoutBinding
import com.emproto.hoabl.databinding.NewInvestmentTopLayoutBinding
import com.emproto.hoabl.databinding.TrendingProjectsLayoutBinding
import com.emproto.hoabl.model.RecyclerViewItem
import com.emproto.hoabl.utils.ItemClickListener
import com.emproto.networklayer.response.investment.Data
import com.emproto.networklayer.response.investment.MediaGalleries
import com.emproto.networklayer.response.investment.PageManagementsOrCollectionOneModel
import com.emproto.networklayer.response.investment.PageManagementsOrCollectionTwoModel

class NewInvestmentAdapter(
    private val context: Context,
    val list: List<RecyclerViewItem>,
    private val data: Data,
    private val itemClickListener: ItemClickListener,
    private val mediaGalleries: MediaGalleries?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_NEW_LAUNCH = 1
        const val TYPE_LAST_PLOTS = 2
        const val TYPE_TRENDING_PROJECTS = 3
    }

    private lateinit var adapter: InvestmentViewPagerAdapter
    private lateinit var lastFewPlotsAdapter: LastFewPlotsAdapter
    private lateinit var trendingProjectsAdapter: TrendingProjectsAdapter
    private lateinit var onItemClickListener: View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_NEW_LAUNCH -> {
                NewLaunchViewHolder(
                    NewInvestmentTopLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_LAST_PLOTS -> {
                LastFewPlotsViewHolder(
                    LastFewPlotsLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                TrendingProjectsViewHolder(
                    TrendingProjectsLayoutBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (list[position].viewType) {
            TYPE_NEW_LAUNCH -> {
                (holder as NewLaunchViewHolder).bind()
            }
            TYPE_LAST_PLOTS -> {
                (holder as LastFewPlotsViewHolder).bind()
            }
            TYPE_TRENDING_PROJECTS -> {
                (holder as TrendingProjectsViewHolder).bind()
            }
        }
    }

    override fun getItemCount(): Int = list.size

    private inner class NewLaunchViewHolder(private val binding: NewInvestmentTopLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val rating = "${
                String.format(
                    " % .0f",
                    data.pageManagementsOrNewInvestments[0].generalInfoEscalationGraph.estimatedAppreciation
                )
            }%"
            binding.tvRating.text = rating
            binding.tvNewLaunch.text = data.page.newInvestments.displayName
            binding.tvComingSoon.text = data.page.newInvestments.subHeading
            binding.tvInvestmentProjectLocation.text =
                data.pageManagementsOrNewInvestments[0].address.city + "," + data.pageManagementsOrNewInvestments[0].address.state


            binding.tvInvestmentProjectName.text =
                data.pageManagementsOrNewInvestments[0].launchName
            if (data.pageManagementsOrNewInvestments[0].isSoldOut){
                binding.tvApplyNow.text="Sold Out"
                binding.tvApplyNow.setBackgroundColor(ContextCompat.getColor(context, R.color.background))
            }

            val price =  data.pageManagementsOrNewInvestments[0].priceStartingFrom.toDouble()
            val value = Utility.currencyConversion(price)
            binding.tvAmount.text = SpannableStringBuilder()
                .append(value)
            binding.tvArea.text = SpannableStringBuilder()
                .append("${data.pageManagementsOrNewInvestments[0].areaStartingFrom} Sqft")
            binding.tvBackgroundGrey.text = data.pageManagementsOrNewInvestments[0].shortDescription
            if(data.pageManagementsOrNewInvestments[0].fomoContent.isDaysActive){
                binding.tvViewInfo.visibility=View.VISIBLE
                binding.tvViewInfo.text = SpannableStringBuilder()
                    .bold { append("${data.pageManagementsOrNewInvestments[0].fomoContent.noOfViews} People") }
                    .append(" saw this in ${data.pageManagementsOrNewInvestments[0].fomoContent.days} days")
            }
            else{
                binding.tvViewInfo.visibility=View.GONE
            }


            val listViews = ArrayList<String>()
            mediaGalleries?.let {
                for (item in mediaGalleries?.images!!) {
                    listViews.add(item.mediaContent.value.url)
                }
                for (item in mediaGalleries?.threeSixtyImages!!) {
                    listViews.add(item.mediaContent.value.url)
                }
            }
            adapter = InvestmentViewPagerAdapter(listViews)
            binding.viewPager.adapter = adapter
            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(binding.viewPager)

            Glide.with(context)
                .load(data.pageManagementsOrNewInvestments[0].projectCoverImages.newInvestmentPageMedia.value.url)
                .into(binding.ivSmallImage)

            when (data.page.isPromotionAndOfferActive) {
                true -> {
                    Glide.with(context)
                        .load(data.page.promotionAndOffersMedia.value.url)
                        .into(binding.ivDontMissImage)
                    binding.ivDontMissImage.visibility = View.VISIBLE
                }
                false -> {
                    binding.ivDontMissImage.visibility = View.GONE
                }
            }

            binding.tvNewLaunchSeeAll.setOnClickListener(onItemClickListener)
            binding.clPlaceInfo.setOnClickListener(onItemClickListener)

            binding.tvApplyNow.setOnClickListener(onItemClickListener)
            binding.ivDontMissImage.setOnClickListener(onItemClickListener)
            binding.clBtnDiscover.setOnClickListener(onItemClickListener)
        }
    }

    private inner class LastFewPlotsViewHolder(private val binding: LastFewPlotsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvSmartDealsTitle.text = data.page.collectionOne.heading
            binding.tvSmartDealsSubtitle.text = data.page.collectionOne.subHeading
            val list = data.pageManagementsOrCollectionOneModels
            val itemsCount = data.page.collectionOne.totalProjectContentsToDisplay
            val showList = ArrayList<PageManagementsOrCollectionOneModel>()
            if (!list.isNullOrEmpty() && itemsCount<=list.size){
                for (i in 0 until itemsCount) {
                    showList.add(list[i])
                }
            } else{
                for (i in 0 until list.size-1) {
                    showList.add(list[i])
                }
            }

            lastFewPlotsAdapter = LastFewPlotsAdapter(context, showList, itemClickListener)
            binding.rvSmartDealsNv.adapter = lastFewPlotsAdapter
            binding.tvSmartDealsSeeAll.setOnClickListener(onItemClickListener)
        }
    }

    private inner class TrendingProjectsViewHolder(private val binding: TrendingProjectsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvTrendingProjectsTitle.text = data.page.collectionTwo.heading
            binding.tvTrendingProjectsSubtitle.text = data.page.collectionTwo.subHeading

            val list = data.pageManagementsOrCollectionTwoModels
            val itemsCount = data.page.collectionTwo.totalProjectContentsToDisplay
            val showList = ArrayList<PageManagementsOrCollectionTwoModel>()

            if (!list.isNullOrEmpty() && itemsCount<=list.size){
                for (i in 0 until itemsCount) {
                    showList.add(list[i])
                }
            } else{
                for (i in 0 until list.size-1) {
                    showList.add(list[i])
                }
            }

            trendingProjectsAdapter = TrendingProjectsAdapter(context, showList, itemClickListener)
            binding.rvTrendingProjects.adapter = trendingProjectsAdapter
            binding.tvTrendingProjectsSeeAll.setOnClickListener(onItemClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].viewType
    }

    fun setItemClickListener(clickListener: View.OnClickListener) {
        onItemClickListener = clickListener
    }

}