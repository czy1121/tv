package me.reezy.cosmo.tv.marquee

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import me.reezy.cosmo.R
import kotlin.math.min


/**
 * 文本跑马灯效果
 *
 * TextView 开启跑马灯效果
 * ```
 * isSingleLine = true
 * ellipsize = TextUtils.TruncateAt.MARQUEE
 * isSelected = true
 * ```
 *
 * */
class MarqueeTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 14f * resources.displayMetrics.density
    }
    private val clip = RectF()

    private var mScrollWidth: Int = 0
    private var mScrollX = 0
    private var mScrollStartAt = System.currentTimeMillis()

    private var mText: String? = null
    private var mSpeed: Float = 50f

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MarqueeTextView)

        if (a.hasValue(R.styleable.MarqueeTextView_android_textColor)) {
            paint.color = a.getColor(R.styleable.MarqueeTextView_android_textColor, Color.BLACK)
        }
        if (a.hasValue(R.styleable.MarqueeTextView_android_textSize)) {
            paint.textSize = a.getDimension(R.styleable.MarqueeTextView_android_textSize, paint.textSize)
        }
        if (a.hasValue(R.styleable.MarqueeTextView_android_fontFamily)) {
            paint.typeface = a.getFont(R.styleable.MarqueeTextView_android_fontFamily)
        }
        if (a.hasValue(R.styleable.MarqueeTextView_android_text)) {
            mText = a.getString(R.styleable.MarqueeTextView_android_text)
        }
        a.recycle()

    }


    var text: String?
        get() = mText
        set(value) {
            if (mText == value) return
            mText = value
            requestLayout()
            postInvalidate()
        }

    var textSize: Float
        get() = paint.textSize
        set(value) {
            paint.textSize = value
            if (!mText.isNullOrEmpty()) {
                requestLayout()
                postInvalidate()
            }
        }

    var textColor: Int
        get() = paint.color
        set(value) {
            paint.color = value
            postInvalidate()
        }

    var typeface: Typeface
        get() = paint.typeface
        set(value) {
            paint.typeface = value
            if (!mText.isNullOrEmpty()) {
                requestLayout()
                postInvalidate()
            }
        }


    var speed: Float
        get() = mSpeed
        set(value) {
            mSpeed = value
            invalidate()
        }

    var isRunning: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            if (field) {
                mScrollStartAt = System.currentTimeMillis()
            }
            invalidate()
        }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val textWidth = paint.measureText(mText ?: "").toInt()
        val textHeight = (paint.fontMetrics.descent - paint.fontMetrics.ascent).toInt()

        val w = chooseSize(widthMeasureSpec, textWidth + paddingLeft + paddingRight)
        val h = chooseSize(heightMeasureSpec, textHeight + paddingTop + paddingBottom)
        setMeasuredDimension(w, h)

        mScrollWidth = measuredWidth + textWidth

        clip.set(paddingLeft.toFloat(), paddingTop.toFloat(), measuredWidth - paddingRight.toFloat(), measuredHeight - paddingBottom.toFloat())
    }


    override fun onDraw(canvas: Canvas) {

        val text = mText ?: return


        if (isRunning) {
            val now = System.currentTimeMillis()
            val distance = (now - mScrollStartAt) / 1000f * mSpeed * resources.displayMetrics.density
            mScrollX = measuredWidth - (measuredWidth / 2f + distance).toInt() % mScrollWidth

            val count = canvas.saveLayer(clip, null)
            canvas.drawText(text, mScrollX.toFloat(), paddingTop - paint.ascent(), paint)
            canvas.restoreToCount(count)

            invalidate()
        } else {
            canvas.drawText(text, mScrollX.toFloat(), paddingTop - paint.ascent(), paint)
        }
    }

    private fun chooseSize(spec: Int, size: Int): Int {
        val specMode = MeasureSpec.getMode(spec)
        val specSize = MeasureSpec.getSize(spec)
        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> min(size, specSize)
            else -> size
        }
    }
}