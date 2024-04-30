package com.app.faizanzw.customui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.app.faizanzw.R

class SpinnerAdapter<T>(
    val list: ArrayList<T>,
    val mContext: Context,
    val resource: Int,
    val inflater: LayoutInflater
) : ArrayAdapter<T>(mContext, resource, list) {

    override fun getItem(position: Int): T? {
        return list[position]
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            //val inflater = (context as Fragment).layoutInflater
            convertView = inflater.inflate(resource, parent, false)
            convertView.setPadding(0,convertView.paddingTop,convertView.paddingRight,convertView.paddingBottom)
        }
        try {
            val cityAutoCompleteView = convertView!!.findViewById<View>(R.id.txtSpinner) as TextView
            cityAutoCompleteView.text = list[position].toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }


}