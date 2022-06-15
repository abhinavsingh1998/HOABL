package com.emproto.hoabl.feature.home.adapters

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emproto.core.Utility
import com.emproto.core.textviews.CustomTextView
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.ItemInsightsBinding
import com.emproto.networklayer.response.home.PageManagementOrInsight

class InsightsAdapter(
    val context: Context,
    val list: List<PageManagementOrInsight>,
    val itemIntrface: InsightsItemInterface
) : RecyclerView.Adapter<InsightsAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemInsightsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list.get(holder.adapterPosition)
        holder.binding.tvVideotitle.text = item.displayTitle
        holder.binding.shortDesc.text = showHTMLText(item.insightsMedia[0].description)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Utility.convertStringIns(holder.binding.shortDesc,context,showHTMLText(item.insightsMedia[0].description).toString(),2)
        }

        if(item.insightsMedia[0].media!=null){
                when(item.insightsMedia[0].media.value.mediaType){
                    "VIDEO" -> {
                        val url = item.insightsMedia[0].media.value.url.replace("https://www.youtube.com/embed/","")
                        val youtubeUrl = "https://img.youtube.com/vi/${url}/hqdefault.jpg"

                        holder.binding.playBtn.isVisible= true
                        Glide.with(context)
                            .load(youtubeUrl)
                            .into(holder.binding.image)
                    }
                    else -> {
                        Glide.with(context)
                            .load(item.insightsMedia[0].media.value.url)
                            .into(holder.binding.image)
                    }
                }}

        when{
            item.insightsMedia[0].description.isNullOrEmpty() -> {
                holder.binding.btnReadMore.visibility = View.GONE
            }
        }
        holder.binding.tvVideotitle.text= item.displayTitle

        holder.binding.rootView.setOnClickListener {
            itemIntrface.onClickItem(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(val binding: ItemInsightsBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface InsightsItemInterface {
        fun onClickItem(position: Int)
    }

    public fun showHTMLText(message: String?): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(message)
        }
    }
}

