package me.reezy.cosmo.tv.countingtextview

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import java.math.RoundingMode
import java.text.DecimalFormat


class CountingAnimator(pattern: String? = null, private var onUpdate: (Double) -> Unit = {}) : ValueAnimator() {

    private var mFormat = DecimalFormat(pattern)


    init {
        interpolator = LinearInterpolator()
        duration = 1000
        addUpdateListener { animation ->
            onUpdate(animation.animatedValue as Double)
        }

    }

    fun setPattern(pattern: String): CountingAnimator {
        mFormat.applyPattern(pattern)
        return this
    }

    fun setRoundingMode(mode: RoundingMode): CountingAnimator {
        mFormat.roundingMode = mode
        return this
    }

    fun attachTo(view: TextView): CountingAnimator {
        onUpdate = { view.text = mFormat.format(it) }
        return this
    }

    fun doOnUpdate(block: (Double) -> Unit): CountingAnimator {
        onUpdate = block
        return this
    }

    fun countBy(delta: Double) {
        val value = (animatedValue as? Double) ?: 0.0
        count(value, value + delta)
    }

    fun countTo(to: Double) {
        val value = (animatedValue as? Double) ?: 0.0
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