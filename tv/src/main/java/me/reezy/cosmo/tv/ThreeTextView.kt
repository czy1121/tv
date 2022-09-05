package me.reezy.cosmo.tv

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.BoringLayout
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import me.reezy.cosmo.R
import kotlin.math.max

class ThreeTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : AppCompatTextView(context, attrs, defStyle) {

    companion object {
        const val TERTIARY_GRAVITY_CENTER: Int = 0
        const val TERTIARY_GRAVITY_TOP: Int = 1
        const val TERTIARY_GRAVITY_BOTTOM: Int = 2
    }

    private var mText2: CharSequence? = null
    private var mText2Layout: Layout? = null
    private val mText2Paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var mText2Padding: Int = 0

    private var mText3: CharSequence? = null
    private var mText3Layout: Layout? = null
    private val mText3Paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var mText3Padding: Int = 0
    private var mText3MaxWidth: Int = 0
    private var mText3Gravity: Int = 0

    private var mLeftIcon: Drawable? = null
    private var mLeftIconSize: Int = 0
    private var mLeftIconPadding: Int = 0
    private var mLeftIconTint: ColorStateList? = null

    private var mRightIcon: Drawable? = null
    private var mRightIconSize: Int = 0
    private var mRightIconPadding: Int = 0
    private var mRightIconTint: ColorStateList? = null

    private var mRemeasure: Boolean = false

    init {
        val defaultColor2 = context.resolveColor(android.R.attr.textColorSecondary, 0xff999999.toInt())
        val defaultColor3 = context.resolveColor(android.R.attr.textColorTertiary, 0xff999999.toInt())

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.ThreeTextView)

        val text2Color = a.getColor(R.styleable.ThreeTextView_tvText2Color, defaultColor2)
        val text2Size = a.getDimension(R.styleable.ThreeTextView_tvText2Size, 0.75f * textSize)
        val text2Font = a.getResourceId(R.styleable.ThreeTextView_tvText2Font, 0)
        mText2 = a.getString(R.styleable.ThreeTextView_tvText2)
        mText2Padding = a.getDimensionPixelSize(R.styleable.ThreeTextView_tvText2Padding, 0)

        val text3Color = a.getColor(R.styleable.ThreeTextView_tvText3Color, defaultColor3)
        val text3Size = a.getDimension(R.styleable.ThreeTextView_tvText3Size, 0.75f * textSize)
        val text3Font = a.getResourceId(R.styleable.ThreeTextView_tvText3Font, 0)
        mText3 = a.getString(R.styleable.ThreeTextView_tvText3)
        mText3Padding = a.getDimensionPixelSize(R.styleable.ThreeTextView_tvText3Padding, 0)
        mText3MaxWidth = a.getDimensionPixelSize(R.styleable.ThreeTextView_tvText3MaxWidth, (120 * resources.displayMetrics.density).toInt())
        mText3Gravity = a.getInt(R.styleable.ThreeTextView_tvText3Gravity, 0)

        mLeftIconSize = a.getDimensionPixelSize(R.styleable.ThreeTextView_tvLeftIconSize, 0)
        mLeftIconPadding = a.getDimensionPixelSize(R.styleable.ThreeTextView_tvLeftIconPadding, 0)
        mLeftIconTint = a.getColorStateList(R.styleable.ThreeTextView_tvLeftIconTint)
        mLeftIcon = a.getDrawable(R.styleable.ThreeTextView_tvLeftIcon)?.tint(mLeftIconTint)

        mRightIconSize = a.getDimensionPixelSize(R.styleable.ThreeTextView_tvRightIconSize, 0)
        mRightIconPadding = a.getDimensionPixelSize(R.styleable.ThreeTextView_tvRightIconPadding, 0)
        mRightIconTint = a.getColorStateList(R.styleable.ThreeTextView_tvRightIconTint)
        mRightIcon = a.getDrawable(R.styleable.ThreeTextView_tvRightIcon)?.tint(mRightIconTint)

        a.recycle()


        mText2Paint.textAlign = Paint.Align.LEFT
        mText2Paint.color = text2Color
        mText2Paint.textSize = text2Size
        mText2Paint.typeface = getFontFamily(text2Font)

        mText3Paint.textAlign = Paint.Align.LEFT
        mText3Paint.color = text3Color
        mText3Paint.textSize = text3Size
        mText3Paint.typeface = getFontFamily(text3Font)

        textAlignment = View.TEXT_ALIGNMENT_GRAVITY

