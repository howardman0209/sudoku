package com.puzzle.sudoku.extension

import android.graphics.Paint
import android.graphics.Rect

fun Paint.measureTextHeight(text: String): Float {
    val bounds = Rect()
    getTextBounds(text, 0, text.length, bounds)
    return bounds.let { it.bottom - it.top }.toFloat()
}