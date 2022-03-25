package com.android.norram.patienter

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView


class SofTextView(context: Context?, attrs: AttributeSet?) : AppCompatTextView(context!!, attrs) {
    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (text.toString() == "〇") {
            setTextColor(Color.rgb(255, 80, 80))
        } else if (text.toString() == "X") {
            setTextColor(Color.rgb(0, 160, 255))
        }
    }
}