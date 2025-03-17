package me.reezy.cosmo.tv

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.BoringLayout
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.color.utilities.ColorUtils
import me.reezy.cosmo.R
import kotlin.math.max
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

        const val STROKE_MODE_NORMAL: Int = 0
        const val STROKE_MODE_PATH: Int = 1

        private val hasLetterSpacingIssue by lazy {
            val paint = TextPaint()
            paint.textSize = 100f
            paint.typeface = Typeface.MONOSPACE
            paint.letterSpacing = 0f
            val w1 = paint.measureText(".")
            paint.letterSpacing = 1f
            val w2 = paint.measureText(".")
            Log.e("OoO", "w1 = $w1, w2 = $w2")
            w1 == w2
        }
    }


    private var mTextWidth: Int = 0
    private var mTextHeight: Int = 0
    private var mTextLeft: Int = 0
    private var mTextTop: Int = 0
    private val mTextStrokePath by lazy { Path() }

    private var mTextGradientColors: IntArray? = null
    private var mTextGradientOrientation: Int = 0
    private var mTextGradientRect: RectF = RectF()

    private var mSubtext: CharSequence? = null
    private var mSubtextLayout: Layout? = null
    private val mSubtextStrokePath by lazy { Path() }
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

    private val linePath = Path()


    //<editor-fold desc="icon 属性">
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

    var iconGravity: Int
        get() = mIconGravity
        set(value) {
            if (mIconGravity != value) {
                mIconGravity = value
                relayout()
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

    //</editor-fold>

    //<editor-fold desc="subtext 属性">

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

    //</editor-fold>

    //<editor-fold desc="stroke 属性">
    var strokeMode: Int = 0
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }
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
                requestLayout()
            }
        }
    //</editor-fold>

    init {

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.SuperTextView)

        mSubtextPaint.color = a.getColor(R.styleable.SuperTextView_tvSubtextColor, Color.BLACK)
        mSubtextPaint.textSize = a.getDimension(R.styleable.SuperTextView_tvSubtextSize, 0.75f * textSize)
        mSubtextPaint.typeface = a.getTypeface(R.styleable.SuperTextView_tvSubtextFont, R.styleable.SuperTextView_tvSubtextStyle)
        if (a.hasValue(R.styleable.SuperTextView_tvSubtextLetterSpacing)) {
            mSubtextPaint.letterSpacing = a.getDimension(R.styleable.SuperTextView_tvSubtextLetterSpacing, 0f) / mSubtextPaint.textSize
        }

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

        mTextGradientOrientation = a.getInt(R.styleable.SuperTextView_tvGradientOrientation, 0)
        val startColor = a.getColor(R.styleable.SuperTextView_tvGradiantStartColor, 0)
        val endColor = a.getColor(R.styleable.SuperTextView_tvGradiantEndColor, 0)
        if (startColor != 0 && endColor != 0) {
            mTextGradientColors = intArrayOf(startColor, endColor)
        }

        val alpha = (currentTextColor.toUInt() shr 24).toInt()
        val defaultStrokeMode = if (mTextGradientColors != null || alpha == 0xff) STROKE_MODE_NORMAL else STROKE_MODE_PATH
        strokeWidth = a.getDimensionPixelSize(R.styleable.SuperTextView_tvStrokeWidth, 0)
        strokeColor = a.getColor(R.styleable.SuperTextView_tvStrokeColor, 0)
        strokeMode = a.getInt(R.styleable.SuperTextView_tvStrokeMode, defaultStrokeMode)


        val letterSpacing = a.getDimensionPixelSize(R.styleable.SuperTextView_tvLetterSpacing, 0)

        a.recycle()

        paint.strokeJoin = Paint.Join.ROUND

        mSubtextPaint.textAlign = Paint.Align.LEFT
        mSubtextPaint.strokeJoin = Paint.Join.ROUND

        textAlignment = View.TEXT_ALIGNMENT_GRAVITY

        super.setLetterSpacing(letterSpacing / textSize)
        super.setHorizontallyScrolling(false)
    }


    override fun setHorizontallyScrolling(whether: Boolean) {}

    override fun onRtlPropertiesChanged(layoutDirection: Int) {}

    //<editor-fold desc="compoundPadding 属性">
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
    //</editor-fold>

    private fun getCompoundSpace(gravity: Int): Int {
        return getIconSpace(mIconGravity and 0xF, gravity) + getSubtextSpace(mSubtextGravity and 0xF, gravity) + getStrokeSpace()
    }

    private fun getStrokeSpace(): Int = if (strokeColor != 0 && strokeWidth > 0) strokeWidth else 0

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

        val strokeSpace = if (subtextStrokeColor != 0 && subtextStrokeWidth > 0) subtextStrokeWidth * 2 else 0

        return mSubtextPadding + strokeSpace + if (isHorizontal) layout.width else layout.height
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)

        mSubtextLayout = obtainLayout(mSubtextLayout, mSubtext, maxWidth, mSubtextPaint)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        updateText()
        updateIcon()
        updateSubtext()
        updateGradiant()
    }

    private fun updateText() {
        val lineCount = max(1, min(layout.lineCount, maxLines))
        mTextWidth = (0 until lineCount).maxOf { layout.getLineRight(it) - layout.getLineLeft(it) }.toInt()
        mTextHeight = (0 until lineCount).sumOf { layout.getLineBottom(it) - layout.getLineTop(it) }

        val left = compoundPaddingLeft
        val right = compoundPaddingRight
        val boxWidth = measuredWidth - left - right
        mTextLeft = if (boxWidth < mTextWidth) left else when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.LEFT -> left
            Gravity.RIGHT -> measuredWidth - right - mTextWidth
            else -> left + (boxWidth - mTextWidth) / 2
        }
        val top = compoundPaddingTop
        val bottom = compoundPaddingBottom
        val boxHeight = measuredHeight - top - bottom
        mTextTop = if (boxHeight < mTextHeight) top else when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.TOP -> top
            Gravity.BOTTOM -> measuredHeight - bottom - mTextHeight
            else -> top + (boxHeight - mTextHeight) / 2
        }

        if (strokeMode == STROKE_MODE_PATH) {
            mTextStrokePath.update(layout, lineCount)
        }
    }

    private fun updateIcon() {
        val icon = mIcon ?: return

        val iconWidth = if (mIconSize > 0) mIconSize else icon.intrinsicWidth
        val iconHeight = if (mIconSize > 0) mIconSize else icon.intrinsicHeight

        mIconLeft = when (mIconGravity) {
            GRAVITY_START -> super.getPaddingLeft()
            GRAVITY_END -> measuredWidth - super.getPaddingRight() - iconWidth
            GRAVITY_TEXT_START -> mTextLeft - getIconSpace(mIconGravity)
            GRAVITY_TEXT_END -> mTextLeft + mTextWidth + mIconPadding
            GRAVITY_TOP, GRAVITY_BOTTOM -> when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> super.getPaddingLeft()
                Gravity.RIGHT -> measuredWidth - super.getPaddingRight() - iconWidth
                else -> (measuredWidth + super.getPaddingLeft() - super.getPaddingRight() - iconWidth) / 2
            }

            else -> when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> mTextLeft
                Gravity.RIGHT -> mTextLeft + mTextWidth - iconWidth
                else -> mTextLeft + (mTextWidth - iconWidth) / 2
            }
        }
        mIconTop = when (mIconGravity) {
            GRAVITY_TOP -> super.getPaddingTop()
            GRAVITY_BOTTOM -> measuredHeight - super.getPaddingBottom() - iconHeight
            GRAVITY_TEXT_TOP -> mTextTop - getSubtextSpace(mSubtextGravity, mIconGravity) - getIconSpace(mIconGravity)
            GRAVITY_TEXT_BOTTOM -> mTextTop + mTextHeight + getSubtextSpace(mSubtextGravity, mIconGravity) + mIconPadding
            GRAVITY_START, GRAVITY_END -> when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> super.getPaddingTop()
                Gravity.BOTTOM -> measuredHeight - super.getPaddingBottom() - iconHeight
                else -> (measuredHeight + super.getPaddingTop() - super.getPaddingBottom() - iconHeight) / 2
            }

            else -> when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> mTextTop
                Gravity.BOTTOM -> mTextTop + mTextHeight - iconHeight
                else -> mTextTop + (mTextHeight - iconHeight) / 2
            }
        }
        icon.setBounds(mIconLeft, mIconTop, mIconLeft + iconWidth, mIconTop + iconHeight)
    }

    private fun updateSubtext() {
        val subtextLayout = mSubtextLayout ?: return

        val subtextWidth = subtextLayout.width
        val subtextHeight = subtextLayout.height

        mSubtextLeft = when (mSubtextGravity) {
            GRAVITY_START -> super.getPaddingLeft() + getIconSpace(mIconGravity, mSubtextGravity) + subtextStrokeWidth
            GRAVITY_END -> measuredWidth - super.getPaddingRight() - subtextWidth - subtextStrokeWidth - getIconSpace(mIconGravity, mSubtextGravity)
            GRAVITY_TEXT_START -> mTextLeft - getIconSpace(mIconGravity, mSubtextGravity) - getSubtextSpace(mSubtextGravity)
            GRAVITY_TEXT_END -> mTextLeft + mTextWidth + getIconSpace(mIconGravity, mSubtextGravity) + mSubtextPadding
            GRAVITY_TOP, GRAVITY_BOTTOM -> when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> super.getPaddingLeft() + subtextStrokeWidth
                Gravity.RIGHT -> measuredWidth - super.getPaddingRight() - subtextWidth - subtextStrokeWidth
                else -> (measuredWidth + super.getPaddingLeft() - super.getPaddingRight() - subtextWidth) / 2
            }

            else -> when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> mTextLeft
                Gravity.RIGHT -> mTextLeft + mTextWidth - subtextWidth
                else -> mTextLeft + (mTextWidth - subtextWidth) / 2
            }
        }
        mSubtextTop = when (mSubtextGravity) {
            GRAVITY_TOP -> super.getPaddingTop() + getIconSpace(mIconGravity, mSubtextGravity)
            GRAVITY_BOTTOM -> measuredHeight - super.getPaddingBottom() - subtextHeight - getIconSpace(mIconGravity, mSubtextGravity)
            GRAVITY_TEXT_TOP -> mTextTop - getSubtextSpace(mSubtextGravity)
            GRAVITY_TEXT_BOTTOM -> mTextTop + mTextHeight + mSubtextPadding
            GRAVITY_START, GRAVITY_END -> when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> layout.getLineBaseline(0) - subtextLayout.getLineBaseline(0) + super.getPaddingTop()
                Gravity.BOTTOM -> -layout.getLineDescent(0) + subtextLayout.getLineDescent(0) + measuredHeight - super.getPaddingBottom() - subtextHeight
                else -> (measuredHeight + super.getPaddingTop() - super.getPaddingBottom() - subtextHeight) / 2
            }

            else -> when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> layout.getLineBaseline(0) - subtextLayout.getLineBaseline(0) + mTextTop
                Gravity.BOTTOM -> -layout.getLineDescent(0) + subtextLayout.getLineDescent(0) + mTextTop + mTextHeight - subtextHeight
                else -> mTextTop + (mTextHeight - subtextHeight) / 2
            }
        }

        if (subtextStrokeWidth > 0 && subtextStrokeColor != 0) {
            mSubtextStrokePath.update(subtextLayout, subtextLayout.lineCount)
        }
    }

    private fun updateGradiant() {
        val colors = mTextGradientColors ?: return
        val rect = RectF(mTextLeft.toFloat(), mTextTop.toFloat(), mTextLeft + mTextWidth.toFloat(), mTextTop + mTextHeight.toFloat())
        if (!rect.isEmpty && mTextGradientRect != rect) {
            mTextGradientRect = rect
            val r = when (mTextGradientOrientation) {
                // TOP_BOTTOM
                0 -> RectF(rect.left, rect.top, rect.left, rect.bottom)
                // TR_BL
                1 -> RectF(rect.right, rect.top, rect.left, rect.bottom)
                // RIGHT_LEFT
                2 -> RectF(rect.right, rect.top, rect.left, rect.top)
                // BR_TL
                3 -> RectF(rect.right, rect.bottom, rect.left, rect.top)
                // BOTTOM_TOP
                4 -> RectF(rect.left, rect.bottom, rect.left, rect.top)
                // BL_TR
                5 -> RectF(rect.left, rect.bottom, rect.right, rect.top)
                // LEFT_RIGHT
                6 -> RectF(rect.left, rect.top, rect.right, rect.top)
                // TL_BR
                else -> RectF(rect.left, rect.top, rect.right, rect.bottom)
            }

            layout.paint.shader = LinearGradient(r.left, r.top, r.right, r.bottom, colors, null, Shader.TileMode.CLAMP)
        }
    }

    private fun Path.update(layout: Layout, lineCount: Int) {
        reset()
        linePath.reset()
        val paint = layout.paint
        val text = layout.text.toString()
        for (line in 0 until lineCount) {
            val start = layout.getLineStart(line)
            val end = layout.getLineEnd(line)
            val x = layout.getLineLeft(line)
            val y = layout.getLineBaseline(line).toFloat()
            paint.getTextPath(text, start, end, x, y, linePath)
            addPath(linePath)
        }
    }


    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {

        if (layout != null && strokeWidth > 0 && strokeColor != 0) {
            canvas.drawTextStroke(strokeWidth.toFloat(), strokeColor, layout, paint)
        }

        super.onDraw(canvas)

        mIcon?.draw(canvas)

        mSubtextLayout?.let {
            canvas.save()
            canvas.translate(mSubtextLeft.toFloat(), mSubtextTop.toFloat())
            if (subtextStrokeWidth > 0 && subtextStrokeColor != 0) {
                canvas.drawSubtextStroke(subtextStrokeWidth.toFloat(), subtextStrokeColor, mSubtextStrokePath, it.paint)
            }
            it.draw(canvas)
            canvas.restore()
        }

    }


    private fun relayout() {
        requestLayout()
        postInvalidate()
    }

    private fun Canvas.drawTextStroke(strokeWidth: Float, strokeColor: Int, layout: Layout, paint: Paint) {

        val width = paint.strokeWidth
        val shader = paint.shader
        val color = paint.color


        paint.color = strokeColor
        paint.strokeWidth = strokeWidth * 2
        paint.style = Paint.Style.STROKE
        paint.shader = null

        val saveCount = save()
        translate(compoundPaddingLeft.toFloat(), mTextTop.toFloat())

        if (strokeMode == STROKE_MODE_PATH) {
            val path = mTextStrokePath
            if (!path.isEmpty) {
                if (hasLetterSpacingIssue) {
                    translate(-paint.letterSpacing * paint.textSize * 0.5f, 0f)
                }
                clipOutPath(path)
                drawPath(path, paint)
            }
        } else {
            layout.draw(this)
        }
        restoreToCount(saveCount)

        paint.color = color
        paint.strokeWidth = width
        paint.style = Paint.Style.FILL
        paint.shader = shader
    }

    private fun Canvas.drawSubtextStroke(strokeWidth: Float, strokeColor: Int, path: Path, paint: Paint) {
        val color = paint.color
        val width = paint.strokeWidth
        val shader = paint.shader

        paint.color = strokeColor
        paint.strokeWidth = strokeWidth * 2
        paint.style = Paint.Style.STROKE
        paint.shader = null

        val saveCount = save()
        clipOutPath(path)
        drawPath(path, paint)
        restoreToCount(saveCount)

        paint.color = color
        paint.strokeWidth = width
        paint.style = Paint.Style.FILL
        paint.shader = shader
    }

    private fun obtainLayout(layout: Layout?, source: CharSequence?, maxWidth: Int, paint: TextPaint): Layout? {

        if (source.isNullOrEmpty()) return null

        if (layout != null && layout.text == source) return layout

        val metrics = BoringLayout.isBoring(source, paint)
        val alignment = when {
            gravity and Gravity.START == Gravity.START -> Layout.Alignment.ALIGN_NORMAL
            gravity and Gravity.LEFT == Gravity.LEFT -> Layout.Alignment.ALIGN_NORMAL
            gravity and Gravity.END == Gravity.END -> Layout.Alignment.ALIGN_OPPOSITE
            gravity and Gravity.RIGHT == Gravity.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }
        val width = when {
            metrics != null && maxWidth > 0 -> min(metrics.width, maxWidth)
            metrics != null -> metrics.width
            else -> min(paint.measureText(source.toString()).toInt(), maxWidth)
        }
        if (metrics != null && (maxWidth == 0 || metrics.width < maxWidth)) {
            return BoringLayout.make(source, paint, width, alignment, 1f, 0f, metrics, false)
        }
        return StaticLayout.Builder.obtain(source, 0, source.length, paint, width)
            .setAlignment(alignment)
            .setIncludePad(false)
            .setLineSpacing(0f, 1f)
            .build()
    }

    private fun Drawable.tint(tint: ColorStateList?): Drawable {
        tint ?: return this
        return DrawableCompat.wrap(this).mutate().also {
            DrawableCompat.setTintList(it, tint)
        }
    }

    private fun TypedArray.getTypeface(fontFamilyIndex: Int, textStyleIndex: Int): Typeface {
        val font = getFont(fontFamilyIndex)
        if (font != null) {
            if (hasValue(textStyleIndex)) {
                return Typeface.create(font, getInt(textStyleIndex, Typeface.NORMAL))
            }
            return font
        }
        return Typeface.create(Typeface.DEFAULT, getInt(textStyleIndex, Typeface.NORMAL))
    }
}

