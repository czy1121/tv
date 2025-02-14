package me.reezy.cosmo.tv.readmore


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.BoringLayout
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.doOnPreDraw
import me.reezy.cosmo.R

class ReadMoreTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    AppCompatTextView(context, attrs, defStyle) {

    private var mText: CharSequence? = null
    private var mBufferType: BufferType = BufferType.NORMAL
    private var mMaxLines: Int = 0
    private var mLineCount: Int = 0
    private var mLastLineWidth: Float = 0f

    private var mMoreText: CharSequence? = null
    private var mMoreIndicator: CharSequence? = null
    private var mMoreIndicatorWidth: Int = 0

    private var mLessText: CharSequence? = null
    private var mLessIndicator: CharSequence? = null
    private var mLessIndicatorWidth: Int = 0


    private var mOnExpandListener: ((Boolean) -> Unit)? = null
    private var mIsInitialized: Boolean = false


    var isExpand: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                update()
                mOnExpandListener?.invoke(value)
            }
        }

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.ReadMoreTextView)

        val moreText = a.getString(R.styleable.ReadMoreTextView_tvMoreText) ?: resources.getString(R.string.rmtv_more_text)
        val moreTextSize = a.getDimensionPixelSize(R.styleable.ReadMoreTextView_tvMoreTextSize, textSize.toInt())
        val moreTextColor = a.getColor(R.styleable.ReadMoreTextView_tvMoreTextColor, linkTextColors.defaultColor)

        val moreIconSize = a.getDimensionPixelSize(R.styleable.ReadMoreTextView_tvMoreIconSize, moreTextSize)
        val moreIconTint = a.getColorStateList(R.styleable.ReadMoreTextView_tvMoreIconTint)
        val moreIcon = a.getDrawable(R.styleable.ReadMoreTextView_tvMoreIcon)?.wrap(moreIconTint, moreIconSize)

        val lessText = a.getString(R.styleable.ReadMoreTextView_tvLessText) ?: resources.getString(R.string.rmtv_less_text)
        val lessTextSize = a.getDimensionPixelSize(R.styleable.ReadMoreTextView_tvLessTextSize, moreTextSize)
        val lessTextColor = a.getColor(R.styleable.ReadMoreTextView_tvLessTextColor, moreTextColor)

        val lessIconSize = a.getDimensionPixelSize(R.styleable.ReadMoreTextView_tvLessIconSize, lessTextSize)
        val lessIconTint = a.getColorStateList(R.styleable.ReadMoreTextView_tvLessIconTint) ?: moreIconTint
        val lessIcon = a.getDrawable(R.styleable.ReadMoreTextView_tvLessIcon)?.wrap(lessIconTint, lessIconSize)

        a.recycle()

        setMoreIndicator(indicator(moreText, moreTextColor, moreTextSize, moreIcon))
        setLessIndicator(indicator(lessText, lessTextColor, lessTextSize, lessIcon))

        mMaxLines = if (mMaxLines < 1) Int.MAX_VALUE else mMaxLines
        mIsInitialized = true
        super.setMaxLines(Int.MAX_VALUE)
        movementMethod = LinkMovementMethod.getInstance()

        prepare()

        if (isInEditMode) {
            setup()
        }
    }

    fun setMoreIndicator(value: CharSequence) {
        if (mMoreIndicator != value) {
            mMoreIndicator = value
            mMoreIndicatorWidth = BoringLayout.isBoring(value, paint)?.width ?: 0
            mMoreText = null
            update()
        }
    }

    fun setLessIndicator(value: CharSequence) {
        if (mLessIndicator != value) {
            mLessIndicator = value
            mLessIndicatorWidth = BoringLayout.isBoring(value, paint)?.width ?: 0
            mLessText = null
            update()
        }
    }

    fun setOnExpandListener(listener: ((Boolean) -> Unit)) {
        mOnExpandListener = listener
    }


    override fun setMaxLines(maxlines: Int) {
        mMaxLines = maxlines
        mMoreText = null
        update()
    }

    override fun setText(text: CharSequence, type: BufferType) {
        mText = text
        mBufferType = type
        mMoreText = null
        mLessText = null
        super.setText(text, type)
        prepare()
    }

    private fun prepare() {
        if (mText == null || !mIsInitialized) {
            return
        }
        doOnPreDraw {
            setup()
        }
    }

    private fun setup() {
        val layout = layout ?: return
        if (layout.text.length == mText?.length) {
            mLineCount = layout.lineCount
            mLastLineWidth = layout.getLineWidth(mLineCount - 1)
            update()
        }
    }

    private fun update() {
        if (mLineCount > mMaxLines) {
            if (isExpand) {
                if (!mLessIndicator.isNullOrBlank()) {
                    ensureLessText(mLessIndicator!!)
                    super.setText(mLessText, mBufferType)
                }
            } else {
                if (!mMoreIndicator.isNullOrBlank()) {
                    ensureMoreText(mMoreIndicator!!)
                    super.setText(mMoreText, mBufferType)
                }
            }
        }
    }


    private fun ensureLessText(indicator: CharSequence) {
        if (mLessText != null) return

        val text = mText ?: return

        val width = measuredWidth - compoundPaddingLeft - compoundPaddingRight
        mLessText = buildSpannedString {
            append(text)
            if (mLastLineWidth + mLessIndicatorWidth > width) {
                append("\n")
            }
            inSpans(clickable()) {
                append(indicator)
            }
        }
    }

    private fun ensureMoreText(indicator: CharSequence) {
        if (mMoreText != null) return
        val text = mText ?: return

        val start = layout.getLineStart(mMaxLines - 1)
        val end = layout.getLineEnd(mMaxLines - 1)

        val width = measuredWidth - compoundPaddingLeft - compoundPaddingRight
        val maxWidth = if (mMoreIndicatorWidth > 0) {
            width - paint.measureText("...") - mMoreIndicatorWidth
        } else {
            width / 2f
        }

        val len = paint.breakText(text, start, end, true, maxWidth, null)

        mMoreText = buildSpannedString {
            append(text.subSequence(0, start + len).trimEnd())
            append("...")
            inSpans(clickable()) {
                append(indicator)
            }
        }
    }

    private fun clickable() = object : ClickableSpan() {
        override fun onClick(widget: View) {
            isExpand = !isExpand
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
        }
    }

    private fun indicator(text: String?, textColor: Int, textSize: Int, icon: Drawable?) = buildSpannedString {
        if (text != null) {
            inSpans(ForegroundColorSpan(textColor), AbsoluteSizeSpan(textSize)) {
                append(text)
            }
        }
        if (icon != null) {
            inSpans(ImageSpan(icon, 2)) {
                append("*")
            }
        }
    }

    private fun Drawable.wrap(tint: ColorStateList?, height: Int): Drawable = DrawableCompat.wrap(this).mutate().also {
        if (tint != null) {
            DrawableCompat.setTintList(it, tint)
        }
        if (height > 0) {
            val width = (it.intrinsicWidth * height.toFloat() / it.intrinsicHeight).toInt()
            it.setBounds(0, 0, width, height)
        } else {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
        }
        it.setVisible(true, false)
    }
}