        super.setGravity(Gravity.LEFT or Gravity.TOP)
        super.setHorizontallyScrolling(false)
    }


    override fun setHorizontallyScrolling(whether: Boolean) {}

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
    }

    override fun setGravity(gravity: Int) {}

    override fun getCompoundPaddingLeft(): Int {
        val icon = mLeftIcon ?: return super.getPaddingLeft()
        val size = if (mLeftIconSize > 0) mLeftIconSize else icon.intrinsicWidth
        return super.getPaddingLeft() + size + mLeftIconPadding
    }

    override fun getCompoundPaddingRight(): Int {
        val icon = mRightIcon ?: return super.getPaddingRight()
        val size = if (mRightIconSize > 0) mRightIconSize else icon.intrinsicWidth

        val text3Width = when {
            mText3Layout == null -> 0
            mText3Gravity == TERTIARY_GRAVITY_BOTTOM -> 0
            else -> mText3Layout!!.getLineWidth(0).toInt() + mText3Padding
        }

        return super.getPaddingRight() + size + mRightIconPadding + text3Width
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mText3Layout = obtainLayout(mText3Layout, mText3, mText3Paint, mText3MaxWidth)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingHorizontal()

        val text3Width = if (mText3Layout == null) 0 else (mText3Layout!!.getLineWidth(0).toInt() + mText3Padding)

        val text2MaxWidth = when (mText3Gravity) {
            TERTIARY_GRAVITY_TOP -> width
            else -> width - text3Width
        }

        mText2Layout = obtainLayout(mText2Layout, mText2, mText2Paint, text2MaxWidth)

        if (mText2Layout != null || mText3Layout != null) {
            val hm = MeasureSpec.getMode(heightMeasureSpec)
            if (hm != MeasureSpec.EXACTLY) {
                val text1Height = layout.height
                val text2Height = mText2Layout?.height ?: 0
                val padding = if (text1Height > 0 && text2Height > 0) mText2Padding else 0
                val height = compoundPaddingTop + compoundPaddingBottom + max(text1Height + padding + text2Height, mText3Layout?.height ?: 0)
                setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(max(measuredHeight, height), hm))
            }
        }
        mRemeasure = false
    }

    override fun onDraw(canvas: Canvas) {

        val top = compoundPaddingTop
        val textHeight = max(layout.height + mText2Padding + (mText2Layout?.height ?: 0), mText3Layout?.height ?: 0)
        val viewHeight = height - top - compoundPaddingBottom

        val offsetY = (viewHeight - textHeight) / 2

        canvas.save()
        canvas.translate(0f, offsetY.toFloat())
        super.onDraw(canvas)
        canvas.restore()

        drawIcons(canvas, top + offsetY, textHeight)
        drawTexts(canvas, top + offsetY, textHeight)
    }


    private fun drawTexts(canvas: Canvas, textY: Int, textHeight: Int) {

        mText2Layout?.let {
            canvas.save()
            canvas.translate(compoundPaddingLeft.toFloat(), textY.toFloat() + layout.height + mText2Padding)
            it.draw(canvas)
            canvas.restore()
        }
        mText3Layout?.let {
            canvas.save()
            val x = when (mText3Gravity) {
                TERTIARY_GRAVITY_BOTTOM -> width.toFloat() - compoundPaddingRight - it.getLineWidth(0)
                else -> width.toFloat() - compoundPaddingRight + mText3Padding
            }

            val y = when (mText3Gravity) {
                TERTIARY_GRAVITY_TOP -> textY
                TERTIARY_GRAVITY_BOTTOM -> textY + textHeight - it.height
                else -> textY + (textHeight - it.height) / 2
            }
            canvas.translate(x, y.toFloat())
            it.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawIcons(canvas: Canvas, textY: Int, textHeight: Int) {
        mLeftIcon?.let {
            val size = if (mLeftIconSize > 0) mLeftIconSize else it.intrinsicWidth
            val x = super.getPaddingLeft()
            val y = textY + (textHeight - size) / 2
            it.setBounds(x, y, x + size, y + size)
            it.draw(canvas)
        }
        mRightIcon?.let {
            val size = if (mRightIconSize > 0) mRightIconSize else it.intrinsicWidth
            val x = width - super.getPaddingLeft() - size
            val y = textY + (textHeight - size) / 2
            it.setBounds(x, y, x + size, y + size)
            it.draw(canvas)
        }
    }

    var leftIcon: Drawable?
        get() = mLeftIcon
        set(value) {
            if (mLeftIcon != value) {
                if (mLeftIconSize == 0 && mLeftIcon?.intrinsicWidth != value?.intrinsicWidth) {
                    mRemeasure = true
                    requestLayout()
                } else {
                    postInvalidate()
                }
                mLeftIcon = value?.tint(mLeftIconTint)
            }
        }
    var leftIconSize: Int
        get() = mLeftIconSize
        set(value) {
            if (mLeftIconSize != value) {
                mLeftIconSize = value
                relayout()
            }
        }
    var leftIconPadding: Int
        get() = mLeftIconPadding
        set(value) {
            if (mLeftIconPadding != value) {
                mLeftIconPadding = value
                relayout()
            }
        }
    var leftIconTint: ColorStateList?
        get() = mLeftIconTint
        set(value) {
            if (mLeftIconTint != value) {
                mLeftIconTint = value
                mLeftIcon = mLeftIcon?.tint(value)
                postInvalidate()
            }
        }


    var rightIcon: Drawable?
        get() = mRightIcon
        set(value) {
            if (mRightIcon != value) {
                if (mRightIconSize == 0 && mRightIcon?.intrinsicWidth != value?.intrinsicWidth) {
                    mRemeasure = true
                    requestLayout()
                } else {
                    postInvalidate()
                }
                mRightIcon = value?.tint(mRightIconTint)
            }
        }
    var rightIconSize: Int
        get() = mRightIconSize
        set(value) {
            if (mRightIconSize != value) {
                mRightIconSize = value
                relayout()
            }
        }
    var rightIconPadding: Int
        get() = mRightIconPadding
        set(value) {
            if (mRightIconPadding != value) {
                mRightIconPadding = value
                relayout()
            }
        }
    var rightIconTint: ColorStateList?
        get() = mRightIconTint
        set(value) {
            if (mRightIconTint != value) {
                mRightIconTint = value
                mRightIcon = mRightIcon?.tint(value)
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

    var text3: CharSequence?
        get() = mText3
        set(value) {
            if (mText3 != value) {
                mText3 = value
                relayout()
            }
        }
    var text3Size: Float
        get() = mText3Paint.textSize
        set(value) {
            if (mText3Paint.textSize != value) {
                mText3Paint.textSize = value
                relayout()
            }
        }
    var text3Padding: Int
        get() = mText3Padding
        set(value) {
            if (mText3Padding != value) {
                mText3Padding = value
                relayout()
            }
        }
    var text3Color: Int
        get() = mText3Paint.color
        set(value) {
            if (mText3Paint.color != value) {
                mText3Paint.color = value
                postInvalidate()
            }
        }
    var text3Gravity: Int
        get() = mText3Gravity
        set(value) {
            if (mText3Gravity != value) {
                mText3Gravity = value
                postInvalidate()
            }
        }



    private fun relayout() {
        mRemeasure = true
        requestLayout()
        postInvalidate()
    }

    @Suppress("deprecation")
    private fun obtainLayout(layout: Layout?, source: CharSequence?, paint: TextPaint, maxWidth: Int): Layout? {

        if (source.isNullOrEmpty()) return null
        if (maxWidth < 1) return null

        if (!mRemeasure && layout != null &&layout.text == source) return layout

        val metrics = BoringLayout.isBoring(source, paint)
        val alignment = Layout.Alignment.ALIGN_NORMAL
        return when {
            metrics != null && metrics.width < maxWidth -> BoringLayout.make(source, paint, maxWidth, alignment, 1.0f, 0f, metrics, false)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> StaticLayout.Builder.obtain(source, 0, source.length, paint, maxWidth).setAlignment(alignment).setIncludePad(false).build()
            else -> StaticLayout(source, paint, maxWidth, alignment, 1.0f, 0f, false)
        }
    }

    private fun getPaddingHorizontal(): Int {
        val padding = super.getPaddingLeft() + super.getPaddingRight()

        val left = when {
            mLeftIcon == null -> 0
            mLeftIconSize > 0 -> mLeftIconSize + mLeftIconPadding
            else -> mLeftIcon!!.intrinsicWidth + mLeftIconPadding
        }
        val right = when {
            mRightIcon == null -> 0
            mRightIconSize > 0 -> mRightIconSize + mRightIconPadding
            else -> mRightIcon!!.intrinsicWidth + mRightIconPadding
        }
        return padding + left + right
    }

    private fun Drawable.tint(tint: ColorStateList?): Drawable {
        return DrawableCompat.wrap(this).mutate().also {
            DrawableCompat.setTintList(it, tint)
        }
    }

    private fun getFontFamily(resId: Int): Typeface? {
        if (resId > 0) {
            return ResourcesCompat.getFont(context, resId) ?: paint.typeface
        }
        return paint.typeface
    }

    private fun Context.resolveColor(attrId: Int, defaultColor: Int): Int {
        val outValue = TypedValue()
        theme.resolveAttribute(attrId, outValue, true)

        if (outValue.resourceId > 0 && outValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && outValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return ContextCompat.getColor(this, outValue.resourceId)
        }
        return defaultColor
    }
}