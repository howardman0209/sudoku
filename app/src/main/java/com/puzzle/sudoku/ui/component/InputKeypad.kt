package com.puzzle.sudoku.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View
import com.puzzle.sudoku.extension.measureTextHeight

class InputKeypad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val TAG = "InputKeypad"
    }

    private val cellHeight by lazy { height.toFloat() / 3 }
    private val cellWidth by lazy { width.toFloat() / 3 }
    private val textPaint by lazy {
        Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = cellHeight * 3 / 4
        }
    }
    private val keys = List(9) { idx -> idx + 1 }
    private var pressedKey: Int? = null
    private var listener: InputListener? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        keys.forEach {
            val xStart = cellWidth * ((it - 1) % 3)
            val yStart = cellHeight * ((it - 1) / 3 + 1)
            val textWidth = textPaint.measureText(it.toString())
            val textHeight = textPaint.measureTextHeight(it.toString())
//            Log.d(TAG, "yStart: $yStart, textSize: ${textPaint.textSize}, textHeight: $textHeight, textBounds: ${textBounds.bottom - textBounds.top}")
            canvas.drawText("$it", xStart + (cellWidth - textWidth) / 2, yStart - (cellHeight - textHeight) / 2, textPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                playSoundEffect(SoundEffectConstants.CLICK)
                val x = event.x
                val y = event.y
//                Log.d(TAG, "ACTION_DOWN - ($x, $y)")
                pressedKey = findKey(x, y)
//                Log.d(TAG, "pressedCell - $pressedCell")
                invalidate() // Request to redraw
                return true
            }

            MotionEvent.ACTION_UP -> {
                val x = event.x
                val y = event.y
//                Log.d(TAG, "ACTION_UP - ($x, $y)")
                val releasedCell = findKey(x, y)
//                Log.d(TAG, "pressedKey - $pressedKey")
                if (releasedCell != null && releasedCell == pressedKey) {
                    listener?.onKeyInput(releasedCell)
                }
                pressedKey = null
                invalidate() // Request to redraw
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun findKey(x: Float, y: Float): Int? {
//        Log.d(TAG, "findKey - x: $x, y: $y")
        val key = keys.find { key ->
            val xRange = (cellWidth * ((key - 1) % 3)).let { it.rangeUntil(it + cellWidth) }
            val yRange = (cellHeight * ((key - 1) / 3)).let { it.rangeUntil(it + cellHeight) }
//            Log.d(TAG, "findKey - xRange: $xRange, yRange: $yRange")
            (x in xRange && y in yRange)
        }
//        Log.d(TAG, "findKey - key: $key")
        return key
    }

    fun setListener(inputListener: InputListener) {
        this.listener = inputListener
    }

    interface InputListener {
        fun onKeyInput(key: Int)
    }
}