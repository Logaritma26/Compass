package com.log.compass.util

import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter


@BindingAdapter("degree")
fun degree(view: TextView, degree: Int?) {
    view.text = "$degree °"
}

@BindingAdapter("distance")
fun distance(view: TextView, distance: Int) {
    if (distance == 0) {
        view.visibility = View.GONE
    } else {
        if (distance > 10000) {
            val kmDistance = distance/1000
            view.text = "$kmDistance km left"
        } else {
            view.text = "$distance m left"
        }
        view.visibility = View.VISIBLE
    }
}

@BindingAdapter("rotate")
fun rotateAnimation(view: ImageView, degree: Array<Int>?) {
    degree?.let {
        val anim = RotateAnimation(
            -degree[1].toFloat(),
            -degree[0].toFloat(),
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        anim.duration = 200
        anim.repeatCount = 0
        anim.fillAfter = true
        view.startAnimation(anim)
    }

}
