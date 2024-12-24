package com.puzzle.sudoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SudokuBoard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val cellSize by lazy { height.toFloat() / 9 }
    private var puzzle: Array<IntArray>? = null

    private val thinBorderPaint = Paint().apply {
        strokeWidth = 1f
        color = Color.BLACK
    }

    private val thickBorderPaint = Paint().apply {
        strokeWidth = 5f
        color = Color.BLACK
    }

    private val textPaint by lazy {
        Paint().apply {
            color = Color.BLACK
            textSize = cellSize / 2
        }
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SudokuBoard, 0, 0).apply {
            try {

            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)

        val horizontalLines = List<FloatArray>(9) { idx -> floatArrayOf(0f, cellSize * (idx + 1), width.toFloat(), cellSize * (idx + 1)) }

        horizontalLines.forEachIndexed { idx, line ->
            val paint = if ((idx + 1) % 3 == 0) thickBorderPaint else thinBorderPaint
            canvas.drawLines(line, paint)
        }

        val verticalLines = List<FloatArray>(9) { idx -> floatArrayOf(cellSize * (idx + 1), 0f, cellSize * (idx + 1), height.toFloat()) }

        verticalLines.forEachIndexed { idx, line ->
            val paint = if ((idx + 1) % 3 == 0) thickBorderPaint else thinBorderPaint
            canvas.drawLines(line, paint)
        }

        puzzle?.let {
            it.forEachIndexed { row, intArray ->
                intArray.forEachIndexed { col, int ->
                    if (int != 0) {
                        val cellValue = "$int"
                        val labelWidth = textPaint.measureText(cellValue)
                        val labelHeight = textPaint.textSize
                        canvas.drawText(cellValue, horizontalLines[col][3] - (cellSize + labelWidth) / 2, verticalLines[row][0] - labelHeight / 2, textPaint)
                    }
                }
            }
        }
    }

    fun updatePuzzle(puzzle: Array<IntArray>?) {
        this.puzzle = puzzle
        invalidate()
    }
}