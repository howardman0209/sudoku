package com.puzzle.sudoku

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.puzzle.sudoku.databinding.ActivitySudokuBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SudokuActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySudokuBinding.inflate(layoutInflater) }
    private var puzzle: Array<IntArray>? = null
    private var solution: Map<Pair<Int, Int>, Int>? = null

    private val progressDialog: AlertDialog by lazy {
        ProgressDialog(this).create()
    }
    private var progressStart = 0L
    private var firstBackPressedTime = 0L


    private fun showLoadingIndicator(show: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (show) {
                progressStart = System.currentTimeMillis()
                progressDialog.show()
            } else {
                val processTime = System.currentTimeMillis() - progressStart
                Log.d("progressDialog", "processTime: $processTime")
                if (processTime < 500) {
                    delay(500L - processTime)
                    progressDialog.dismiss()
                } else {
                    progressDialog.dismiss()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnNewGame.setOnClickListener {
            showLoadingIndicator(true)
            lifecycleScope.launch(Dispatchers.IO) {
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
            val randomTips = items.getOrNull(Random.nextInt(items.size.coerceAtLeast(1)))
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
    }

    private fun syncPuzzleBoard() {
        binding.sudokuBoard.updatePuzzle(puzzle)
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
}