package me.reezy.cosmo.tv

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import me.reezy.cosmo.R

class TwoTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : AppCompatTextView(context, attrs, defStyle) {

    companion object {
        const val GRAVITY_START: Int = 0x1
        const val GRAVITY_END: Int = 0x2
        const val GRAVITY_TOP: Int = 0x4
        const val GRAVITY_BOTTOM: Int = 0x8

        const val GRAVITY_TEXT_START: Int = 0x11
        const val GRAVITY_TEXT_END: Int = 0x12
        const val GRAVITY_TEXT_TOP: Int = 0x14
        const val GRAVITY_TEXT_BOTTOM: Int = 0x18
    }


    private var mText2: CharSequence? = null
    private var mText2Layout: Layout? = null
    private val mText2Paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var mText2Padding: Int = 0
    private var mText2Gravity: Int = 0
    private var mText2Left: Int = 0
    private var mText2Top: Int = 0

    private var mIcon: Drawable? = null
    private var mIconSize: Int = 0
    private var mIconPadding: Int = 0
    private var mIconGravity: Int = 0
    private var mIconTint: ColorStateList? = null
    private var mIconLeft: Int = 0
    private var mIconTop: Int = 0

    private var mRemeasure: Boolean = false


    var strokeColor: Int = 0
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }

    var strokeWidth: Int = 0
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }


    var text2StrokeColor: Int = 0
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }

    var text2StrokeWidth: Int = 0
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }

    init {

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.TwoTextView)

        mText2Paint.color = a.getColor(R.styleable.TwoTextView_tvText2Color, Color.BLACK)
        mText2Paint.textSize = a.getDimension(R.styleable.TwoTextView_tvText2Size, 0.75f * textSize)
        mText2Paint.typeface = a.getTypeface(R.styleable.TwoTextView_tvText2Font, R.styleable.TwoTextView_tvText2Style)

        mText2 = a.getString(R.styleable.TwoTextView_tvText2)
        mText2Padding = a.getDimensionPixelSize(R.styleable.TwoTextView_tvText2Padding, 0)
        mText2Gravity = a.getInt(R.styleable.TwoTextView_tvText2Gravity, GRAVITY_TEXT_BOTTOM)


        mIconSize = a.getDimensionPixelSize(R.styleable.TwoTextView_tvIconSize, 0)
        mIconPadding = a.getDimensionPixelSize(R.styleable.TwoTextView_tvIconPadding, 0)
        mIconGravity = a.getInt(R.styleable.TwoTextView_tvIconGravity, GRAVITY_TEXT_START)
        mIconTint = a.getColorStateList(R.styleable.TwoTextView_tvIconTint)
        mIcon = a.getDrawable(R.styleable.TwoTextView_tvIcon)?.tint(mIconTint)

        strokeWidth = a.getDimensionPixelSize(R.styleable.TwoTextView_strokeWidth, 0)
        strokeColor = a.getColor(R.styleable.TwoTextView_strokeColor, 0)

        text2StrokeWidth = a.getDimensionPixelSize(R.styleable.TwoTextView_tvText2StrokeWidth, 0)
        text2StrokeColor = a.getColor(R.styleable.TwoTextView_tvText2StrokeColor, 0)
        a.recycle()


        mText2Paint.textAlign = Paint.Align.LEFT

        textAlignment = View.TEXT_ALIGNMENT_GRAVITY

        super.setHorizontallyScrolling(false)
        super.setIncludeFontPadding(false)
    }


    override fun setHorizontallyScrolling(whether: Boolean) {}

    override fun onRtlPropertiesChanged(layoutDirection: Int) {}

    override fun getCompoundPaddingLeft(): Int {
        return getCompoundSpace(GRAVITY_START) + super.getPaddingLeft()
    }

    override fun getCompoundPaddingRight(): Int {
        return getCompoundSpace(GRAVITY_END) + super.getPaddingLeft()
    }

    override fun getCompoundPaddingTop(): Int {
        return getCompoundSpace(GRAVITY_TOP) + super.getCompoundPaddingTop()
    }

    override fun getCompoundPaddingBottom(): Int {
        return getCompoundSpace(GRAVITY_BOTTOM) + super.getCompoundPaddingBottom()
    }

    private fun getCompoundSpace(gravity: Int): Int {
        return getIconSpace(mIconGravity and 0xF, gravity) + getText2Space(mText2Gravity and 0xF, gravity)
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mText2Layout = obtainLayout(mText2Layout, mText2, mText2Paint)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        updateIcon()
        updateText2()
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        requestLayout()
    }


    private val textWidth: Int get() = layout.getLineWidth(0).toInt()

    private val textHeight: Int get() = layout.height
    private val textLeft: Int get() = when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
        Gravity.LEFT -> compoundPaddingLeft
        Gravity.RIGHT -> measuredWidth - compoundPaddingRight - textWidth
        else -> (measuredWidth + compoundPaddingLeft - compoundPaddingRight - textWidth) / 2
    }

    private val textTop: Int get() = when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
        Gravity.TOP -> compoundPaddingTop
        Gravity.BOTTOM -> measuredHeight - compoundPaddingBottom - textHeight
        else -> (measuredHeight + compoundPaddingTop - compoundPaddingBottom - textHeight) / 2
    }

    private fun getIconSpace(gravity: Int, limitGravity: Int = gravity): Int {
        if (gravity != limitGravity) return 0

        if (mIconSize > 0) return mIconPadding + mIconSize

        val icon = mIcon ?: return 0

        val isHorizontal = gravity and (GRAVITY_START or GRAVITY_END) != 0

        return mIconPadding + if (isHorizontal) icon.intrinsicWidth else icon.intrinsicHeight
    }

    private fun getText2Space(gravity: Int, limitGravity: Int = gravity): Int {
        if (gravity != limitGravity) return 0

        val layout = mText2Layout ?: return 0

        val isHorizontal = gravity and (GRAVITY_START or GRAVITY_END) != 0

        return mText2Padding + if (isHorizontal) layout.width else layout.height
    }
    private fun updateIcon() {
        val icon = mIcon ?: return

        val iconWidth = if (mIconSize > 0) mIconSize else icon.intrinsicWidth
        val iconHeight = if (mIconSize > 0) mIconSize else icon.intrinsicHeight

        mIconLeft = when (mIconGravity) {
            GRAVITY_START -> super.getPaddingLeft()
            GRAVITY_END -> measuredWidth - super.getPaddingRight() - iconWidth
            GRAVITY_TEXT_START -> textLeft - getIconSpace(mIconGravity) // - getText2Space(mText2Gravity, mIconGravity)
            GRAVITY_TEXT_END -> textLeft + textWidth + mIconPadding //  + getText2Space(mText2Gravity, mIconGravity)
            GRAVITY_TOP, GRAVITY_BOTTOM -> when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> super.getPaddingLeft()
                Gravity.RIGHT -> measuredWidth - super.getPaddingRight() - iconWidth
                else -> (measuredWidth + super.getPaddingLeft() - super.getPaddingRight() - iconWidth) / 2
            }
            else -> when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> textLeft
                Gravity.RIGHT -> textLeft + textWidth - iconWidth
                else -> textLeft + (textWidth - iconWidth) / 2
            }
        }
        mIconTop = when (mIconGravity) {
            GRAVITY_TOP -> super.getPaddingTop()
            GRAVITY_BOTTOM -> measuredHeight - super.getPaddingBottom() - iconHeight
            GRAVITY_TEXT_TOP -> textTop - getText2Space(mText2Gravity, mIconGravity) - getIconSpace(mIconGravity)
            GRAVITY_TEXT_BOTTOM -> textTop + textHeight + getText2Space(mText2Gravity, mIconGravity) + mIconPadding
            GRAVITY_START, GRAVITY_END -> when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> super.getPaddingTop()
                Gravity.BOTTOM -> measuredHeight - super.getPaddingBottom() - iconHeight
                else -> (measuredHeight + super.getPaddingTop() - super.getPaddingBottom() - iconHeight) / 2
            }
            else -> when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> textTop
                Gravity.BOTTOM -> textTop + textHeight - iconHeight
                else -> textTop + (textHeight - iconHeight) / 2
            }
        }
        icon.setBounds(mIconLeft, mIconTop, mIconLeft + iconWidth, mIconTop + iconHeight)
    }

    private fun updateText2() {
        val layout2 = mText2Layout ?: return

        val text2Width = layout2.width
        val text2Height = layout2.height

        mText2Left = when (mText2Gravity) {
            GRAVITY_START -> super.getPaddingLeft() + getIconSpace(mIconGravity, mText2Gravity)
            GRAVITY_END -> measuredWidth - super.getPaddingRight() - text2Width - getIconSpace(mIconGravity, mText2Gravity)
            GRAVITY_TEXT_START -> textLeft - getIconSpace(mIconGravity, mText2Gravity) - getText2Space(mText2Gravity)
            GRAVITY_TEXT_END -> textLeft + textWidth + getIconSpace(mIconGravity, mText2Gravity) + mText2Padding
            GRAVITY_TOP, GRAVITY_BOTTOM -> when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> super.getPaddingLeft()
                Gravity.RIGHT -> measuredWidth - super.getPaddingRight() - text2Width
                else -> (measuredWidth + super.getPaddingLeft() - super.getPaddingRight() - text2Width) / 2
            }
            else -> when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> textLeft
                Gravity.RIGHT -> textLeft + textWidth - text2Width
                else -> textLeft + (textWidth - text2Width) / 2
            }
        }
        mText2Top = when (mText2Gravity) {
            GRAVITY_TOP -> super.getPaddingTop() + getIconSpace(mIconGravity, mText2Gravity)
            GRAVITY_BOTTOM -> measuredHeight - super.getPaddingBottom() - text2Height - getIconSpace(mIconGravity, mText2Gravity)
            GRAVITY_TEXT_TOP -> textTop - getText2Space(mText2Gravity)
            GRAVITY_TEXT_BOTTOM -> textTop + textHeight + mText2Padding
            GRAVITY_START, GRAVITY_END -> when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> super.getPaddingTop()
                Gravity.BOTTOM -> measuredHeight - super.getPaddingBottom() - text2Height
                else -> (measuredHeight + super.getPaddingTop() - super.getPaddingBottom() - text2Height) / 2
            }
            else -> when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> textTop
                Gravity.BOTTOM -> textTop + textHeight - text2Height
                else -> textTop + (textHeight - text2Height) / 2
            }
        }
    }

    override fun onDraw(canvas: Canvas) {

        if (strokeWidth > 0 && strokeColor != 0) {
            val color = textColors
            val width = paint.strokeWidth

            setTextColor(strokeColor)
            paint.strokeWidth = strokeWidth.toFloat()
            paint.style = Paint.Style.STROKE
            super.onDraw(canvas)

            setTextColor(color)
            paint.strokeWidth = width
            paint.style = Paint.Style.FILL
        }
        super.onDraw(canvas)

        mIcon?.draw(canvas)

        mText2Layout?.let {

            canvas.save()
            canvas.translate(mText2Left.toFloat(), mText2Top.toFloat())

            if (text2StrokeWidth > 0 && text2StrokeColor != 0) {
                val paint = it.paint

                val color = paint.color
                val width = paint.strokeWidth

                paint.color = text2StrokeColor
                paint.strokeWidth = text2StrokeWidth.toFloat()
                paint.style = Paint.Style.STROKE
                it.draw(canvas)

                paint.color = color
                paint.strokeWidth = width
                paint.style = Paint.Style.FILL
            }
            it.draw(canvas)
            canvas.restore()
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


    var text2: CharSequence?
        get() = mText2
        set(value) {
            if (mText2 != value) {
                mText2 = value
                relayout()
            }
        }
    var text2Size: Float
        get() = mText2Paint.textSize
        set(value) {
            if (mText2Paint.textSize != value) {
                mText2Paint.textSize = value
                relayout()
            }
        }
    var text2Padding: Int
        get() = mText2Padding
        set(value) {
            if (mText2Padding != value) {
                mText2Padding = value
                relayout()
            }
        }
    var text2Color: Int
        get() = mText2Paint.color
        set(value) {
            if (mText2Paint.color != value) {
                mText2Paint.color = value
                postInvalidate()
            }
        }

    var text2Gravity: Int
        get() = mText2Gravity
        set(value) {
            if (mText2Gravity != value) {
                mText2Gravity = value
                relayout()
            }
        }


    private fun relayout() {
        mRemeasure = true
        requestLayout()
        postInvalidate()
    }
}

