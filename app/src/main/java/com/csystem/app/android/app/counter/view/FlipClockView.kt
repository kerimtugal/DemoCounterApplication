package com.csystem.app.android.app.counter.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import org.csystem.app.android.app.counter.R

class FlipClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val topCard: MaterialCardView
    private val bottomCard: MaterialCardView
    private val numberText: MaterialTextView
    private var currentValue = 0

    init {
        // Layout inflate
        val view = LayoutInflater.from(context).inflate(R.layout.view_flip_clock, this, true)

        // View references
        topCard = view.findViewById(R.id.topCard)
        bottomCard = view.findViewById(R.id.bottomCard)
        numberText = view.findViewById(R.id.numberText)

        // Initial setup
        numberText.text = "00"
    }

    fun setValue(value: Int) {
        if (value == currentValue) return

        // Animate flip
        val flipUpAnimation = AnimationUtils.loadAnimation(context, R.anim.flip_up)
        val flipDownAnimation = AnimationUtils.loadAnimation(context, R.anim.flip_down)

        topCard.startAnimation(flipUpAnimation)
        bottomCard.startAnimation(flipDownAnimation)

        // Update value
        numberText.text = String.format("%02d", value)
        currentValue = value
    }
}