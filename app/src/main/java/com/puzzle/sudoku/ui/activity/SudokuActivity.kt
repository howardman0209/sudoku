package com.puzzle.sudoku.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.puzzle.sudoku.ui.component.InputKeypad
import com.puzzle.sudoku.R
import com.puzzle.sudoku.util.SudokuHelper
import com.puzzle.sudoku.databinding.ActivitySudokuBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SudokuActivity() : BasicDataBindingActivity<ActivitySudokuBinding>(), InputKeypad.InputListener {
    companion object {
        const val TAG = "SudokuActivity"
    }

    override val layoutResourceId: Int = R.layout.activity_sudoku
    private var puzzle: Array<IntArray>? = null
    private var solution: Map<Pair<Int, Int>, Int>? = null
    private var firstBackPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.inputKeypad.setListener(this)

        binding.btnNewGame.setOnClickListener {
            showLoadingIndicator(true)
            lifecycleScope.launch(Dispatchers.Default) {
                val (puzzle, solution) = SudokuHelper.generatePuzzleAndSolutionSet(numberOfEmptyCell = 53)
                this@SudokuActivity.puzzle = puzzle
                this@SudokuActivity.solution = solution

                withContext(Dispatchers.Main) {
                    showLoadingIndicator(false)
                    syncPuzzleBoard()
                }
            }
        }

        binding.btnTips.setOnClickListener {
            val items = this@SudokuActivity.solution?.map { "${it.key}=${it.value}" }.orEmpty().toTypedArray()
            val randomTips = items.getOrNull(Random.Default.nextInt(items.size.coerceAtLeast(1)))
            MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle(R.string.label_tips)
                .setMessage(randomTips)
                .setNegativeButton(R.string.button_close) { _, _ -> }
                .create()
                .show()
        }

        binding.btnTips.setOnLongClickListener {
            MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle(R.string.label_solution)
                .setItems(
                    this@SudokuActivity.solution?.map { "${it.key}=${it.value}" }.orEmpty().toTypedArray()
                ) { _, index -> }
                .setNegativeButton(R.string.button_close) { _, _ -> }
                .create()
                .show()
            true
        }

        binding.btnClear.setOnClickListener {
            binding.sudokuBoard.clearCell()
        }
    }

    private fun syncPuzzleBoard() {
        binding.sudokuBoard.updatePuzzle(puzzle, solution)
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (System.currentTimeMillis() - firstBackPressedTime < 2000) {
            super.onBackPressed()
        } else {
            firstBackPressedTime = System.currentTimeMillis()
            Toast.makeText(this, R.string.toast_message_click_exit_again, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onKeyInput(key: Int) {
        // Log.i(TAG, "onKeyInput - key: $key")
        if (binding.checkBoxNote.isChecked != true) {
            binding.sudokuBoard.markOrEraseAnswer(key)
        } else {
            binding.sudokuBoard.markOrEraseNote(key)
        }
    }
}