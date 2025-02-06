package com.demo.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class StrokeTextView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var mStrokeWidth = 2 * resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {

        val color = textColors
        val width = paint.strokeWidth
        val shader = paint.shader

        setTextColor((0xffff0000).toInt())
        paint.strokeWidth = mStrokeWidth.toFloat() * 2
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.STROKE
        paint.shader = null


        super.onDraw(canvas)

        setTextColor(color)
        paint.strokeWidth = width
        paint.style = Paint.Style.FILL
        paint.shader = shader

        // 绘制文本
        super.onDraw(canvas);
    }
}