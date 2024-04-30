package com.app.faizanzw.ui.postLogin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.faizanzw.R
import com.app.faizanzw.databinding.RowSearchPaymentBinding
import com.app.faizanzw.ui.postLogin.fragments.search.model.NewSearchPayment
import com.app.faizanzw.utils.Extension.convertTodate
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter

interface PaymentOprationListener {
    fun onEdit(data: NewSearchPayment, position: Int)
    fun onInProgress(id: String, position: Int, status: String)
    fun onComplete(id: String, position: Int, status: String)
}

class PendingPaymentAdapter(
    val context: Context,
    val arrayList: ArrayList<NewSearchPayment>,
    val listener: PaymentOprationListener,
    val userId:Int
) :
    RecyclerSwipeAdapter<PendingPaymentAdapter.PaymentHolder>() {


    fun setData(arrayList: ArrayList<NewSearchPayment>) {
        this.arrayList.clear()
        this.arrayList.addAll(arrayList)
        notifyDataSetChanged()
    }

    fun updateStatus(status: String, position: Int) {
        arrayList[position].tranStatus = status
        notifyItemChanged(position)
    }

    inner class PaymentHolder(val binding: RowSearchPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NewSearchPayment, position: Int) {
            //binding.model = item
            binding.swipe.showMode = SwipeLayout.ShowMode.PullOut
            binding.swipe.addDrag(SwipeLayout.DragEdge.Left, binding.left)
            binding.swipe.addDrag(SwipeLayout.DragEdge.Right, binding.right)
            binding.swipe.isSwipeEnabled = false

            if (item.tranStatus.equals("PENDING") && item.paymentTo == userId) {
                binding.btnAccept.isVisible = true
                binding.btnReject.isVisible = true
            } else {
                binding.btnAccept.isVisible = false
                binding.btnReject.isVisible = false
            }


            with(binding)
            {
                txtPaymentNo.text =
                    context.resources.getString(
                        R.string.with_bold_double,
                        "Payment No:",
                        item.tranNo
                    )
                txtDate.text =
                    context.resources.getString(
                        R.string.with_bold_double,
                        "",
                        item.trandate.convertTodate()
                    )
                txtSentBy.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Sent by: ",
                    item.sentByName
                )
                txtReceivedBy.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Received by: ",
                    item.reciveByName
                )
                txtNetAmount.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Amount: ",
                    item.amount
                )
                txtStatus.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Status: ",
                    item.tranStatus
                )
                txtCurrency.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Currency Type: ",
                    item.currencyType
                )

                txtViewMode.isVisible = isAbleToRedirect(item)

                txtViewMode.debouncedOnClick() {
                    listener.onEdit(item, position)
                }

                btnAccept.debouncedOnClick {
                    listener.onInProgress(item.tranID.toString(), position, "ACCEPT")
                }

                btnReject.debouncedOnClick {
                    //swipe.close()
                    listener.onInProgress(item.tranID.toString(), position, "REJECT")
                }

            }

        }

        fun swipeListener() {
            binding.swipe.addSwipeListener(object : SimpleSwipeListener() {
                override fun onStartOpen(layout: SwipeLayout?) {
                    closeAllExcept(layout)
                }
            })
        }
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    override fun onBindViewHolder(holder: PaymentHolder, position: Int) {
        mItemManger.bindView(holder.itemView, position)
        holder.bind(arrayList[position], position)
        holder.swipeListener()
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentHolder {
        return PaymentHolder(
            RowSearchPaymentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private fun isAbleToRedirect(data: NewSearchPayment): Boolean {
        if (data.paymentTo.toInt() ==
            userId || data.CreatedBy == userId
        )
            return true
        return false
    }


}