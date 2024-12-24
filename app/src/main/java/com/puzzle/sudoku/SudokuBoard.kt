package com.puzzle.sudoku

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class SudokuBoard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    companion object {
        const val TAG = "SudokuBoard"
    }

    private val cellSize by lazy { height.toFloat() / 9 }
    private var puzzle: Array<IntArray>? = null
    private var pressedCell: Pair<Int, Int>? = null
    private var focusedCell: Pair<Int, Int>? = null

    private val borderPaint = Paint().apply {
        color = Color.BLACK
    }

    private val horizontalLines by lazy { List<FloatArray>(9) { idx -> floatArrayOf(0f, cellSize * (idx + 1), width.toFloat(), cellSize * (idx + 1)) } }
    private val verticalLines by lazy { List<FloatArray>(9) { idx -> floatArrayOf(cellSize * (idx + 1), 0f, cellSize * (idx + 1), height.toFloat()) } }

    private val textPaint by lazy {
        Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textSize = cellSize * 3 / 4
        }
    }

    private val focusedCellPaint = Paint().apply {
        color = Color.argb(80, 0, 0, 255)
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

        horizontalLines.forEachIndexed { idx, line ->
            borderPaint.strokeWidth = if ((idx + 1) % 3 == 0) 5f else 1f
            canvas.drawLines(line, borderPaint)
        }

        verticalLines.forEachIndexed { idx, line ->
            borderPaint.strokeWidth = if ((idx + 1) % 3 == 0) 5f else 1f
            canvas.drawLines(line, borderPaint)
        }

        puzzle?.let {
            it.forEachIndexed { row, intArray ->
                intArray.forEachIndexed { col, int ->
                    if (int != 0) {
                        val cellValue = "$int"
                        val labelWidth = textPaint.measureText(cellValue)
                        val labelHeight = textPaint.textSize
                        // Log.d(TAG, "cellSize: $cellSize, labelHeight: $labelHeight, textSize: ${textPaint.textSize}")
                        canvas.drawText(cellValue, horizontalLines[col][3] - (cellSize + labelWidth) / 2, verticalLines[row][0] - (cellSize - labelHeight) / 2, textPaint)
                    }
                }
            }
        }

        focusedCell?.let {
            val highlightedCells = mutableListOf<Pair<Int, Int>>()
            val boundary = findBoundary(it)
            focusedCellPaint.color = Color.argb(80, 0, 0, 255) // blue
            canvas.drawRect(boundary[0], boundary[1], boundary[2], boundary[3], focusedCellPaint)
            highlightedCells.add(it)

            focusedCellPaint.color = Color.argb(60, 128, 128, 128) // grey
            val sameRow = List(9) { idx -> pairOf(idx, it.second) }
            sameRow.forEach { cell ->
                if (!highlightedCells.contains(cell)) {
                    val boundary = findBoundary(cell)
                    canvas.drawRect(boundary[0], boundary[1], boundary[2], boundary[3], focusedCellPaint)
                    highlightedCells.add(cell)
                }
            }

            val sameCol = List(9) { idx -> pairOf(it.first, idx) }
            sameCol.forEach { cell ->
                if (!highlightedCells.contains(cell)) {
                    val boundary = findBoundary(cell)
                    canvas.drawRect(boundary[0], boundary[1], boundary[2], boundary[3], focusedCellPaint)
                    highlightedCells.add(cell)
                }
            }

            val sameSubGrid = getSubGridCells(it)
            sameSubGrid.forEach { cell ->
                if (!highlightedCells.contains(cell)) {
                    val boundary = findBoundary(cell)
                    canvas.drawRect(boundary[0], boundary[1], boundary[2], boundary[3], focusedCellPaint)
                    highlightedCells.add(cell)
                }
            }
        }
    }

    fun getSubGridCells(cell: Pair<Int, Int>): List<Pair<Int, Int>> {
        val (row, col) = cell
        val subGridSize = 3 // For a standard 9x9 Sudoku
        val startRow = (row / subGridSize) * subGridSize
        val startCol = (col / subGridSize) * subGridSize

        val cells = mutableListOf<Pair<Int, Int>>()

        for (r in startRow until startRow + subGridSize) {
            for (c in startCol until startCol + subGridSize) {
                cells.add(Pair(r, c))
            }
        }

        return cells
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                playSoundEffect(android.view.SoundEffectConstants.CLICK)
                val x = event.x
                val y = event.y
//                Log.d(TAG, "ACTION_DOWN - ($x, $y)")
                pressedCell = findCell(x, y)
//                Log.d(TAG, "pressedCell - $pressedCell")
                invalidate() // Request to redraw
                return true
            }

            MotionEvent.ACTION_UP -> {
                val x = event.x
                val y = event.y
//                Log.d(TAG, "ACTION_UP - ($x, $y)")
                val releasedCell = findCell(x, y)
//                Log.d(TAG, "pressedCell - $pressedCell")
                if (releasedCell != null && releasedCell == pressedCell) {
                    if (focusedCell != releasedCell) {
                        focusedCell = releasedCell
                        Log.d(TAG, "focusedCell - $focusedCell")
                    } else {
                        focusedCell = null
                    }
                }
                pressedCell = null
                invalidate() // Request to redraw
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun findCell(x: Float, y: Float): Pair<Int, Int>? {
//        Log.d(TAG, "y: $y, horizontalLines: ${horizontalLines.map { it.toList() }}")
//        Log.d(TAG, "x: $x, verticalLines: ${verticalLines.map { it.toList() }}")

        val yRanges = horizontalLines.map { (it[1] - cellSize).rangeUntil(it[1]) }
        val xRanges = verticalLines.map { (it[0] - cellSize).rangeUntil(it[0]) }
//        Log.d(TAG, "yRanges: $yRanges")
//        Log.d(TAG, "xRanges: $xRanges")
        val yMatch = yRanges.indexOfFirst { y in it }
        val xMatch = xRanges.indexOfFirst { x in it }
//        Log.d(TAG, "yMatch: $yMatch, xMatch: $xMatch")

        if (yMatch != -1 && xMatch != -1) {
            return Pair(xMatch, yMatch)
        }
        return null
    }

    private fun findBoundary(cell: Pair<Int, Int>): FloatArray {
        val yRanges = horizontalLines.map { (it[1] - cellSize).rangeUntil(it[1]) }
        val xRanges = verticalLines.map { (it[0] - cellSize).rangeUntil(it[0]) }
        val yRange = yRanges[cell.second]
        val xRange = xRanges[cell.first]
//        Log.d(TAG, "yRange: $yRange, xRange: $xRange")
        return floatArrayOf(xRange.start, yRange.start, xRange.endExclusive, yRange.endExclusive)
    }

    fun updatePuzzle(puzzle: Array<IntArray>?) {
        this.puzzle = puzzle
        invalidate()
    }

    fun <A, B> pairOf(first: A, second: B): Pair<A, B> {
        return Pair(first, second)
    }
}