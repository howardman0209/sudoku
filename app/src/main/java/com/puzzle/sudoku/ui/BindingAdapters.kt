package com.puzzle.sudoku.ui

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("showIf")
fun View.bindShowIf(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}