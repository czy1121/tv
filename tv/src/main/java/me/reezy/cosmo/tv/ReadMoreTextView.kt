package me.reezy.cosmo.tv


import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatTextView
import me.reezy.cosmo.R
import kotlin.math.max

class ReadMoreTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    AppCompatTextView(context, attrs, defStyle) {

    private var mMaxLines: Int = 0
    private var mBufferType: BufferType = BufferType.NORMAL

    private var mMoreText: String? = null
    private var mLessText: String? = null
    private var mMoreTextColor: Int = 0
    private var mLessTextColor: Int = 0

    private var mText: CharSequence? = null
    private var mContent: CharSequence? = null
    private var mSummary: CharSequence? = null
    private var mExpand: Boolean = false

    private var mOnExpandListener: ((Boolean) -> Unit)? = null
    private var mIsInitialized: Boolean = false

    private val mListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            setup()
        }
    }


    init {



        val a = context.obtainStyledAttributes(attrs, R.styleable.ReadMoreTextView)
        mMoreText = a.getString(R.styleable.ReadMoreTextView_tvMoreText)
        mLessText = a.getString(R.styleable.ReadMoreTextView_tvLessText)
        mMoreTextColor = a.getColor(R.styleable.ReadMoreTextView_tvMoreTextColor, linkTextColors.defaultColor)
        mLessTextColor = a.getColor(R.styleable.ReadMoreTextView_tvLessTextColor, mMoreTextColor)
        a.recycle()

        mMaxLines = if (mMaxLines < 1) Int.MAX_VALUE else mMaxLines
        mIsInitialized = true
        doOnGlobalLayout()
    }


    fun setMoreText(more: String) {
        if (mMoreText != more) {
            mMoreText = more
            mContent = null
            setup()
        }
    }

    fun setMoreTextColor(value: Int) {
        if (mMoreTextColor != value) {
            mMoreTextColor = value
            mContent = null
            setup()
        }
    }

    fun setLessText(less: String) {
        if (mLessText != less) {
            mLessText = less
            mSummary = null
            setup()
        }
    }

    fun setLessTextColor(value: Int) {
        if (mLessTextColor != value) {
            mLessTextColor = value
            mSummary = null
            setup()
        }
    }

    fun setOnExpandListener(listener: ((Boolean) -> Unit)) {
        mOnExpandListener = listener
    }

    override fun setMaxLines(maxlines: Int) {
        Log.e("OoO", "setMaxLines($maxlines, $mMaxLines)")
        mMaxLines = maxlines
        doOnGlobalLayout()
    }


    override fun getText(): CharSequence? {
        return mText
    }

    override fun setText(text: CharSequence, type: BufferType) {
        mText = text
        mBufferType = type
        super.setText(text, type)
        doOnGlobalLayout()
    }

    private fun doOnGlobalLayout() {
        if (mText == null || !mIsInitialized) {
            return
        }
        viewTreeObserver.addOnGlobalLayoutListener(mListener)
    }

    private fun setup() {
        if (lineCount > mMaxLines) {
            updateExpand(mExpand)
            setOnClickListener {
                updateExpand(!mExpand)
            }
        } else {
            updateText()
            setOnClickListener(null)
        }
    }

    private fun updateText() {
        super.setMaxLines(Int.MAX_VALUE)
        super.setText(mText, mBufferType)
    }
    private fun updateExpand(expand: Boolean) {
        if (expand) {
            super.setMaxLines(Int.MAX_VALUE)
            if (mContent == null) {
                mContent = createContent()
            }
            super.setText(mContent, mBufferType)
        } else {
            super.setMaxLines(mMaxLines)
            if (mSummary == null) {
                mSummary = createSummary()
            }
            super.setText(mSummary, mBufferType)
        }
        if (mExpand != expand) {
            mExpand = expand
            mOnExpandListener?.invoke(expand)
        }
    }


    private fun createContent(): CharSequence? {
        val text = mText ?: return null
        val label = mLessText ?: return mText

        if (label.isEmpty()) return mText

        val builder = SpannableStringBuilder(label)
        builder.setSpan(ForegroundColorSpan(mMoreTextColor), 0, label.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return SpannableStringBuilder(text).append(builder)
    }

    private fun createSummary(): CharSequence? {
        val text = mText ?: return null
        val label = mMoreText ?: return mText

        if (label.isEmpty()) return mText

        val layout = layout
        val start = layout.getLineStart(mMaxLines - 1)
        var end = layout.getLineEnd(mMaxLines - 1) - start

        val content = text.subSequence(start, text.length)

        val ellipsized = "...$label"

        val moreWidth = paint.measureText(ellipsized, 0, ellipsized.length)
        val maxWidth = layout.width - moreWidth
        var len = paint.breakText(content, 0, content.length, true, maxWidth, null)
        if (content[end - 1] == '\n') {
            end -= 1
        }
        len = len.coerceAtMost(end)

        val builder = SpannableStringBuilder(label)
        builder.setSpan(ForegroundColorSpan(mLessTextColor), 0, label.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return SpannableStringBuilder(text.subSequence(0, start + len)).append("...").append(builder)
    }
}