package com.puzzle.sudoku.ui.activity

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.puzzle.sudoku.R
import com.puzzle.sudoku.service.OverlayService
import com.puzzle.sudoku.service.TouchEventService

class MainActivity : AppCompatActivity() {

    val settingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        var allConditionPassed = true
        permissionsRequiredAndRequestIntent.forEach {
            if (!it.first.invoke()) {
                allConditionPassed = false
            }
        }
        if (allConditionPassed) {
            startOverlayService()
            finish()
        } else {
            requestMissingPermissions()
        }
    }

    private val permissionsRequiredAndRequestIntent = listOf<Pair<() -> Boolean, Intent>>(
        Pair({ canDrawOverlays() }, Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))),
        Pair({ isAccessibilityServiceEnabled(TouchEventService::class.java) }, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (permissionsRequiredAndRequestIntent.all { it.first.invoke() == true }) {
            Log.d("MainActivity", "starting OverlayService")
            startOverlayService()
            finish()
        } else {
            requestMissingPermissions()
        }
    }

    private fun canDrawOverlays(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun requestMissingPermissions() {
        val intent = permissionsRequiredAndRequestIntent.first { it.first.invoke() != true }.second
        settingLauncher.launch(intent)
    }

    private fun isAccessibilityServiceEnabled(service: Class<out AccessibilityService>): Boolean {
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val colonSeparatedServices = enabledServices.split(":")
        // Check if the service is in the list
        return colonSeparatedServices.any { it.contains(service.name, ignoreCase = true) }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        startService(intent)
    }
}