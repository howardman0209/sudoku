package com.puzzle.sudoku.util

import android.util.Log
import com.puzzle.sudoku.model.Difficulty
import java.util.LinkedList

object SudokuHelper {
    const val TAG = "SudokuHelper"

    fun generatePuzzleAndSolutionSet(difficulty: Difficulty): Pair<Array<IntArray>, Map<Pair<Int, Int>, Int>> {
        val solvedBoard = generateSolvedSudoku()
        val puzzleAndSolutionPair = createPuzzleAndSolutionPair(solvedBoard, difficulty.maxNumOfBlank)
        printSudoku(puzzleAndSolutionPair.first) // print puzzle
        Log.d(TAG, "(${puzzleAndSolutionPair.second.size}) ${puzzleAndSolutionPair.second}") // print solution
        return puzzleAndSolutionPair
    }

    private fun generateSolvedSudoku(): Array<IntArray> {
        val board = Array(9) { IntArray(9) }
        fillBoard(board)
        return board
    }

    fun fillBoard(board: Array<IntArray>, filled: Map<Pair<Int, Int>, Int> = emptyMap()): Map<Pair<Int, Int>, Int> { // return filled cell data
        if (!isValid(board)) {
            return emptyMap()
        }
        val solution: MutableMap<Pair<Int, Int>, Int> = mutableMapOf()
        solution.putAll(filled)

        for (i in 0 until 9) {
            for (j in 0 until 9) {
                if (board[i][j] == 0) {
                    val numbers = (1..9).shuffled() // Randomize the numbers
                    for (num in numbers) {
                        if (isSafe(board, i, j, num)) {
                            board[i][j] = num // Place the number
                            solution.put(Pair(j, i), num)
                            val trial = fillBoard(board, solution)
                            if (trial.isNotEmpty()) {
                                solution.putAll(trial)
                                return solution
                            }
                            solution.remove(Pair(j, i))
                            board[i][j] = 0 // Backtrack
                        }
                    }
                    return emptyMap()
                }
            }
        }
        return solution
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

    fun isValid(board: Array<IntArray>): Boolean {
        val seen = HashSet<String>()

        for (i in 0..8) {
            for (j in 0..8) {
                val num = board[i][j]
                if (num != 0) { // Assuming 0 represents an empty cell
                    val rowKey = "row_$i$num"
                    val colKey = "col_$j$num"
                    val boxKey = "box_${i / 3}_${j / 3}$num"

                    if (seen.contains(rowKey) || seen.contains(colKey) || seen.contains(boxKey)) {
                        return false
                    }

                    seen.add(rowKey)
                    seen.add(colKey)
                    seen.add(boxKey)
                }
            }
        }
        return true
    }

    fun createPuzzleAndSolutionPair(solvedBoard: Array<IntArray>, cellsToRemove: Int): Pair<Array<IntArray>, Map<Pair<Int, Int>, Int>> {
        val puzzle = solvedBoard.map { it.clone() }.toTypedArray() // Clone the solved board
        var removed = 0

        // Keep track of the positions of cells removed
        val solution = mutableMapOf<Pair<Int, Int>, Int>()
        val cellIndices = LinkedList<Int>(List(81) { it }.shuffled())

        // Continue removing cells until we reach the desired count
        while (removed < cellsToRemove) {
            // Randomly select a cell to remove
            val cellIndex = cellIndices.pop()
            val i = cellIndex / 9
            val j = cellIndex % 9

            if (cellIndices.isEmpty()) { // cannot remove clue anymore
                Log.d(TAG, "no more clue can be removed")
                break
            }

            // Ensure we only remove a filled cell
            if (puzzle[i][j] != 0) {
//                printSudoku(puzzle)
                Log.d(TAG, "remains: ${cellIndices.size}, removed: $removed, chosen cell: i:$i, j:$j")
                val temp = puzzle.map { it.clone() }.toTypedArray() // Clone the solved board
                temp[i][j] = 0 // Remove the cell
                val possibleSolutionCount = countSolutions(temp)
//                Log.d(TAG, "possibleSolutionCount: $possibleSolutionCount")
                // Check if the puzzle still has a unique solution
                if (possibleSolutionCount == 1) {
                    solution.put(Pair(j, i), puzzle[i][j]) // Track removed position
                    puzzle[i][j] = 0
                    removed++
                }
            }
        }

        return puzzle to solution.toSortedMap(compareBy<Pair<Int, Int>> { it.second }.thenBy { it.first })
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