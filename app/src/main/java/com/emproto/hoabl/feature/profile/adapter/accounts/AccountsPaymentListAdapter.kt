package com.emproto.hoabl.feature.profile.adapter.accounts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.AccNoDataBinding
import com.emproto.hoabl.databinding.ItemAccountsKycDocBinding
import com.emproto.hoabl.databinding.ItemAccountsPaymentBinding
import com.emproto.networklayer.response.profile.AccountsResponse
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AccountsPaymentListAdapter(
    private var mContext: Context?,
    private var accountsPaymentList: ArrayList<AccountsResponse.Data.PaymentHistory>,
    private var mListener: OnPaymentItemClickListener,
    private  var type: String

) : RecyclerView.Adapter<AccountsPaymentListAdapter.BaseViewHolder>() {

    lateinit var binding: ItemAccountsPaymentBinding
    lateinit var bindingEmpty: AccNoDataBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (viewType == R.layout.item_accounts_payment) {
            binding =
                ItemAccountsPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return NotEmptyList(binding.root)
        } else {
            bindingEmpty =
                AccNoDataBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

            return EmptyList(bindingEmpty.root)
        }

    }
    override fun getItemViewType(position: Int): Int {
        if (type == "not") {
            return R.layout.item_accounts_payment
        }
        else {
            return R.layout.acc_no_data
        }
    }
    class NotEmptyList(ItemView: View) : AccountsPaymentListAdapter.BaseViewHolder(ItemView) {
        val tvProjectName: TextView = itemView.findViewById(R.id.tvProjectName)
        val tvPaidAmount: TextView = itemView.findViewById(R.id.tvPaidAmount)
        val tvPaymentDate: TextView = itemView.findViewById(R.id.tvPaymentDate)
        val tvSeeReceipt: TextView = itemView.findViewById(R.id.tvSeeReceipt)
        val tvLandId: TextView = itemView.findViewById(R.id.tvLandId)
    }

    class EmptyList(ItemView: View) : AccountsPaymentListAdapter.BaseViewHolder(ItemView) {
        var tvMsg = itemView.findViewById<TextView>(R.id.tvMsg)
    }
    abstract class BaseViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {}

    interface OnPaymentItemClickListener {
        fun onAccountsPaymentItemClick(
            accountsPaymentList: ArrayList<AccountsResponse.Data.PaymentHistory>,
            view: View,
            position: Int,
            name: String,
            path: String,
        )
    }
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if(holder is NotEmptyList) {
            if (!accountsPaymentList[position].paidAmount.toString().isNullOrEmpty()) {
                holder.tvPaidAmount.text = "₹" + accountsPaymentList[position].paidAmount.toString()
            }
            if (!accountsPaymentList[position].launchName.isNullOrEmpty()) {
                holder.tvProjectName.text = accountsPaymentList[position].launchName
            }
            if (!accountsPaymentList[position].paymentDate.isNullOrEmpty()) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val outputFormat = SimpleDateFormat("dd-MM-yyyy")
                val date: Date = inputFormat.parse(accountsPaymentList[position].paymentDate)
                val formattedDate: String = outputFormat.format(date)
                holder.tvPaymentDate.text = formattedDate
            }
            if (accountsPaymentList[position].crmInventory != null) {
                holder.tvLandId.text =
                    "Land id:" + "" + accountsPaymentList[position].crmInventory
            }

            holder.tvSeeReceipt.setOnClickListener {
                if (accountsPaymentList[position].document == null) {
                    Toast.makeText(mContext, "No Receipt", Toast.LENGTH_SHORT).show()
                } else {
                    mListener.onAccountsPaymentItemClick(
                        accountsPaymentList,
                        it,
                        position,
                        accountsPaymentList[position].document?.name.toString(),
                        accountsPaymentList[position].document?.path.toString()
                    )

                }

            }
        }else if(holder is EmptyList){
            holder.tvMsg.text="No Payment is done yet,See your payment history after making your first payment."
        }
    }

    override fun getItemCount(): Int {
        return if (accountsPaymentList.size < 4) {
            accountsPaymentList.size
        } else {
            return 4

        }
    }


}