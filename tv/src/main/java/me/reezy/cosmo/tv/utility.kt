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

internal fun TypedArray.getTypeface(fontFamilyIndex: Int, textStyleIndex: Int): Typeface {
    val font = getFont(fontFamilyIndex)
    if (font != null) {
        if (hasValue(textStyleIndex)) {
            return Typeface.create(font, getInt(textStyleIndex, Typeface.NORMAL))
        }
        return font
    }
    return Typeface.create(Typeface.DEFAULT, getInt(textStyleIndex, Typeface.NORMAL))
}