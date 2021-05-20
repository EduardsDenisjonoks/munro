package com.example.munros.utils

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("app:visible_or_gone")
fun View.visibleOrGone(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}
