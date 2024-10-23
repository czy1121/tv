package me.reezy.cosmo.tv

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.text.BoringLayout
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import me.reezy.cosmo.R
import kotlin.math.min

class SuperTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : AppCompatTextView(context, attrs, defStyle) {

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


    private var mSubtext: CharSequence? = null
    private var mSubtextLayout: Layout? = null
    private val mSubtextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var mSubtextPadding: Int = 0
    private var mSubtextGravity: Int = 0
    private var mSubtextLeft: Int = 0
    private var mSubtextTop: Int = 0
    

    private var mIcon: Drawable? = null
    private var mIconSize: Int = 0
    private var mIconPadding: Int = 0
    private var mIconGravity: Int = 0
    private var mIconTint: ColorStateList? = null
    private var mIconLeft: Int = 0
    private var mIconTop: Int = 0

    private var mRemeasure: Boolean = false

    private var mGradientColors: IntArray? = null
    private var mGradientHeight: Float = 0f

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


    var subtextStrokeColor: Int = 0
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }

    var subtextStrokeWidth: Int = 0
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }

    init {

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.SuperTextView)

        mSubtextPaint.color = a.getColor(R.styleable.SuperTextView_tvSubtextColor, Color.BLACK)
        mSubtextPaint.textSize = a.getDimension(R.styleable.SuperTextView_tvSubtextSize, 0.75f * textSize)
        mSubtextPaint.typeface = a.getTypeface(R.styleable.SuperTextView_tvSubtextFont, R.styleable.SuperTextView_tvSubtextStyle)

        mSubtext = a.getString(R.styleable.SuperTextView_tvSubtext)
        mSubtextPadding = a.getDimensionPixelSize(R.styleable.SuperTextView_tvSubtextPadding, 0)
        mSubtextGravity = a.getInt(R.styleable.SuperTextView_tvSubtextGravity, GRAVITY_TEXT_BOTTOM)

        subtextStrokeWidth = a.getDimensionPixelSize(R.styleable.SuperTextView_tvSubtextStrokeWidth, 0)
        subtextStrokeColor = a.getColor(R.styleable.SuperTextView_tvSubtextStrokeColor, 0)


        mIconSize = a.getDimensionPixelSize(R.styleable.SuperTextView_tvIconSize, 0)
        mIconPadding = a.getDimensionPixelSize(R.styleable.SuperTextView_tvIconPadding, 0)
        mIconGravity = a.getInt(R.styleable.SuperTextView_tvIconGravity, GRAVITY_TEXT_START)
        mIconTint = a.getColorStateList(R.styleable.SuperTextView_tvIconTint)
        mIcon = a.getDrawable(R.styleable.SuperTextView_tvIcon)?.tint(mIconTint)


        strokeWidth = a.getDimensionPixelSize(R.styleable.SuperTextView_tvStrokeWidth, 0)
        strokeColor = a.getColor(R.styleable.SuperTextView_tvStrokeColor, 0)


        val letterSpacing = a.getDimensionPixelSize(R.styleable.SuperTextView_tvLetterSpacing, 0)

        val startColor = a.getColor(R.styleable.SuperTextView_tvGradiantStartColor, 0)
        val endColor = a.getColor(R.styleable.SuperTextView_tvGradiantEndColor, 0)
        if (startColor != 0 && endColor != 0) {
            mGradientColors = intArrayOf(startColor, endColor)
        }
        a.recycle()

        if (strokeColor != 0 && strokeWidth > 0 && (shadowColor == 0 || shadowRadius == 0f)) {
            setShadowLayer(strokeWidth.toFloat(), 0f, 0f, 0x01000000)
        }

        mSubtextPaint.textAlign = Paint.Align.LEFT

        textAlignment = View.TEXT_ALIGNMENT_GRAVITY

        super.setLetterSpacing(letterSpacing / textSize)
        super.setHorizontallyScrolling(false)
    }


    override fun setHorizontallyScrolling(whether: Boolean) {}

    override fun onRtlPropertiesChanged(layoutDirection: Int) {}

    override fun getCompoundPaddingLeft(): Int {
        return getCompoundSpace(GRAVITY_START) + super.getPaddingLeft()
    }

    override fun getCompoundPaddingRight(): Int {
        return getCompoundSpace(GRAVITY_END) + super.getPaddingRight()
    }

    override fun getCompoundPaddingTop(): Int {
        return getCompoundSpace(GRAVITY_TOP) + super.getCompoundPaddingTop()
    }

    override fun getCompoundPaddingBottom(): Int {
        return getCompoundSpace(GRAVITY_BOTTOM) + super.getCompoundPaddingBottom()
    }

    private fun getCompoundSpace(gravity: Int): Int {
        return getIconSpace(mIconGravity and 0xF, gravity) + getSubtextSpace(mSubtextGravity and 0xF, gravity) + getStrokeSpace()
    }

    private fun getStrokeSpace(): Int = if (strokeColor != 0 && strokeWidth > 0) strokeWidth else 0


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mSubtextLayout = obtainLayout(mSubtextLayout, mSubtext, mSubtextPaint)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        updateIcon()
        updateSubText()
        updateGradiant()
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        requestLayout()

    }


    private val textWidth: Int get() = layout.getLineWidth(0).toInt()

    private val textHeight: Int get() = layout.height
    private val textLeft: Int
        get() = when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.LEFT -> compoundPaddingLeft
            Gravity.RIGHT -> measuredWidth - compoundPaddingRight - textWidth
            else -> (measuredWidth + compoundPaddingLeft - compoundPaddingRight - textWidth) / 2
        }

    private val textTop: Int
        get() = when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
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

    private fun getSubtextSpace(gravity: Int, limitGravity: Int = gravity): Int {
        if (gravity != limitGravity) return 0

        val layout = mSubtextLayout ?: return 0

        val isHorizontal = gravity and (GRAVITY_START or GRAVITY_END) != 0

        return mSubtextPadding + if (isHorizontal) layout.width else layout.height
    }

    private fun updateIcon() {
        val icon = mIcon ?: return

        val iconWidth = if (mIconSize > 0) mIconSize else icon.intrinsicWidth
        val iconHeight = if (mIconSize > 0) mIconSize else icon.intrinsicHeight

        mIconLeft = when (mIconGravity) {
            GRAVITY_START -> super.getPaddingLeft()
            GRAVITY_END -> measuredWidth - super.getPaddingRight() - iconWidth
            GRAVITY_TEXT_START -> textLeft - getIconSpace(mIconGravity) 
            GRAVITY_TEXT_END -> textLeft + textWidth + mIconPadding 
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
            GRAVITY_TEXT_TOP -> textTop - getSubtextSpace(mSubtextGravity, mIconGravity) - getIconSpace(mIconGravity)
            GRAVITY_TEXT_BOTTOM -> textTop + textHeight + getSubtextSpace(mSubtextGravity, mIconGravity) + mIconPadding
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

    private fun updateSubText() {
        val subtextLayout = mSubtextLayout ?: return

        val subtextWidth = subtextLayout.width
        val subtextHeight = subtextLayout.height

        mSubtextLeft = when (mSubtextGravity) {
            GRAVITY_START -> super.getPaddingLeft() + getIconSpace(mIconGravity, mSubtextGravity)
            GRAVITY_END -> measuredWidth - super.getPaddingRight() - subtextWidth - getIconSpace(mIconGravity, mSubtextGravity)
            GRAVITY_TEXT_START -> textLeft - getIconSpace(mIconGravity, mSubtextGravity) - getSubtextSpace(mSubtextGravity)
            GRAVITY_TEXT_END -> textLeft + textWidth + getIconSpace(mIconGravity, mSubtextGravity) + mSubtextPadding
            GRAVITY_TOP, GRAVITY_BOTTOM -> when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> super.getPaddingLeft()
                Gravity.RIGHT -> measuredWidth - super.getPaddingRight() - subtextWidth
                else -> (measuredWidth + super.getPaddingLeft() - super.getPaddingRight() - subtextWidth) / 2
            }

            else -> when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> textLeft
                Gravity.RIGHT -> textLeft + textWidth - subtextWidth
                else -> textLeft + (textWidth - subtextWidth) / 2
            }
        }
        mSubtextTop = when (mSubtextGravity) {
            GRAVITY_TOP -> super.getPaddingTop() + getIconSpace(mIconGravity, mSubtextGravity)
            GRAVITY_BOTTOM -> measuredHeight - super.getPaddingBottom() - subtextHeight - getIconSpace(mIconGravity, mSubtextGravity)
            GRAVITY_TEXT_TOP -> textTop - getSubtextSpace(mSubtextGravity)
            GRAVITY_TEXT_BOTTOM -> textTop + textHeight + mSubtextPadding
            GRAVITY_START, GRAVITY_END -> when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> layout.getLineBaseline(0) - subtextLayout.getLineBaseline(0) + super.getPaddingTop()
                Gravity.BOTTOM -> -layout.getLineDescent(0) + subtextLayout.getLineDescent(0) + measuredHeight - super.getPaddingBottom() - subtextHeight
                else -> (measuredHeight + super.getPaddingTop() - super.getPaddingBottom() - layout.height) / 2
            }

            else -> when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> layout.getLineBaseline(0) - subtextLayout.getLineBaseline(0) + textTop
                Gravity.BOTTOM -> -layout.getLineDescent(0) + subtextLayout.getLineDescent(0) + textTop + textHeight - subtextHeight
                else -> textTop + (textHeight - subtextHeight) / 2
            }
        }
    }

    private fun updateGradiant() {
        mGradientColors?.let {
            val newHeight = layout.height.toFloat()
            if (mGradientHeight != newHeight) {
                mGradientHeight = newHeight
                paint.shader = LinearGradient(0f, 0f, 0f, newHeight, it, null, Shader.TileMode.CLAMP)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {

        if (strokeWidth > 0 && strokeColor != 0) {
            val color = textColors
            val width = paint.strokeWidth
            val shader = paint.shader

            setTextColor(strokeColor)
            paint.strokeWidth = strokeWidth.toFloat() * 2
            paint.strokeJoin = Paint.Join.ROUND
            paint.style = Paint.Style.STROKE
            paint.shader = null


            super.onDraw(canvas)

            setTextColor(color)
            paint.strokeWidth = width
            paint.style = Paint.Style.FILL
            paint.shader = shader
        }
        super.onDraw(canvas)

        mIcon?.draw(canvas)

        mSubtextLayout?.let {

            canvas.save()
            canvas.translate(mSubtextLeft.toFloat(), mSubtextTop.toFloat())

            if (subtextStrokeWidth > 0 && subtextStrokeColor != 0) {
                val paint = it.paint

                val color = paint.color
                val width = paint.strokeWidth

                paint.color = subtextStrokeColor
                paint.strokeWidth = subtextStrokeWidth.toFloat() * 2
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


    var subtext: CharSequence?
        get() = mSubtext
        set(value) {
            if (mSubtext != value) {
                mSubtext = value
                relayout()
            }
        }
    var subtextSize: Float
        get() = mSubtextPaint.textSize
        set(value) {
            if (mSubtextPaint.textSize != value) {
                mSubtextPaint.textSize = value
                relayout()
            }
        }
    var subtextPadding: Int
        get() = mSubtextPadding
        set(value) {
            if (mSubtextPadding != value) {
                mSubtextPadding = value
                relayout()
            }
        }
    var subtextColor: Int
        get() = mSubtextPaint.color
        set(value) {
            if (mSubtextPaint.color != value) {
                mSubtextPaint.color = value
                postInvalidate()
            }
        }

    var subtextGravity: Int
        get() = mSubtextGravity
        set(value) {
            if (mSubtextGravity != value) {
                mSubtextGravity = value
                relayout()
            }
        }


    private fun relayout() {
        mRemeasure = true
        requestLayout()
        postInvalidate()
    }

    private fun obtainLayout(layout: Layout?, source: CharSequence?, paint: TextPaint): Layout? {

        if (source.isNullOrEmpty()) return null

        if (layout != null && layout.text == source) return layout

        return makeLayout(source, paint)
    }

    private fun makeLayout(source: CharSequence, paint: TextPaint, maxWidth: Int = 0): Layout? {
        val metrics = BoringLayout.isBoring(source, paint)
        val alignment = Layout.Alignment.ALIGN_NORMAL
        val width = when {
            metrics != null && maxWidth > 0 -> min(metrics.width, maxWidth)
            metrics != null -> metrics.width
            maxWidth > 0 -> min(paint.measureText(source.toString()).toInt(), maxWidth)
            else -> paint.measureText(source.toString()).toInt()
        }
        if (metrics != null && (maxWidth == 0 || metrics.width < maxWidth)) {
            return BoringLayout.make(source, paint, width, alignment, 1.0f, 0f, metrics, false)
        }
        return StaticLayout.Builder.obtain(source, 0, source.length, paint, width).setAlignment(alignment).setIncludePad(false).build()
    }
}

