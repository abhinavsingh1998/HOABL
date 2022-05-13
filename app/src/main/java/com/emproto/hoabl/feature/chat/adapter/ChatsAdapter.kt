package com.emproto.hoabl.feature.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.ItemChatBinding
import com.emproto.networklayer.response.chats.Chat

class ChatsAdapter(
    private var mContext: Context?,
    private var chatList: List<Chat>,
    private var mListener: OnItemClickListener

) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    lateinit var binding: ItemChatBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    interface OnItemClickListener {
        fun onChatItemClick(chat: Chat, view: View, position: Int)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        binding.tvTitle.text = chatList[position].project[position].launchName
        binding.tvMsg.text = chatList[position].lastMessage.toString()
//        binding.tvTime.text = chatRequest[position].data[position].project[position].

        mContext?.let {
            Glide.with(it)
                .load(chatList[position].project[position].projectCoverImages[position].chatListViewPageMedia[position].value[position].url)
                .placeholder(R.drawable.ic_baseline_image_24).into(binding.ivThumb)
        }

        binding.clChat.setOnClickListener {
            mListener.onChatItemClick(chatList[position], it, position)
        }

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
//        val imageView: ImageView = itemView.findViewById(R.id.ivThumb)
//        val topic: TextView = itemView.findViewById(R.id.tvTitle)
//        val desc: TextView = itemView.findViewById(R.id.tvMsg)
//        val time: TextView = itemView.findViewById(R.id.tvTime)

    }


}