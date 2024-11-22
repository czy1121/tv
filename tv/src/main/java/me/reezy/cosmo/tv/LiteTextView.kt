package me.reezy.cosmo.tv

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources.NotFoundException
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import me.reezy.cosmo.R
import kotlin.math.min

/**
 * 简化版 TextView，单行文本，支持图标
 *
 * 支持以下属性：
 * - android:text
 * - android:textColor
 * - android:textSize
 * - android:fontFamily
 * - android:textStyle
 * - android:gravity
 * - icon
 * - iconSize
 * - iconPadding
 * - iconGravity
 * - iconTint
 */
class LiteTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    companion object {

        const val ICON_GRAVITY_START = 0x1
        const val ICON_GRAVITY_TEXT_START = 0x2
        const val ICON_GRAVITY_END = 0x3
        const val ICON_GRAVITY_TEXT_END = 0x4
        const val ICON_GRAVITY_TOP = 0x10
        const val ICON_GRAVITY_TEXT_TOP = 0x20
    }

    val paint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 14f * resources.displayMetrics.density
    }

    private var mText: String? = null
    private var mTextGravity: Int = Gravity.START or Gravity.TOP

    private var mTextLeft: Int = 0
    private var mTextTop: Int = 0


    private var mIcon: Drawable? = null
    private var mIconSize: Int = 0
    private var mIconPadding: Int = 0
    private var mIconGravity: Int = 0
    private var mIconTint: ColorStateList? = null

    private var mIconLeft: Int = 0
    private var mIconTop: Int = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LiteTextView)

        paint.color = a.getColor(R.styleable.LiteTextView_android_textColor, Color.BLACK)
        paint.textSize = a.getDimension(R.styleable.LiteTextView_android_textSize, paint.textSize)
        paint.typeface = a.getTypeface(R.styleable.LiteTextView_android_fontFamily, R.styleable.LiteTextView_android_textStyle)

        mText = a.getString(R.styleable.LiteTextView_android_text)
        mTextGravity = a.getInt(R.styleable.LiteTextView_android_gravity, mTextGravity)

        mIconSize = a.getDimensionPixelSize(R.styleable.LiteTextView_iconSize, 0)
        mIconPadding = a.getDimensionPixelSize(R.styleable.LiteTextView_iconPadding, 0)
        mIconGravity = a.getInt(R.styleable.LiteTextView_iconGravity, 0)
        mIconTint = a.getColorStateList(R.styleable.LiteTextView_iconTint)
        mIcon = a.getDrawable(R.styleable.LiteTextView_icon)?.tint(mIconTint)

        a.recycle()

    }

    var text: String?
        get() = mText
        set(value) {
            if (mText == value) return
            mText = value
            relayout()
        }

    var textSize: Float
        get() = paint.textSize
        set(value) {
            paint.textSize = value
            if (!mText.isNullOrEmpty()) {
                relayout()
            }
        }

    var textColor: Int
        get() = paint.color
        set(value) {
            paint.color = value
            postInvalidate()
        }

    var gravity: Int
        get() = mTextGravity
        set(value) {
            if (mTextGravity != value) {
                mTextGravity = value
                relayout()
            }
        }

    var typeface: Typeface
        get() = paint.typeface
        set(value) {
            paint.typeface = value
            if (!mText.isNullOrEmpty()) {
                relayout()
            }
        }

    var icon: Drawable?
        get() = mIcon
        set(value) {
            if (mIcon != value) {
                if (mIconSize == 0 && (mIcon?.intrinsicWidth != value?.intrinsicWidth || mIcon?.intrinsicHeight != value?.intrinsicHeight)) {
                    requestLayout()
                } else {
                    postInvalidate()
                }
                mIcon = value?.tint(mIconTint)
            }
        }
    var iconSize: Int
        get() = mIconSize
        set(value) {
            if (mIconSize != value) {
                mIconSize = value
                relayout()
            }
        }
    var iconPadding: Int
        get() = mIconPadding
        set(value) {
            if (mIconPadding != value) {
                mIconPadding = value
                relayout()
            }
        }
    var iconTint: ColorStateList?
        get() = mIconTint
        set(value) {
            if (mIconTint != value) {
                mIconTint = value
                mIcon = mIcon?.tint(value)
                postInvalidate()
            }
        }
    var iconGravity: Int
        get() = mIconGravity
        set(value) {
            if (mIconGravity != value) {
                mIconGravity = value
                relayout()
            }
        }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val textWidth = paint.measureText(mText ?: "").toInt()
        val textHeight = (paint.fontMetrics.descent - paint.fontMetrics.ascent).toInt()


        val w = chooseSize(widthMeasureSpec, textWidth + getIconSpace(!isIconTop()) + paddingLeft + paddingRight)
        val h = chooseSize(heightMeasureSpec, textHeight + getIconSpace(isIconTop()) + paddingTop + paddingBottom)
        setMeasuredDimension(w, h)

        val iconWidth = mIcon?.let { if (mIconSize > 0) mIconSize else it.intrinsicWidth } ?: 0
        val iconHeight = mIcon?.let { if (mIconSize > 0) mIconSize else it.intrinsicHeight } ?: 0


        mTextLeft = when (mTextGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.LEFT -> paddingLeft + getIconSpace(isIconStart())
            Gravity.RIGHT -> measuredWidth - paddingRight - textWidth - getIconSpace(isIconEnd())
            else -> {
                val offset = when {
                    isIconStart() -> getIconSpace(true)
                    isIconEnd() -> -getIconSpace(true)
                    else -> 0
                }
                (measuredWidth + paddingLeft - paddingRight - textWidth + offset) / 2
            }
        }

        mTextTop = when (mTextGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.TOP -> paddingTop + getIconSpace(isIconTop())
            Gravity.BOTTOM -> measuredHeight - paddingBottom - textHeight
            else -> (measuredHeight + paddingTop - paddingBottom - textHeight + getIconSpace(isIconTop())) / 2
        }


        mIconLeft = when (mIconGravity) {
            ICON_GRAVITY_START -> paddingLeft
            ICON_GRAVITY_END -> measuredWidth - paddingRight - iconWidth
            ICON_GRAVITY_TEXT_START -> mTextLeft - getIconSpace(true)
            ICON_GRAVITY_TEXT_END -> mTextLeft + textWidth + mIconPadding
            else -> (measuredWidth + paddingLeft - paddingRight - iconWidth) / 2
        }
        mIconTop = when (mIconGravity) {
            ICON_GRAVITY_TOP -> paddingTop
            ICON_GRAVITY_TEXT_TOP -> mTextTop - getIconSpace(true)
            else -> (measuredHeight + paddingTop - paddingBottom - iconHeight) / 2
        }
        mIcon?.setBounds(mIconLeft, mIconTop, mIconLeft + iconWidth, mIconTop + iconHeight)

    }


    override fun onDraw(canvas: Canvas) {

        mIcon?.draw(canvas)

        mText?.let {
            canvas.drawText(it, mTextLeft.toFloat(), mTextTop - paint.ascent(), paint)
        }
    }


    private fun relayout() {
        requestLayout()
        postInvalidate()
    }

    private fun isIconTop() = mIconGravity == ICON_GRAVITY_TOP || mIconGravity == ICON_GRAVITY_TEXT_TOP
    private fun isIconStart() = mIconGravity == ICON_GRAVITY_START || mIconGravity == ICON_GRAVITY_TEXT_START
    private fun isIconEnd() = mIconGravity == ICON_GRAVITY_END || mIconGravity == ICON_GRAVITY_TEXT_END

    private fun getIconSpace(isNeed: Boolean): Int {
        if (!isNeed) return 0
        val icon = mIcon ?: return 0
        if (mIconSize > 0) return mIconPadding + mIconSize
        return mIconPadding + if (!isIconTop()) icon.intrinsicWidth else icon.intrinsicHeight
    }

    private fun chooseSize(spec: Int, size: Int): Int {
        val specMode = View.MeasureSpec.getMode(spec)
        val specSize = View.MeasureSpec.getSize(spec)
        return when (specMode) {
            View.MeasureSpec.EXACTLY -> specSize
            View.MeasureSpec.AT_MOST -> min(size, specSize)
            else -> size
        }
    }
}