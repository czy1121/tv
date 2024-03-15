package me.reezy.cosmo.tv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import me.reezy.cosmo.R

class CamelTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : AppCompatTextView(context, attrs, defStyle) {


    private var mPrefixText: CharSequence? = null
    private var mPrefixLayout: Layout? = null
    private val mPrefixPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var mSuffixText: CharSequence? = null
    private var mSuffixLayout: Layout? = null
    private val mSuffixPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    init {

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.CamelTextView)

        mPrefixPaint.textAlign = Paint.Align.LEFT
        mPrefixPaint.color = a.getColor(R.styleable.CamelTextView_tvPrefixTextColor, currentTextColor)
        mPrefixPaint.textSize = a.getDimension(R.styleable.CamelTextView_tvPrefixTextSize, textSize * 2 / 3f)
        mPrefixPaint.typeface = a.getTypeface(R.styleable.CamelTextView_tvPrefixTextFont, R.styleable.CamelTextView_tvPrefixTextStyle)

        mSuffixPaint.textAlign = Paint.Align.LEFT
        mSuffixPaint.color = a.getColor(R.styleable.CamelTextView_tvSuffixTextColor, currentTextColor)
        mSuffixPaint.textSize = a.getDimension(R.styleable.CamelTextView_tvSuffixTextSize, textSize * 2 / 3f)
        mSuffixPaint.typeface = a.getTypeface(R.styleable.CamelTextView_tvSuffixTextFont, R.styleable.CamelTextView_tvSuffixTextStyle)

        mPrefixText = a.getString(R.styleable.CamelTextView_tvPrefixText)
        mSuffixText = a.getString(R.styleable.CamelTextView_tvSuffixText)

        a.recycle()

        super.setSingleLine()
        super.setHorizontallyScrolling(false)

    }

    override fun getCompoundPaddingLeft(): Int {
        return super.getCompoundPaddingLeft() + (mPrefixLayout?.width ?: 0)
    }

    override fun getCompoundPaddingRight(): Int {
        return super.getCompoundPaddingRight() + (mSuffixLayout?.width ?: 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mPrefixLayout = obtainLayout(mPrefixLayout, mPrefixText, mPrefixPaint)
        mSuffixLayout = obtainLayout(mSuffixLayout, mSuffixText, mSuffixPaint)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val y = layout.getLineBaseline(0).toFloat() + (height - layout.height + compoundPaddingTop - compoundPaddingBottom) / 2f
        mPrefixLayout?.let {
            canvas.save()
            canvas.translate(layout.getLineLeft(0), y - it.getLineBaseline(0))
            it.draw(canvas)
            canvas.restore()
        }
        mSuffixLayout?.let {
            val offset = mPrefixLayout?.width ?: 0
            canvas.save()
            canvas.translate(layout.getLineRight(0) + offset, y - it.getLineBaseline(0))
            it.draw(canvas)
            canvas.restore()
        }
    }
}