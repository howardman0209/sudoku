package com.puzzle.sudoku.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.lifecycleScope
import com.puzzle.sudoku.ui.component.InputKeypad
import com.puzzle.sudoku.R
import com.puzzle.sudoku.util.SudokuHelper
import com.puzzle.sudoku.databinding.ActivitySudokuBinding
import com.puzzle.sudoku.model.Difficulty
import com.puzzle.sudoku.ui.component.SudokuBoard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SudokuActivity() : BasicDataBindingActivity<ActivitySudokuBinding>(), InputKeypad.InputListener, SudokuBoard.EventListener {
    companion object {
        const val TAG = "SudokuActivity"
    }

    override val layoutResourceId: Int = R.layout.activity_sudoku
    private var puzzle: Array<IntArray> = Array(9) { IntArray(9) }
    private var solution: Map<Pair<Int, Int>, Int>? = null
    private var firstBackPressedTime = 0L
    private var difficulty = Difficulty.HARD
    private var focusedCell: Pair<Int, Int>? = null

    val isEditing = ObservableField(false)

    override fun setBindingData() {
        binding.view = this
        super.setBindingData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.inputKeypad.setListener(this)
        binding.sudokuBoard.setListener(this)

        binding.btnNewGame.setOnClickListener {
            showLoadingIndicator(true)
            lifecycleScope.launch(Dispatchers.Default) {
                val (puzzle, solution) = SudokuHelper.generatePuzzleAndSolutionSet(difficulty = difficulty)
                this@SudokuActivity.puzzle = puzzle
                this@SudokuActivity.solution = solution

                withContext(Dispatchers.Main) {
                    showLoadingIndicator(false)
                    syncPuzzleBoard()
                }
            }
        }

        binding.btnEdit.apply {
            setImageResource(if (isEditing.get() == true) R.drawable.ic_round_done_outline_24 else R.drawable.ic_baseline_edit_24)
            setOnClickListener {
                val isEditing = isEditing.get() == true
                showLoadingIndicator(true)
                lifecycleScope.launch(Dispatchers.IO) {
                    if (isEditing) {
                        val trial = SudokuHelper.fillBoard(puzzle.map { it.clone() }.toTypedArray())
                        if (trial.isNotEmpty()) {
                            solution = trial
                            Log.d(TAG, "solution: $solution")
                        } else {
                            puzzle = Array(9) { IntArray(9) }
                        }
                    } else {
                        puzzle = Array(9) { IntArray(9) }
                        solution = null
                    }

                    withContext(Dispatchers.Main) {
                        showLoadingIndicator(false)
                        this@SudokuActivity.isEditing.set(!isEditing)
                        setImageResource(if (!isEditing) R.drawable.ic_baseline_edit_24 else R.drawable.ic_round_done_outline_24)

                        if (isEditing && solution.isNullOrEmpty()) {
                            showDialog(this@SudokuActivity, getString(R.string.dialog_message_invalid_sudoku_puzzle))
                        }
                        syncPuzzleBoard()
                    }
                }
            }

            setOnLongClickListener {
                if (isEditing.get() != true) {
                    solution?.forEach { key, value ->
                        binding.sudokuBoard.markOrEraseAnswer(value, key)
                    }
                }
                true
            }
        }

        binding.btnClear.setOnClickListener {
            if (isEditing.get() == true) {
                val cell = focusedCell ?: return@setOnClickListener
                puzzle[cell.second][cell.first] = 0
                syncPuzzleBoard()
            } else {
                binding.sudokuBoard.clearCell()
            }
        }


        binding.spinnerDifficulty.apply {
            val items = Difficulty.entries.toTypedArray()
            val adapter = ArrayAdapter(this@SudokuActivity, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
            this.setSelection(items.indexOf(Difficulty.HARD))

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    difficulty = items[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun syncPuzzleBoard() {
        binding.sudokuBoard.initBoard(puzzle, solution)
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
        if (isEditing.get() == true) {
            val cell = focusedCell ?: return
            puzzle[cell.second][cell.first] = key
            syncPuzzleBoard()
        } else {
            if (binding.checkBoxNote.isChecked != true) {
                binding.sudokuBoard.markOrEraseAnswer(key)
            } else {
                binding.sudokuBoard.markOrEraseNote(key)
            }
        }
    }

    override fun onCellFocused(cell: Pair<Int, Int>?) {
        focusedCell = cell
    }
}