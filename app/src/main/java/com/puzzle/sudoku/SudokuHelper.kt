package com.puzzle.sudoku

import android.util.Log
import kotlin.random.Random

object SudokuHelper {
    const val TAG = "SudokuHelper"

    fun generatePuzzleAndSolutionSet(numberOfEmptyCell: Int): Pair<Array<IntArray>, Map<Pair<Int, Int>, Int>> {
        val solvedBoard = generateSolvedSudoku()
        val puzzleAndSolutionPair = createPuzzleAndSolutionPair(solvedBoard, numberOfEmptyCell)
        printSudoku(puzzleAndSolutionPair.first)
        Log.d(TAG, puzzleAndSolutionPair.second.toString())
        return puzzleAndSolutionPair
    }

    private fun generateSolvedSudoku(): Array<IntArray> {
        val board = Array(9) { IntArray(9) }
        fillBoard(board)
        return board
    }

    fun fillBoard(board: Array<IntArray>): Boolean {
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                if (board[i][j] == 0) {
                    val numbers = (1..9).shuffled() // Randomize the numbers
                    for (num in numbers) {
                        if (isSafe(board, i, j, num)) {
                            board[i][j] = num // Place the number
                            if (fillBoard(board)) {
                                return true
                            }
                            board[i][j] = 0 // Backtrack
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    fun isSafe(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // Check row and column
        for (x in 0 until 9) {
            if (board[row][x] == num || board[x][col] == num) {
                return false
            }
        }
        // Check 3x3 subgrid
        val startRow = row - row % 3
        val startCol = col - col % 3
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i + startRow][j + startCol] == num) {
                    return false
                }
            }
        }
        return true
    }

    fun createPuzzleAndSolutionPair(solvedBoard: Array<IntArray>, cellsToRemove: Int): Pair<Array<IntArray>, Map<Pair<Int, Int>, Int>> {
        val puzzle = solvedBoard.map { it.clone() }.toTypedArray() // Clone the solved board
        var removed = 0

        // Keep track of the positions of cells removed
        val positions = mutableMapOf<Pair<Int, Int>, Int>()

        // Continue removing cells until we reach the desired count
        while (removed < cellsToRemove) {
            // Randomly select a cell to remove
            val i = Random.nextInt(0, 9)
            val j = Random.nextInt(0, 9)

            // Ensure we only remove a filled cell
            if (puzzle[i][j] != 0) {
                // Log.d(TAG, "chosen cell: i:$i, j:$j")
                val temp = puzzle.map { it.clone() }.toTypedArray() // Clone the solved board
                temp[i][j] = 0 // Remove the cell
                val possibleSolutionCount = countSolutions(temp)
                // Log.d(TAG, "puzzle: ${printSudoku(temp)}, possibleSolutionCount: $possibleSolutionCount")
                // Check if the puzzle still has a unique solution
                if (possibleSolutionCount == 1) {
                    // Log.d(TAG, "removed: $removed -> find next\n ")
                    positions.put(Pair(i, j), puzzle[i][j]) // Track removed position
                    puzzle[i][j] = 0
                    removed++
                }
            }
        }

        return puzzle to positions.toSortedMap(compareBy<Pair<Int, Int>> { it.first }.thenBy { it.second })
    }

    fun countSolutions(board: Array<IntArray>): Int {
        var count = 0

        fun solve(): Boolean {
            for (i in 0 until 9) {
                for (j in 0 until 9) {
                    if (board[i][j] == 0) { // Find an empty cell
                        for (num in 1..9) { // Try numbers 1-9
                            if (isSafe(board, i, j, num)) {
                                board[i][j] = num // Place the number
                                if (solve()) { // Recur to fill the next cell
                                    count++
                                    if (count > 1) return false // More than one solution found
                                }
                                board[i][j] = 0 // Backtrack
                            }
                        }
                        return false // No valid number can be placed, backtrack
                    }
                }
            }
            return true // All cells filled
        }

        solve()
        return count
    }

    fun printSudoku(board: Array<IntArray>) {
        val builder = StringBuilder()
        for (row in board) {
            for (num in row) {
                builder.append(if (num == 0) ". " else "$num ")
            }
            builder.append("\n")
        }
        Log.d(TAG, builder.toString())
    }
}