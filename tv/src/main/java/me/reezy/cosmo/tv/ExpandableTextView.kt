package me.reezy.cosmo.tv


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.DrawableCompat
import me.reezy.cosmo.R

class ExpandableTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    AppCompatTextView(context, attrs, defStyle) {

    private var mExpand: Boolean = false
    private var mMaxLines: Int = 0
    private var mOnExpandListener: ((Boolean) -> Unit)? = null

    private var mIconMore: Drawable? = null
    private var mIconLess: Drawable? = null
    private var mIconSize: Int = 0
    private var mIconTint: ColorStateList? = null

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView)
        mIconSize = a.getDimensionPixelSize(R.styleable.ExpandableTextView_tvIconSize, textSize.toInt())
        mIconTint = a.getColorStateList(R.styleable.ExpandableTextView_tvIconTint)
        mIconMore = a.getDrawable(R.styleable.ExpandableTextView_tvIconMore)?.wrap()
        mIconLess = a.getDrawable(R.styleable.ExpandableTextView_tvIconLess)?.wrap()
        a.recycle()


        updateExpand(mExpand)
        setOnClickListener {
            updateExpand(!mExpand)
        }

    }


    fun setOnExpandListener(listener: ((Boolean) -> Unit)) {
        mOnExpandListener = listener
    }

    override fun setMaxLines(maxlines: Int) {
        super.setMaxLines(maxLines)
        mMaxLines = maxlines
    }

    override fun getCompoundPaddingRight(): Int = super.getCompoundPaddingRight() + mIconSize

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val notNeedIcon = !mExpand && mMaxLines > 0 && layout.getEllipsisCount(mMaxLines - 1) == 0

        if (!notNeedIcon) {

            val icon = if (mExpand) mIconLess else mIconMore

            icon?.let {
                val x = width - compoundPaddingRight
                val y = height - compoundPaddingBottom - mIconSize

                val bounds = it.bounds
                bounds.set(x, y, x + mIconSize, y + mIconSize)
                it.bounds = bounds
                it.draw(canvas)
            }
        }
    }

    private fun updateExpand(expand: Boolean) {
        if (expand) {
            super.setMaxLines(Int.MAX_VALUE)
        } else {
            super.setMaxLines(mMaxLines)
        }
        if (mExpand != expand) {
            mExpand = expand
            mOnExpandListener?.invoke(expand)
        }
    }

    private fun Drawable.wrap(): Drawable = DrawableCompat.wrap(this).mutate().also {
        DrawableCompat.setTintList(it, mIconTint)
        val width = if (mIconSize != 0) mIconSize else it.intrinsicWidth
        val height = if (mIconSize != 0) mIconSize else it.intrinsicHeight
        it.setBounds(0, 0, width, height)
        it.setVisible(true, false)
    }
}