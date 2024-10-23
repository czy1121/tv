package me.reezy.cosmo.tv.countingtextview

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import java.math.RoundingMode
import java.text.DecimalFormat


class CountingAnimator(pattern: String? = null, duration: Long = 1000) : ValueAnimator() {

    private val format = DecimalFormat(pattern)

    private var onUpdate: (String) -> Unit = {}

    init {
        setDuration(duration)
        interpolator = LinearInterpolator()
        format.roundingMode = RoundingMode.FLOOR
        addUpdateListener { animation ->
            onUpdate(format.format(animation.animatedValue as Double))
        }

    }

    fun setPattern(pattern: String): CountingAnimator {
        format.applyPattern(pattern)
        return this
    }

    fun setRoundingMode(mode: RoundingMode): CountingAnimator {
        format.roundingMode = mode
        return this
    }

    fun attachTo(view: TextView, letterWidth: Float = 0f, emptyWidth: Float = 0f): CountingAnimator {
        if (letterWidth > 0f) {
            onUpdate = {
                val oldLen = view.text.toString().length
                view.text = it
                val newLen = view.text.toString().length
                if (oldLen != newLen) {
                    val lp = view.layoutParams as ViewGroup.LayoutParams
                    lp.width = (view.resources.displayMetrics.density * (emptyWidth + newLen * letterWidth)).toInt()
                    view.layoutParams = lp
                }
            }
        } else {
            onUpdate = { view.text = it }
        }
        return this
    }


    fun countBy(delta: Double) {
        val value = (animatedValue as? Double) ?: 0.0
        count(value, value + delta)
    }

    fun countTo(to: Double) {
        val value = (animatedValue as? Double) ?: to
        count(value, to)
    }

    fun count(from: Double, to: Double) {
        if (java.lang.Double.isNaN(from) || java.lang.Double.isNaN(to)) {
            return
        }
        setObjectValues(from, to)
        setEvaluator(DOUBLE_EVALUATOR)
        start()
    }

    companion object {

        internal var DOUBLE_EVALUATOR: TypeEvaluator<Double> = TypeEvaluator { fraction, startValue, endValue -> startValue + (endValue - startValue) * fraction.toDouble() }
    }
}