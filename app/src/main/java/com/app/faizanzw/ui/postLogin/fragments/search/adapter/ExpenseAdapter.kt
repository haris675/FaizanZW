package com.app.faizanzw.ui.postLogin.fragments.search.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.app.faizanzw.R
import com.app.faizanzw.databinding.RowExpenseItemBinding
import com.app.faizanzw.ui.postLogin.fragments.search.ExpenseSearch
import com.app.faizanzw.utils.Extension.convertTodate
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter

interface ExpenseOprationListener {
    fun onEdit(data: ExpenseSearch, position: Int)
}

class ExpenseAdapter(
    val context: Context,
    val arrayList: ArrayList<ExpenseSearch>,
    val listener: ExpenseOprationListener,
    val userId:Int
) :
    RecyclerSwipeAdapter<ExpenseAdapter.ExpenseHolder>() {

    fun setData(arrayList: ArrayList<ExpenseSearch>) {
        this.arrayList.clear()
        this.arrayList.addAll(arrayList)
        notifyDataSetChanged()
    }

    fun updateItem(position: Int, data: ExpenseSearch) {
        arrayList.get(position).netAmount = data.netAmount
        arrayList.get(position).paymentType = data.paymentType
        notifyItemChanged(position)
    }

    fun updateStatus(status: String, position: Int) {
        arrayList[position].expenseStatus = status
        notifyItemChanged(position)
    }

    inner class ExpenseHolder(val binding: RowExpenseItemBinding) : ViewHolder(binding.root) {
        fun bind(item: ExpenseSearch, position: Int) {
            with(binding)
            {
                txtExpenseNo.text =
                    context.resources.getString(
                        R.string.with_bold_double,
                        "Expense No : ",
                        item.tranNo
                    )
                txtDate.text =
                    context.resources.getString(
                        R.string.with_bold_double,
                        "",
                        item.trandate.convertTodate()
                    )
                txtPartyName.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Party Name : ",
                    item.partyName
                )
                /*txtExpenseType.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Expense Type : ",
                    item.accGroupName
                )*/
                txtPaymentType.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Payment Type : ",
                    item.paymentType
                )
                txtNetAmount.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Net Amount : ",
                    item.netAmount
                )
                txtStatus.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Status : ",
                    item.expenseStatus
                )
                txtCurrencyType.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Currency Type : ",
                    item.currencyType
                )
                txtAccGroupName.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Expense Type : ",
                    item.accGroupName.trim()
                )
                /*txtCurrency.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Priority",
                    item.currencey
                )*/

                txtViewMore.isVisible = item.TranPartyKey == userId

                txtViewMore.debouncedOnClick {
                    listener.onEdit(item, position)
                }

                imgEdit.debouncedOnClick() {
                    listener.onEdit(item, position)
                }


            }
            binding.swipe.showMode = SwipeLayout.ShowMode.PullOut
            binding.swipe.isLeftSwipeEnabled = false
            binding.swipe.isRightSwipeEnabled = false
            binding.swipe.addDrag(SwipeLayout.DragEdge.Left, binding.left)
            binding.swipe.addDrag(SwipeLayout.DragEdge.Right, binding.right)
        }

        fun swipeListener() {
            binding.swipe.addSwipeListener(object : SimpleSwipeListener() {
                override fun onStartOpen(layout: SwipeLayout?) {
                    closeAllExcept(layout)
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseHolder {
        return ExpenseHolder(
            RowExpenseItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ExpenseHolder, position: Int) {
        mItemManger.bindView(holder.itemView, position)
        holder.bind(arrayList[position], position)
        holder.swipeListener()
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

}