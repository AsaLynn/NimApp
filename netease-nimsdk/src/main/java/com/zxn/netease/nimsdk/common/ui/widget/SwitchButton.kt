package com.zxn.netease.nimsdk.common.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.zxn.netease.nimsdk.R

/**
 * 仿iphone 开关按钮
 *
 */
class SwitchButton : View, OnTouchListener {
    var isChoose = false // 记录当前按钮是否打开,true为打开,flase为关闭
        private set
    private var isChecked = false
    private var onSlip = false // 记录用户是否在滑动的变量
    private var down_x = 0f
    private var now_x // 按下时的x,当前的x
            = 0f
    private var btn_off: Rect? = null
    private var btn_on // 打开和关闭状态下,游标的Rect .
            : Rect? = null
    private var isChangeOn = false
    private var isInterceptOn = false
    private var onChangedListener: OnChangedListener? = null
    private lateinit var bg_on: Bitmap
    private lateinit var bg_off: Bitmap
    private lateinit var slip_btn: Bitmap

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    private fun init() { // 初始化
        bg_on = BitmapFactory.decodeResource(resources, R.drawable.nim_slide_toggle_on)
        bg_off = BitmapFactory.decodeResource(resources, R.drawable.nim_slide_toggle_off)
        slip_btn = BitmapFactory.decodeResource(resources, R.drawable.nim_slide_toggle)
        btn_off = Rect(0, 0, slip_btn!!.width, slip_btn.height)
        btn_on = Rect(
            bg_off.width - slip_btn.width,
            0,
            bg_off.width,
            slip_btn.height
        )
        setOnTouchListener(this) // 设置监听器,也可以直接复写OnTouchEvent
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) { // 绘图函数
        super.onDraw(canvas)
        val matrix = Matrix()
        val paint = Paint()
        var x: Float
        // 滑动到前半段与后半段的背景不同,在此做判断
        if (now_x < bg_on!!.width / 2) {
            x = now_x - slip_btn!!.width / 2
            canvas.drawBitmap(bg_off!!, matrix, paint) // 画出关闭时的背景
        } else {
            x = bg_on!!.width - slip_btn!!.width / 2.toFloat()
            canvas.drawBitmap(bg_on!!, matrix, paint) // 画出打开时的背景
        }
        // 是否是在滑动状态
        if (onSlip) {
            x = if (now_x >= bg_on!!.width) { // 是否划出指定范围,不能让游标跑到外头,必须做这个判断
                bg_on!!.width - slip_btn!!.width / 2.toFloat() // 减去游标1/2的长度...
            } else if (now_x < 0) {
                0f
            } else {
                now_x - slip_btn!!.width / 2
            }
        } else { // 非滑动状态
            if (isChoose) { // 根据现在的开关状态设置画游标的位置
                x = btn_on!!.left.toFloat()
                canvas.drawBitmap(bg_on!!, matrix, paint) // 初始状态为true时应该画出打开状态图片
            } else {
                x = btn_off!!.left.toFloat()
            }
        }
        if (isChecked) {
            canvas.drawBitmap(bg_on!!, matrix, paint)
            x = btn_on!!.left.toFloat()
            isChecked = !isChecked
        }

        // 对游标位置进行异常判断...
        if (x < 0) {
            x = 0f
        } else if (x > bg_on!!.width - slip_btn!!.width) {
            x = bg_on!!.width - slip_btn!!.width.toFloat()
        }
        canvas.drawBitmap(slip_btn!!, x, 0f, paint) // 画出游标.
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val old = isChoose
        when (event.action) {
            MotionEvent.ACTION_MOVE -> now_x = event.x
            MotionEvent.ACTION_DOWN -> {
                if (event.x > bg_on!!.width || event.y > bg_on!!.height) {
                    return false
                }
                onSlip = true
                down_x = event.x
                now_x = down_x
            }
            MotionEvent.ACTION_CANCEL -> {
                onSlip = false
                val choose = isChoose
                if (now_x >= bg_on!!.width / 2) {
                    now_x = bg_on!!.width - slip_btn!!.width / 2.toFloat()
                    isChoose = true
                } else {
                    now_x = now_x - slip_btn!!.width / 2
                    isChoose = false
                }
                if (isChangeOn && choose != isChoose) { // 如果设置了监听器,就调用其方法..
                    onChangedListener!!.OnChanged(this, isChoose)
                }
            }
            MotionEvent.ACTION_UP -> {
                onSlip = false
                val lastChoose = isChoose
                if (event.x >= bg_on!!.width / 2) {
                    now_x = bg_on!!.width - slip_btn!!.width / 2.toFloat()
                    isChoose = true
                } else {
                    now_x = now_x - slip_btn!!.width / 2
                    isChoose = false
                }
                if (lastChoose == isChoose) { // 相等表示点击状态未切换，之后切换状态
                    if (event.x >= bg_on!!.width / 2) {
                        now_x = 0f
                        isChoose = false
                    } else {
                        now_x = bg_on!!.width - slip_btn!!.width / 2.toFloat()
                        isChoose = true
                    }
                }
                // 如果设置了监听器,就调用其方法..
                if (isChangeOn) {
                    onChangedListener!!.OnChanged(this, isChoose)
                }
            }
            else -> {
            }
        }
        if (!old && isInterceptOn) {
            isChoose = false
        } else {
            invalidate() // 重画控件
        }
        return true
    }

    fun setOnChangedListener(listener: OnChangedListener?) { // 设置监听器,当状态修改的时候
        isChangeOn = true
        onChangedListener = listener
    }

    interface OnChangedListener {
        fun OnChanged(v: View?, checkState: Boolean)
    }

    var check: Boolean
        get() = isChecked
        set(isChecked) {
            this.isChecked = isChecked
            isChoose = isChecked
            if (isChecked == false) {
                now_x = 0f
            }
            invalidate()
        }

    fun setInterceptState(isIntercept: Boolean) { // 设置监听器,是否在重画钱拦截事件,状态由false变true时 拦截事件
        isInterceptOn = isIntercept
        // onInterceptListener = listener;
    }
}