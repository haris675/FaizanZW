package com.app.faizanzw.ui.postLogin.fragments.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.faizanzw.R
import com.app.faizanzw.database.DBDeliveryTasK
import com.app.faizanzw.databinding.RowTaskSearchItemBinding
import com.app.faizanzw.utils.Extension.convertTodate
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.daimajia.swipe.SwipeLayout

interface OprationListener {
    fun onEdit(data: Any, position: Int)
}

class DeliveryAdapter(
    var context: Context,
    val arrayList: ArrayList<DBDeliveryTasK>,
    val listener: OprationListener
) : RecyclerView.Adapter<DeliveryAdapter.DeliveryHolder>() {

    fun setData(arrayList: ArrayList<DBDeliveryTasK>) {
        arrayList.clear()
        arrayList.addAll(arrayList)
        notifyDataSetChanged()
    }

    fun updateData(data: DBDeliveryTasK, position: Int) {
        arrayList.set(position, data)
        notifyDataSetChanged()
    }

    inner class DeliveryHolder(val binding: RowTaskSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DBDeliveryTasK, position: Int) {
            with(binding)
            {
                txtSubject.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Subject: ",
                    item.subject
                )
                txtTaskNo.text =
                    context.resources.getString(R.string.with_bold_double, "Task No: ", item.tranNo)
                txtDate.text =
                    context.resources.getString(
                        R.string.with_bold_double,
                        "",
                        item.trandate.convertTodate()
                    )
                txtCreatedBy.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Created by: ",
                    item.createdByName
                )
                txtAssigned.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Aiisgned To: ",
                    item.assignToName
                )
                txtTaskPriorirt.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Priority: ",
                    item.taskPriority
                )
                txtDescription.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Description: ",
                    item.taskDescription
                )
                txtStatus.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Status: ",
                    item.tranStatus
                )
                txtTranType.text = context.resources.getString(
                    R.string.with_bold_double,
                    "Tran Type: ",
                    item.tranTypeName
                )

                //txtViewMore.isVisible = isAbleToRedirect(item)

                txtViewMore.debouncedOnClick {
                    listener.onEdit(item, position)
                }
            }
            binding.swipe.showMode = SwipeLayout.ShowMode.PullOut
            binding.swipe.isClickToClose = true
            binding.swipe.isSwipeEnabled = false
            binding.swipe.addDrag(SwipeLayout.DragEdge.Left, binding.left)
            binding.swipe.addDrag(SwipeLayout.DragEdge.Right, binding.right)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryHolder {
        return DeliveryHolder(
            RowTaskSearchItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: DeliveryHolder, position: Int) {
        holder.bind(arrayList[position], position)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}