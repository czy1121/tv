package me.reezy.cosmo.tv

import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.BoringLayout
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import kotlin.math.min


internal fun Drawable.tint(tint: ColorStateList?): Drawable {
    tint ?: return this
    return DrawableCompat.wrap(this).mutate().also {
        DrawableCompat.setTintList(it, tint)
    }
}

internal fun chooseSize(spec: Int, size: Int): Int {
    val specMode = View.MeasureSpec.getMode(spec)
    val specSize = View.MeasureSpec.getSize(spec)
    return when (specMode) {
        View.MeasureSpec.EXACTLY -> specSize
        View.MeasureSpec.AT_MOST -> min(size, specSize)
        else -> size
    }
}


internal fun obtainLayout(layout: Layout?, source: CharSequence?, paint: TextPaint): Layout? {

    if (source.isNullOrEmpty()) return null

    if (layout != null && layout.text == source) return layout

    return makeLayout(source, paint)
}

internal fun makeLayout(source: CharSequence, paint: TextPaint, maxWidth: Int = 0): Layout? {
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


internal fun TypedArray.getTypeface(fontFamilyIndex: Int, textStyleIndex: Int): Typeface {
    val style = getInt(textStyleIndex, Typeface.NORMAL)
    try {
        return Typeface.create(getFont(fontFamilyIndex), style)
    } catch (e: UnsupportedOperationException) {
    } catch (e: Resources.NotFoundException) {
    }
    return Typeface.create(Typeface.DEFAULT, style)
}