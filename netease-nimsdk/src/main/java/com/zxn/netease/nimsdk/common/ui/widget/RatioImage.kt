package com.zxn.netease.nimsdk.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.zxn.netease.nimsdk.R

class RatioImage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var ratioWidth = 0f
    private var ratioHeight = 0f

    // 0 for width
    // 1 for height
    private var standard = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = measuredWidth
        var height = measuredHeight
        when (standard) {
            sEnumWidth ->                 // 以width为准
                height = (width / ratioWidth * ratioHeight).toInt()
            sEnumHeight ->                 // 以height为准
                width = (height / ratioHeight * ratioWidth).toInt()
        }
        setMeasuredDimension(width, height)
    }

    companion object {
        private const val sEnumWidth = 0
        private const val sEnumHeight = 1
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.RatioImage, defStyleAttr, 0)
        if (ta != null) {
            ratioWidth = ta.getFloat(R.styleable.RatioImage_ri_ratio_width, 1f)
            ratioHeight = ta.getFloat(R.styleable.RatioImage_ri_ratio_height, 1f)
            standard = ta.getInt(R.styleable.RatioImage_ri_standard, 0)
            ta.recycle()
        }
    }
}