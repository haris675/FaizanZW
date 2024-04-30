package com.app.faizanzw.customui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.InputFilter
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.app.faizanzw.R
import com.app.faizanzw.utils.Extension.asDrawable


class CustomPrefixEditText : AppCompatEditText {

    private val mPrefix = "$" // add your prefix here for example $

    private val mPrefixRect = Rect() // actual prefix size


    constructor(context: Context) : super(context) {

    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        background = if (focused) {
            R.drawable.edittext_focus.asDrawable(context)
        } else {
            if (text.toString().trim { it <= ' ' }.isEmpty()) {
                R.drawable.edittext_unfocus.asDrawable(context)
            } else {
                R.drawable.edittext_focus.asDrawable(context)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        calculatePrefix()
    }

    var originalLeftPadding = -1f
    private fun calculatePrefix() {
        if (originalLeftPadding == -1f) {
            val prefix = tag as String
            val widths = FloatArray(prefix.length)
            paint.getTextWidths(prefix, widths)
            var textWidth = 0f
            for (w in widths) {
                textWidth += w
            }
            originalLeftPadding = compoundPaddingLeft.toFloat()
            setPadding(
                (textWidth + originalLeftPadding).toInt(),
                paddingRight, paddingTop,
                paddingBottom
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val prefix = tag as String
        canvas.drawText(prefix, originalLeftPadding, getLineBounds(0, null).toFloat(), paint)
    }

    fun setInputFilter(filters: Array<InputFilter>) {
        this.filters = filters
    }

}