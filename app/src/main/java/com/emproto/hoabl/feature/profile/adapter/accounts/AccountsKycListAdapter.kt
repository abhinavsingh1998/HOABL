package com.emproto.hoabl.feature.profile.adapter.accounts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.ItemAccountsKycDocBinding
import com.emproto.networklayer.response.profile.AccountsResponse

class AccountsKycListAdapter(
    private var mContext: Context?,
    private var accountsKycList: ArrayList<AccountsResponse.Data.Document>,
    private var mListener: OnKycItemClickListener

) : RecyclerView.Adapter<AccountsKycListAdapter.ViewHolder>() {

    lateinit var binding: ItemAccountsKycDocBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding =
            ItemAccountsKycDocBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    interface OnKycItemClickListener {
        fun onAccountsKycItemClick(
            accountsDocumentList: ArrayList<AccountsResponse.Data.Document>,
            view: View,
            position: Int
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(accountsKycList[position].documentCategory=="KYC" && accountsKycList[position].documentCategory!=null){
            holder.tvDocName.text = accountsKycList[position].documentType
        }
        holder.tvViewDoc.setOnClickListener {
            mListener.onAccountsKycItemClick(accountsKycList, it, position)
        }
    }

    override fun getItemCount(): Int {
        return accountsKycList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvDocName: TextView = itemView.findViewById(R.id.tvDocName)
        val tvViewDoc: TextView = itemView.findViewById(R.id.tvViewDoc)
    }
}