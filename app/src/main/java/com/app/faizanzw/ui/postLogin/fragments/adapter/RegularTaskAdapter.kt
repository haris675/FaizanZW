package com.app.faizanzw.ui.postLogin.fragments.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.faizanzw.databinding.RowRegularTaskBinding
import com.app.faizanzw.ui.postLogin.fragments.model.RegularData
import com.app.faizanzw.utils.Extension.convertTodate
import com.app.faizanzw.utils.Extension.debouncedOnClick

interface RegularOprationListener {
    fun onEdit(data: RegularData, position: Int)
}

class RegularTaskAdapter(
    val context: Context,
    val arrayList: ArrayList<RegularData>,
    val editListener: RegularOprationListener
) :
    RecyclerView.Adapter<RegularTaskAdapter.RegularHolder>() {

    fun setData(arrayList: ArrayList<RegularData>) {
        this.arrayList.clear()
        this.arrayList.addAll(arrayList)
        notifyDataSetChanged()
    }

    inner class RegularHolder(val binding: RowRegularTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RegularData, position: Int) {
            with(binding)
            {
                txtTaskNo.text = HtmlCompat.fromHtml(
                    "<b>Task No:</b> " + item.tranNo,
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                txtDate.text = HtmlCompat.fromHtml(
                    "<b>${item.trandate.convertTodate()}",
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                txtSubject.text = HtmlCompat.fromHtml(
                    "<b>Subject: </b> " + item.subject,
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                txtStatus.text = HtmlCompat.fromHtml(
                    "<b>Status: </b> " + item.tranStatus,
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                txtRemarks.text = HtmlCompat.fromHtml(
                    "<b>Remarks: </b> " + item.taskDescription,
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )

                txtViewMore.debouncedOnClick() {
                    editListener.onEdit(item, position)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegularHolder {
        return RegularHolder(
            RowRegularTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RegularHolder, position: Int) {
        holder.bind(arrayList[position],position)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

}