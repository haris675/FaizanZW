package com.app.faizanzw.ui.postLogin.fragments.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.faizanzw.R
import com.app.faizanzw.databinding.RowAssosiateTaskBinding
import com.app.faizanzw.ui.postLogin.fragments.edit.model.AssosiateData

class TaskProgressAdapter(
    val context: Context,
    val arrayList: ArrayList<AssosiateData>,
    var isAssigned: Boolean,
    val onEdit : (position:Int,data:AssosiateData) -> Unit
) :
    RecyclerView.Adapter<TaskProgressAdapter.TaskProgressHolder>() {

    /*fun setData(arrayList: ArrayList<AssosiateData>) {
        this.arrayList.clear()
        this.arrayList.addAll(arrayList)
        notifyDataSetChanged()
    }*/

    fun setData(arrayList: ArrayList<AssosiateData>,isAssigned: Boolean)
    {
        this.isAssigned = isAssigned
        this.arrayList.clear()
        this.arrayList.addAll(arrayList)
        notifyDataSetChanged()
    }

    fun updateItem(position: Int, data: AssosiateData) {
        arrayList.set(position, data)
        notifyItemChanged(position)
    }

    inner class TaskProgressHolder(val binding: RowAssosiateTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AssosiateData,position: Int) {
            with(binding)
            {
                txtProgress.text = HtmlCompat.fromHtml(
                    "<b>Status:</b> " + item.taskStatus,
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                txtDescription.text = HtmlCompat.fromHtml(
                    "<b>Remarks:</b> " + item.description,
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                txtPercentage.text = item.progress.toString() + "%"
                progress.progress = item.progress
                binding.llOption.isVisible = isAssigned

                /*if (item.docByte.isNullOrEmpty()) {
                    imgAssosiate.setImageResource(R.drawable.img_placeholder)
                } else {
                    val byteArr = Base64.decode(item.docByte, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
                    imgAssosiate.setImageBitmap(decodedImage)
                }*/

                btnEdit.setOnClickListener {
                    onEdit(position,item)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskProgressHolder {
        return TaskProgressHolder(
            RowAssosiateTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TaskProgressHolder, position: Int) {
        holder.bind(arrayList[position],position)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

}