package com.puzzle.sudoku.ui.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.puzzle.sudoku.R
import com.puzzle.sudoku.ui.component.ProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BasicDataBindingActivity<LAYOUT : ViewDataBinding> : AppCompatActivity() {
    abstract val layoutResourceId: Int
    protected val binding by lazy<LAYOUT> {
        DataBindingUtil.setContentView(this, layoutResourceId)
    }

    private val progressDialog: AlertDialog by lazy {
        ProgressDialog(this).create()
    }
    private var progressStart = 0L

    open fun setBindingData() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBindingData()
    }

    protected fun showLoadingIndicator(show: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (show) {
                progressDialog.show()
                progressStart = System.currentTimeMillis()
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

    protected fun showDialog(context: Context, title: String?, message: String? = null, onCloseCallback: (() -> Unit)? = null) {
        MaterialAlertDialogBuilder(context)
            .setCancelable(false).apply {
                title?.let {
                    setTitle(it)
                }
                message?.let {
                    setMessage(it)
                }
            }
            .setPositiveButton(R.string.button_close) { _, _ ->
                onCloseCallback?.invoke()
            }
            .show()
    }
}