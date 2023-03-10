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
import com.emproto.hoabl.databinding.ProjectUpdatesItemBinding
import com.emproto.networklayer.response.marketingUpdates.Data

class AllLatestUpdatesAdapter(
    val context: Context,
    val list: List<Data>,
    private val listCount: Int,
    private val itemInterface: UpdatesItemsInterface
) : RecyclerView.Adapter<AllLatestUpdatesAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            ProjectUpdatesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[holder.adapterPosition]
        holder.binding.title.text = item.displayTitle
        if (!item.subTitle.isNullOrEmpty()) {
            holder.binding.placeName.text = item.subTitle
        } else {
            holder.binding.placeName.isVisible = false
        }

        if (item.detailedInfo[0].media != null) {
            if (!item.detailedInfo[0].media.value.url.isNullOrEmpty()) {
                holder.binding.imageCard.isVisible = true
                Glide.with(context).load(item.detailedInfo[0].media.value.url)
                    .into(holder.binding.locationImage)
            } else {
                holder.binding.imageCard.isVisible = false
            }

            if (!item.detailedInfo[0].description.isNullOrEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Utility.convertString(
                        holder.binding.deatils,
                        context,
                        showHTMLText(item.detailedInfo[0].description).toString(),
                        2
                    )
                }
            } else {
                holder.binding.btnReadMore.isVisible = false
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

    inner class MyViewHolder(val binding: ProjectUpdatesItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface UpdatesItemsInterface {
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

