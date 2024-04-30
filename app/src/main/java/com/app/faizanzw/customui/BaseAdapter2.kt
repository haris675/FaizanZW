package com.app.faizanzw.customui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BaseAdapter2<S>(
    private val mContext: Context?,
    private var list: List<S>?,//generic list can take any Object
    private val layoutId: Int,// the item_layout
    private val listener: BaseAdapterListener? = null// call back inside your view
) : RecyclerView.Adapter<BaseAdapter2.DataBindingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
        val layoutInflater = LayoutInflater.from(mContext)
        return DataBindingViewHolder(
            DataBindingUtil.inflate(
                layoutInflater,
                layoutId,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    fun getItem(position: Int): S {
        list?.let {
            if (position < it.size)
                return it[position]
        }
        return list?.get(position) ?: null as S
    }

    override fun onBindViewHolder(holder: DataBindingViewHolder, position: Int) {
        listener?.onBind(holder, position)
    }

    interface BaseAdapterListener {
        fun onBind(holder: DataBindingViewHolder, position: Int)
    }

    fun refreshAdapter() {
        notifyDataSetChanged()
    }

    class DataBindingViewHolder(val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root)
}