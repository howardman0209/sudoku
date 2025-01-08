package com.puzzle.sudoku.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.input.InputManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.puzzle.sudoku.R
import com.puzzle.sudoku.databinding.OverlayLayoutBinding

class OverlayService : Service() {

    private lateinit var overlayBinding: OverlayLayoutBinding

    override fun onCreate() {
        super.onCreate()
        Log.d("OverlayService", "onCreate")
        createOverlay()
    }

    private fun createOverlay() {
        overlayBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.overlay_layout, null, false)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_INPUT_METHOD
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
            PixelFormat.TRANSLUCENT
        )
        val inputManager = getSystemService(INPUT_SERVICE) as InputManager
        layoutParams.alpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            inputManager.maximumObscuringOpacityForTouch
        } else {
            0.8F
        }

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayBinding.root, layoutParams)

        overlayBinding.worldPanel.post {
            overlayBinding.worldPanel.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.removeView(overlayBinding.root)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}