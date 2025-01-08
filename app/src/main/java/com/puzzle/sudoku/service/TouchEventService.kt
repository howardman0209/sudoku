package com.puzzle.sudoku.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class TouchEventService : AccessibilityService() {
    companion object {
        const val TAG = "TouchEventService"
        var instance: TouchEventService? = null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "TouchEventService onCreate")
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "TouchEventService onDestroy")
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    fun performTap(x: Float, y: Float) {
        Log.d(TAG, "simulateTap - ($x,$y)")
        val gesture = GestureDescription.Builder()
        val path = Path().apply { moveTo(x, y) }
        gesture.addStroke(GestureDescription.StrokeDescription(path, 0, 100))
        dispatchGesture(gesture.build(), null, null)
    }

    fun performDrag(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long) {
        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(
                    Path().apply {
                        moveTo(startX, startY) // Adjust based on layout
                        lineTo(endX, endY) // Adjust based on layout
                    }, 0L, duration
                )
            )
            .build()

        dispatchGesture(gesture, null, null)
    }
}