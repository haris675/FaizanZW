package com.app.faizanzw.customui

import android.content.Context
import android.graphics.Color
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.app.faizanzw.R
import com.app.faizanzw.utils.Extension.asDrawable

class CustomTextViewUi : LinearLayout {

    var mInflater: LayoutInflater
    var tvHint: TextView? = null
    var edtTitle: TextView? = null
    var rv: LinearLayout? = null
    var rlEditText: RelativeLayout? = null

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
                context.obtainStyledAttributes(attributeSet, R.styleable.CustomTextUI)
            try {
                if (typedArray.hasValue(R.styleable.CustomTextUI_hint)) {
                    tvHint!!.text = typedArray.getString(R.styleable.CustomTextUI_hint)
                }

                if (typedArray.hasValue(R.styleable.CustomTextUI_title)) {
                    edtTitle!!.setText(typedArray.getString(R.styleable.CustomTextUI_title))
                }

                if (typedArray.hasValue(R.styleable.CustomTextUI_text_color)) {
                    edtTitle!!.setTextColor(
                        typedArray.getInteger(
                            R.styleable.CustomTextUI_text_color,
                            R.color.color_primary
                        )
                    )
                }

            } finally {
                typedArray.recycle()
            }
        }
    }

    fun init() {
        val v = mInflater.inflate(R.layout.custom_textview_ui, this, true)
        tvHint = v.findViewById<View>(R.id.textHint) as TextView
        edtTitle = v.findViewById<View>(R.id.edtDesc) as TextView
        rv = v.findViewById<View>(R.id.rlContainer) as LinearLayout
        rlEditText = v.findViewById<RelativeLayout>(R.id.rlEditText) as RelativeLayout
    }

    fun disable() {
        rlEditText?.background = R.drawable.spinner_disabled.asDrawable(context)
        edtTitle?.setTextColor(Color.parseColor("#7A7A7A"))
    }

    fun setHint(hint: String) {
        tvHint!!.text = hint
    }

    fun setTitle(title: String?) {
        edtTitle!!.setText(title)
    }

    fun getTitle(): String {
        edtTitle?.let {
            return it.text.toString().trim()
        }
        return ""
    }

    fun setInputFilter(filters: Array<InputFilter>) {
        edtTitle!!.filters = filters
    }


}