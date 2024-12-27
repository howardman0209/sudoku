package com.puzzle.sudoku.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View
import com.puzzle.sudoku.R
import com.puzzle.sudoku.extension.measureTextHeight

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
    private var solution: Map<Pair<Int, Int>, Int>? = null
    private val notesData: MutableMap<Pair<Int, Int>, List<Int>> = mutableMapOf()
    private var pressedCell: Pair<Int, Int>? = null
    private var focusedCell: Pair<Int, Int>? = null

    private val borderPaint = Paint().apply {
        color = Color.BLACK
    }

    private val horizontalLines by lazy { List<FloatArray>(9) { idx -> floatArrayOf(0f, cellSize * (idx + 1), width.toFloat(), cellSize * (idx + 1)) } }
    private val verticalLines by lazy { List<FloatArray>(9) { idx -> floatArrayOf(cellSize * (idx + 1), 0f, cellSize * (idx + 1), height.toFloat()) } }

    private val notePaint by lazy {
        Paint().apply {
            color = Color.argb(255, 100, 100, 100)
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textSize = cellSize / 4
        }
    }

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
        // draw background color
        canvas.drawColor(Color.WHITE)

        // draw grid
        horizontalLines.forEachIndexed { idx, line ->
            borderPaint.strokeWidth = if ((idx + 1) % 3 == 0) 5f else 1f
            canvas.drawLines(line, borderPaint)
        }

        verticalLines.forEachIndexed { idx, line ->
            borderPaint.strokeWidth = if ((idx + 1) % 3 == 0) 5f else 1f
            canvas.drawLines(line, borderPaint)
        }

        val puzzleSnapshot = puzzle
        if (puzzleSnapshot == null) {
            return
        }

        puzzleSnapshot.forEachIndexed { row, intArray ->
            intArray.forEachIndexed { col, int ->
                if (int != 0) {
                    val cellValue = "$int"
                    val labelWidth = textPaint.measureText(cellValue)
                    val labelHeight = textPaint.measureTextHeight(cellValue)
                    // Log.d(TAG, "cellSize: $cellSize, labelHeight: $labelHeight, textSize: ${textPaint.textSize}")
                    canvas.drawText(cellValue, verticalLines[col][0] - (cellSize + labelWidth) / 2, horizontalLines[row][1] - (cellSize - labelHeight) / 2, textPaint)
                }
            }
        }

        notesData.forEach { cell, notes ->
            markNotes(canvas, cell, notes)
        }

        focusedCell?.let {
            val highlightedCells = mutableListOf<Pair<Int, Int>>()
            fun highlightCell(cell: Pair<Int, Int>, color: Int) {
                val boundary = findBoundary(cell)
                focusedCellPaint.color = color
                canvas.drawRect(boundary[0], boundary[1], boundary[2], boundary[3], focusedCellPaint)
            }

            // focused cell
            highlightCell(it, Color.argb(80, 0, 0, 255))// blue
            highlightedCells.add(it)

            // cells in the same row
            val sameRowCells = List(9) { idx -> pairOf(idx, it.second) }
            sameRowCells.forEach { cell ->
                if (!highlightedCells.contains(cell)) {
                    highlightCell(cell, Color.argb(60, 128, 128, 128))// grey
                    highlightedCells.add(cell)
                }
            }

            // cells in the same column
            val sameColCells = List(9) { idx -> pairOf(it.first, idx) }
            sameColCells.forEach { cell ->
                if (!highlightedCells.contains(cell)) {
                    highlightCell(cell, Color.argb(60, 128, 128, 128))// grey
                    highlightedCells.add(cell)
                }
            }

            val sameSubGridCells = getSubGridCells(it)
            sameSubGridCells.forEach { cell ->
                if (!highlightedCells.contains(cell)) {
                    highlightCell(cell, Color.argb(60, 128, 128, 128))// grey
                    highlightedCells.add(cell)
                }
            }

            val cellValue = puzzleSnapshot[it.second][it.first]
            Log.d(TAG, "cellValue: $cellValue")
            if (cellValue in 1..9) {
                val sameValueCells = findSameValueCells(puzzleSnapshot, cellValue)
                sameValueCells.forEach { cell ->
                    if (!highlightedCells.contains(cell)) {
                        highlightCell(cell, Color.argb(128, 80, 80, 80))// dark grey
                        highlightedCells.add(cell)
                    }
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

    fun findSameValueCells(sudoku: Array<IntArray>, value: Int): List<Pair<Int, Int>> {
        val cells = mutableListOf<Pair<Int, Int>>()

        for (row in sudoku.indices) {
            for (col in sudoku[row].indices) {
                if (sudoku[row][col] == value) {
                    cells.add(Pair(col, row))
                }
            }
        }

        return cells
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                playSoundEffect(SoundEffectConstants.CLICK)
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

    private fun markNotes(canvas: Canvas, cell: Pair<Int, Int>, notes: List<Int>) {
        val boundary = findBoundary(cell)
        val subCellSize = cellSize / 3
        notes.forEach {
            val xOffset = subCellSize * ((it - 1) % 3)
            val yOffset = subCellSize * ((it - 1) / 3)
            val xStart = boundary[0] + notePaint.measureText("$it") / 2 + xOffset
            val yStart = boundary[1] + notePaint.textSize + yOffset
//            Log.d(TAG, "note: $it, offset: $offset")
            canvas.drawText("$it", xStart, yStart, notePaint)
        }
    }

    fun updatePuzzle(puzzle: Array<IntArray>?, solution: Map<Pair<Int, Int>, Int>?) {
        this.puzzle = puzzle
        this.solution = solution
        invalidate()
    }

    fun markOrEraseNote(note: Int) {
        val cell = focusedCell ?: run { return }
        val notes = notesData.getOrDefault(cell, emptyList())
        val updatedNotes = if (!notes.contains(note)) {
            notes.plus(note)
        } else {
            notes.minus(note)
        }
        notesData.put(cell, updatedNotes)
        invalidate()
    }

    fun clearCell() {
        val cell = focusedCell ?: run { return }
        notesData.put(cell, emptyList())
        invalidate()
    }

    fun <A, B> pairOf(first: A, second: B): Pair<A, B> {
        return Pair(first, second)
    }
}