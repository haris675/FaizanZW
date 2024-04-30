package com.app.faizanzw.customui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.app.faizanzw.R
import com.app.faizanzw.utils.Extension.asDrawable

class CustomSpinnerUi<T> : LinearLayout {

    var mInflater: LayoutInflater
    var tvHint: TextView? = null
    var spinner: Spinner? = null
    var iv: ImageView? = null
    var rv: LinearLayout? = null
    var tvError: TextView? = null
    var bgSpinner: RelativeLayout? = null

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

                /*if (typedArray.hasValue(R.styleable.CustomDropDownView_title)) {
                    tvTitle!!.text = typedArray.getString(R.styleable.CustomDropDownView_title)
                }*/

                if (typedArray.hasValue(R.styleable.CustomDropDownView_android_visible)) {
                    iv!!.visibility =
                        typedArray.getInt(R.styleable.CustomDropDownView_android_visible, 2)
                }
            } finally {
                typedArray.recycle()
            }
        }
    }

    fun init() {
        val v = mInflater.inflate(R.layout.custom_spinner_ui, this, true)
        tvHint = v.findViewById<View>(R.id.textView1) as TextView
        spinner = v.findViewById<View>(R.id.spinner) as Spinner
        iv = v.findViewById<View>(R.id.imageView) as ImageView
        rv = v.findViewById<View>(R.id.rlContainer) as LinearLayout
        tvError = v.findViewById<TextView>(R.id.txtError) as TextView
        bgSpinner = v.findViewById(R.id.rlSpinnerBackground) as RelativeLayout
    }

    fun initAdapter(list: ArrayList<T>, listener: AdapterView.OnItemSelectedListener) {
        spinner?.adapter = SpinnerAdapter<T>(
            list,
            context,
            R.layout.spinner_textview,
            LayoutInflater.from(context)
        )
        spinner?.onItemSelectedListener = listener
    }

    fun disableSpinner() {
        spinner?.isEnabled = false
        iv?.isVisible = false
        bgSpinner!!.setBackgroundResource(R.drawable.spinner_disabled)
    }

    fun setSelection(position: Int) {
        spinner?.let {
            it.setSelection(position)
        }
    }

    fun setHint(hint: String) {
        tvHint!!.text = hint
    }

    fun setImageVisibility(value: Int) {
        iv!!.visibility = value
    }

    fun setError(errorMessage: String) {
        tvError!!.setText(errorMessage)
        tvError!!.visibility = View.VISIBLE
        bgSpinner?.background = R.drawable.spinner_beginner_red.asDrawable(context)
    }

    fun hideError() {
        tvError!!.visibility = View.GONE
        bgSpinner?.background = R.drawable.spinner_rounded_bg.asDrawable(context)
    }


}