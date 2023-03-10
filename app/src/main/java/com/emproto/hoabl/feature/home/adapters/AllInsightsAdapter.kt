package com.emproto.hoabl.feature.home.adapters

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emproto.core.Utility
import com.emproto.hoabl.databinding.InsightsListItemBinding
import com.emproto.networklayer.response.insights.Data

@Suppress("DEPRECATION")
class AllInsightsAdapter(
    val context: Context,
    private val listCount: Int,
    val list: List<Data>,
    private val itemInterface: InsightsItemsInterface
) : RecyclerView.Adapter<AllInsightsAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            InsightsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[holder.adapterPosition]
        holder.binding.title.text = item.displayTitle
        if (item.insightsMedia[0].description.isNullOrEmpty()) {
            holder.binding.btnReadMore.isVisible = false

        } else {
            holder.binding.deatils.text = showHTMLText(item.insightsMedia[0].description)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Utility.convertString(
                    holder.binding.deatils,
                    context,
                    showHTMLText(item.insightsMedia[0].description).toString(),
                    2
                )
            }
            if (item.insightsMedia[0].media != null) {
                when (item.insightsMedia[0].media.value.mediaType) {
                    "VIDEO" -> {
                        val url = item.insightsMedia[0].media.value.url.replace(
                            "https://www.youtube.com/embed/",
                            ""
                        )
                        val youtubeUrl = "https://img.youtube.com/vi/${url}/hqdefault.jpg"
                        Glide.with(context)
                            .load(youtubeUrl)
                            .into(holder.binding.locationImage)
                        holder.binding.playBtn.isVisible = true
                    }
                    else -> {
                        Glide.with(context)
                            .load(item.insightsMedia[0].media.value.url)
                            .into(holder.binding.locationImage)
                    }
                }
            }
        }
        holder.binding.rootView.setOnClickListener {
            itemInterface.onClickItem(holder.adapterPosition)
        }

    }

    override fun getItemCount(): Int {

        val itemList = if (list.size < listCount) {
            list.size
        } else {
            listCount
        }
        return itemList
    }

    inner class MyViewHolder(val binding: InsightsListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface InsightsItemsInterface {
        fun onClickItem(position: Int)
    }

    fun showHTMLText(message: String?): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(message)
        }
    }
}