package me.reezy.cosmo.tv.expandable


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

    fun interface OnExpandListener {
        fun onExpand(isExpand: Boolean)
    }

    private var mExpand: Boolean = false
    private var mMaxLines: Int = 0
    private var mOnExpandListener: OnExpandListener? = null

    private var mMoreIcon: Drawable? = null
    private var mLessIcon: Drawable? = null

    private var mIconSize: Int = 0

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView)
        val iconTint = a.getColorStateList(R.styleable.ExpandableTextView_iconTint)
        mIconSize = a.getDimensionPixelSize(R.styleable.ExpandableTextView_iconSize, textSize.toInt())
        mMoreIcon = a.getDrawable(R.styleable.ExpandableTextView_tvMoreIcon)?.wrap(iconTint, mIconSize)
        mLessIcon = a.getDrawable(R.styleable.ExpandableTextView_tvLessIcon)?.wrap(iconTint, mIconSize)
        a.recycle()

        updateExpand(mExpand)
        setOnClickListener {
            updateExpand(!mExpand)
        }

    }


    fun setOnExpandListener(listener: OnExpandListener) {
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

            val icon = if (mExpand) mLessIcon else mMoreIcon

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
            mOnExpandListener?.onExpand(expand)
        }
    }

    private fun Drawable.wrap(tint: ColorStateList?, size: Int): Drawable = DrawableCompat.wrap(this).mutate().also {
        DrawableCompat.setTintList(it, tint)
        val width = if (size != 0) size else it.intrinsicWidth
        val height = if (size != 0) size else it.intrinsicHeight
        it.setBounds(0, 0, width, height)
        it.setVisible(true, false)
    }
}