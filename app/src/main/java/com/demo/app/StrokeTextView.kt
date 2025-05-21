package com.demo.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.ComposePathEffect
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView


class StrokeTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var mStrokeWidth = 2 * resources.displayMetrics.density

    private var mStrokeColor: Int = 0

    init {

        val a = getContext().obtainStyledAttributes(attrs, com.demo.app.R.styleable.StrokeTextView)
        mStrokeWidth = a.getDimensionPixelSize(com.demo.app.R.styleable.StrokeTextView_strokeWidth, 0).toFloat()
        mStrokeColor = a.getColor(com.demo.app.R.styleable.StrokeTextView_strokeColor, 0)
        a.recycle()

//        super.setHorizontallyScrolling(false)

//        Log.e("OoO", "fontFeatureSettings => ${paint.fontVariationSettings}")
    }

//    override fun setHorizontallyScrolling(whether: Boolean) {
//    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {

    }

    override fun onDraw(canvas: Canvas) {


        val color = paint.color
        val width = paint.strokeWidth
        val shader = paint.shader

        paint.color = mStrokeColor
        paint.strokeWidth = mStrokeWidth * 2
        paint.style = Paint.Style.STROKE
        paint.shader = null


        layout?.let {
            val saveCount = canvas.save()
            canvas.translate(compoundPaddingLeft.toFloat(), compoundPaddingTop.toFloat())
            it.draw(canvas)
            canvas.restoreToCount(saveCount)
        }

        paint.color = color
        paint.strokeWidth = width
        paint.style = Paint.Style.FILL
        paint.shader = shader


        super.onDraw(canvas)
    }
}