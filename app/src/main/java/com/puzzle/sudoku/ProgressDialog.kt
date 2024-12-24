package com.puzzle.sudoku

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.puzzle.sudoku.databinding.DialogProgressBinding
import kotlin.apply

class ProgressDialog(context: Context) : MaterialAlertDialogBuilder(context) {
    init {
        val layoutBinding: DialogProgressBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_progress, null, false)
        setView(layoutBinding.root)
        setCancelable(false)
        background = ColorDrawable(Color.TRANSPARENT)
    }

    override fun create(): AlertDialog {
        val dialog = super.create()
        dialog.window?.apply {
            setDimAmount(.4f)
            // for full screen dialog need to set the decorView padding to be 0
            // decorView.setPadding(0, 0, 0, 0)
        }
        return dialog
    }

}