package me.reezy.cosmo.tv.countingtextview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import me.reezy.cosmo.R
import java.math.RoundingMode


class CountingTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatTextView(context, attrs, defStyleAttr) {

    private val animator = CountingAnimator().attachTo(this)

    init {
        val a = getContext().obtainStyledAttributes(attrs, R.styleable.CountingTextView)
        val format = a.getString(R.styleable.CountingTextView_tvFormat) ?: "0.##"
        a.recycle()

        animator.setPattern(format)
    }


    fun setPattern(pattern: String): CountingTextView {
        animator.setPattern(pattern)
        return this
    }

    fun setRoundingMode(mode: RoundingMode): CountingTextView {
        animator.setRoundingMode(mode)
        return this
    }

    fun countTo(to: Double) {
        animator.countTo(to)
    }

    fun count(from: Double, to: Double) {
        animator.count(from, to)
    }

    fun start() {
        animator.start()
    }

}