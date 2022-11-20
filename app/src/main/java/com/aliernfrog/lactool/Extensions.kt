package com.aliernfrog.lactool

import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

fun View.onClick(onClick: Runnable) {
    this.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val opacityDown = ObjectAnimator.ofFloat(v, "alpha", 0.5f)
                opacityDown.duration = 50
                opacityDown.start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.action == MotionEvent.ACTION_UP) { onClick.run(); v?.performClick() }
                val opacityUp = ObjectAnimator.ofFloat(v, "alpha", 0.9f)
                opacityUp.duration = 50
                opacityUp.start()
            }
        }
        return@OnTouchListener true
    })
}