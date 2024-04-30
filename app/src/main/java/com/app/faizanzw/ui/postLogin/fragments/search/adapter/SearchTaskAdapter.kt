package com.app.faizanzw.ui.postLogin.fragments.search.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.faizanzw.R
import com.app.faizanzw.databinding.RowTaskSearchItemBinding
import com.app.faizanzw.ui.postLogin.eventmodel.EventTaskData
import com.app.faizanzw.ui.postLogin.fragments.search.model.TaskItem
import com.app.faizanzw.utils.Extension.convertTodate
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter

interface TaskOprationListener {
    fun onEdit(data: TaskItem, position: Int)
    fun onInProgress(id: String, position: Int, status: String)
    fun onComplete(id: String, position: Int, status: String)
}

class SearchTaskAdapter(
    val context: Context,
    val arrayList: ArrayList<TaskItem>,
    val listener: TaskOprationListener,
    val userId: Int
) :
    RecyclerSwipeAdapter<SearchTaskAdapter.TaskHolder>() {

    fun setData(arrayList: ArrayList<TaskItem>) {
        this.arrayList.clear()
        this.arrayList.addAll(arrayList)
        notifyDataSetChanged()
    }

    fun updateItem(data: EventTaskData, position: Int) {
        arrayList[position].assignToName = data.assignedTo
        arrayList[position].taskPriority = data.prioroty
        notifyItemChanged(position)
    }

    fun updateStatus(status: String, position: Int) {
        arrayList[position].tranStatus = status
        notifyItemChanged(position)
    }

    inner class TaskHolder(val binding: RowTaskSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskItem, position: Int) {
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

                txtViewMore.isVisible = isAbleToRedirect(item)

                txtViewMore.debouncedOnClick {
                    listener.onEdit(item, position)
                }

                right.debouncedOnClick {
                    //swipe.close()
                    listener.onInProgress(item.TranID.toString(), position, "INPROGRESS")
                }

                left.debouncedOnClick {
                    //swipe.close()
                    listener.onComplete(item.TranID.toString(), position, "COMPLETED")
                }


            }
            binding.swipe.showMode = SwipeLayout.ShowMode.PullOut
            binding.swipe.isClickToClose = true
            binding.swipe.isSwipeEnabled = false
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

    private fun isAbleToRedirect(data: TaskItem): Boolean {
        if (data.AssignTo.toInt() ==
            userId || data.createdBy == userId
        )
            return true
        return false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        return TaskHolder(
            RowTaskSearchItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
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