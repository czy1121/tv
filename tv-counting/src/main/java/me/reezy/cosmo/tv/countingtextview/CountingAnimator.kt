package me.reezy.cosmo.tv.countingtextview

import android.animation.Animator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.util.Log
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnLayout
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.logging.Logger


class CountingAnimator(pattern: String? = null, duration: Long = 1000) : ValueAnimator() {

    private val format = DecimalFormat(pattern)

    private var onUpdate: (String) -> Unit = {}

    init {
        setDuration(duration)
        setObjectValues(0.0, 0.0)
        setEvaluator(DOUBLE_EVALUATOR)
        setCurrentFraction(0f)
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

    @Deprecated("")
    fun attachTo(view: TextView, letterWidth: Float, emptyWidth: Float = 0f): CountingAnimator {
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
        return this
    }

    fun attachTo(view: TextView, isCalcWidth: Boolean = false): CountingAnimator {
        if (isCalcWidth) {
            view.doOnLayout {
                view.maxLines = 1
                view.setLineSpacing(view.measuredHeight.toFloat(), 1f)
            }

            var letterWidth = 0f
            var compoundPadding = 0f
            var nowLen = 0
            var nowWidth = 0f
            addListener(object : AnimatorListener {
                override fun onAnimationStart(animator: Animator) {
                    val widths = FloatArray(10)
                    view.paint.getTextWidths("0123456789", widths)
                    letterWidth = widths.max()
                    compoundPadding = (view.compoundPaddingLeft + view.compoundPaddingRight).toFloat()

                    nowLen = view.text.length
                    nowWidth = view.paint.measureText(view.text.toString())
                }
                override fun onAnimationEnd(animator: Animator) {
                    view.postDelayed(::wrapContent, 100)
                }

                private fun wrapContent() {
                    if (!isRunning) {
                        val lp = view.layoutParams as ViewGroup.LayoutParams
                        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        view.layoutParams = lp
                    }

                }
                override fun onAnimationRepeat(animator: Animator) {}
                override fun onAnimationCancel(animator: Animator) {}
            })
            onUpdate = {
                view.text = it
                val lp = view.layoutParams as ViewGroup.LayoutParams

                val newLen = view.text.toString().length
                if (nowLen > newLen) {
                    val newWidth = letterWidth * newLen
                    lp.width = (compoundPadding + newWidth).toInt()
                    view.layoutParams = lp
                    nowWidth = newWidth
                    nowLen = newLen
                } else {
                    val newWidth = view.paint.measureText(it)
                    if (nowWidth < newWidth) {
                        lp.width = (compoundPadding + newWidth).toInt()
                        view.layoutParams = lp
                        nowWidth = newWidth
                        nowLen = newLen
                    }
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