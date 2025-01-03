package com.puzzle.sudoku.model

enum class Difficulty(val maxNumOfBlank: Int) {
    EASY(40),
    MEDIUM(50),
    HARD(64)
}