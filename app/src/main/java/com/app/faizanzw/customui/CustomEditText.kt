package com.app.faizanzw.customui

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.app.faizanzw.R
import com.app.faizanzw.utils.Extension.asDrawable

class CustomEditText : LinearLayout, OnFocusChangeListener {

    var mInflater: LayoutInflater
    var tvHint: TextView? = null
    var tvError: TextView? = null
    var edtTitle: EditText? = null
    var rv: LinearLayout? = null
    var rvEditText: RelativeLayout? = null
    var edtView: View? = null

    constructor(context: Context?) : super(context!!) {
        mInflater = LayoutInflater.from(context)
        init()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
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
                context.obtainStyledAttributes(/* set = */ attributeSet, /* attrs = */
                    R.styleable.CustomEditTextView
                )
            try {
                if (typedArray.hasValue(R.styleable.CustomEditTextView_hint)) {
                    tvHint!!.text = typedArray.getString(R.styleable.CustomEditTextView_hint)
                }

                if (typedArray.hasValue(R.styleable.CustomEditTextView_title)) {
                    edtTitle!!.setText(typedArray.getString(R.styleable.CustomEditTextView_title))
                }

                if (typedArray.hasValue(R.styleable.CustomEditTextView_android_maxLength)) {
                    edtTitle!!.filters = arrayOf<InputFilter>(
                        InputFilter.LengthFilter(
                            typedArray.getInteger(
                                R.styleable.CustomEditTextView_android_maxLength,
                                10
                            )
                        )
                    )
                }

                if (typedArray.hasValue(R.styleable.CustomEditTextView_android_inputType)) {
                    edtTitle!!.inputType =
                        typedArray.getInteger(R.styleable.CustomEditTextView_android_inputType, 0)
                }

                if (typedArray.hasValue(R.styleable.CustomEditTextView_android_digits)) {
                    edtTitle!!.keyListener =
                        DigitsKeyListener.getInstance(typedArray.getString(R.styleable.CustomEditTextView_android_digits)!!)
                    Log.e(
                        "dsadsadasdsadsa : ",
                        typedArray.getString(R.styleable.CustomEditTextView_android_digits)!!
                    )
                }

                if (typedArray.hasValue(R.styleable.CustomEditTextView_android_maxLines)) {
                    edtTitle!!.maxLines =
                        typedArray.getInteger(R.styleable.CustomEditTextView_android_maxLines, 1)
                }

            } finally {
                typedArray.recycle()
            }
        }
    }

    fun init() {
        val v = mInflater.inflate(R.layout.custom_editext_ui, this, true)
        tvHint = v.findViewById<View>(R.id.textHint) as TextView
        tvError = v.findViewById<View>(R.id.txtError) as TextView
        rvEditText = v.findViewById<View>(R.id.rlEditText) as RelativeLayout
        edtTitle = v.findViewById<View>(R.id.edtDesc) as EditText
        rv = v.findViewById<View>(R.id.rlContainer) as LinearLayout
        setTextWatcher()
    }

    fun setTextWatcher() {
        edtTitle?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                hideError()
            }
        })
    }

    fun setError(error: String) {
        tvError?.setText(error)
        tvError?.visibility = View.VISIBLE
        rvEditText?.background = R.drawable.spinner_beginner_red.asDrawable(context)
    }

    fun hideError() {
        tvError?.setText("")
        tvError?.visibility = View.GONE
        rvEditText?.background = R.drawable.spinner_rounded_bg.asDrawable(context)
    }

    fun disable() {
        Handler().postDelayed({
            edtTitle?.isEnabled = false
            edtTitle?.isFocusable = false
            edtTitle?.setTextColor(Color.parseColor("#7a7a7a"))
            rvEditText?.background = R.drawable.spinner_disabled.asDrawable(context)
        }, 2000)
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

    override fun onFocusChange(v: View?, hasFocus: Boolean) {

        /*edtView?.let {
            it.setBackgroundColor(
                if (hasFocus) {
                    R.color.color_primary.asColor(context)
                } else {
                    if (edtTitle?.text.toString().trim { it <= ' ' }.isEmpty()) {
                        R.color.divider.asColor(context)
                    } else {
                        R.color.color_primary.asColor(context)
                    }
                }
            )
        }*/
    }
}