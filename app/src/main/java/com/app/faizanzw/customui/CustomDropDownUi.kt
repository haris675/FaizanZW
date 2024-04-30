package com.app.faizanzw.customui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.app.faizanzw.R

class CustomDropDownUi : RelativeLayout {

    var mInflater: LayoutInflater
    var tvHint: TextView? = null
    var tvTitle: TextView? = null
    var iv: ImageView? = null
    var rv: LinearLayout? = null

    constructor(context: Context?) : super(context!!) {
        mInflater = LayoutInflater.from(context)
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        mInflater = LayoutInflater.from(context)
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        mInflater = LayoutInflater.from(context)
        init()
        if (attributeSet != null) {
            val typedArray =
                context.obtainStyledAttributes(attributeSet, R.styleable.CustomDropDownView)
            try {
                if (typedArray.hasValue(R.styleable.CustomDropDownView_hint)) {
                    tvHint!!.text = typedArray.getString(R.styleable.CustomDropDownView_hint)
                }

                if (typedArray.hasValue(R.styleable.CustomDropDownView_title)) {
                    tvTitle!!.text = typedArray.getString(R.styleable.CustomDropDownView_title)
                }

                if (typedArray.hasValue(R.styleable.CustomDropDownView_android_visible)) {
                    iv!!.visibility =
                        typedArray.getInt(R.styleable.CustomDropDownView_android_visible, 2)
                }
                /*if (typedArray.hasValue(R.styleable.CustomDropDownView_image)) {
                    iv!!.setImageResource(
                        typedArray.getResourceId(
                            R.styleable.CustomDropDownView_image,
                            R.drawable.ic_time_green
                        )
                    )
                }*/
            } finally {
                typedArray.recycle()
            }
        }
    }

    fun init() {
        val v = mInflater.inflate(R.layout.custom_rescue_dropdown, this, true)
        tvHint = v.findViewById<View>(R.id.textView1) as TextView
        tvTitle = v.findViewById<View>(R.id.textView2) as TextView
        iv = v.findViewById<View>(R.id.imageView) as ImageView
        rv = v.findViewById<View>(R.id.rlContainer) as LinearLayout
    }

    fun setHint(hint: String) {
        tvHint!!.text = hint
    }

    fun setTitle(title: String?) {
        tvTitle!!.text = title
    }

    fun getTitle(): String {
        tvTitle?.let {
            return it.text.toString()
        }
        return ""
    }

    fun setImageVisibility(value: Int) {
        iv!!.visibility = value
    }

}