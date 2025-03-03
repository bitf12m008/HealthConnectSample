package com.precor.fitness

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.health.connect.client.records.StepsRecord
import java.time.Instant
import java.time.ZoneOffset

class MainActivity : AppCompatActivity() {
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var permissionLauncher: ActivityResultLauncher<Set<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure XML is set

        healthConnectManager = HealthConnectManager(this)

        // Register the permission contract
        permissionLauncher =
            registerForActivityResult(healthConnectManager.requestPermissionsActivityContract()) { grantedPermissions ->
                if (grantedPermissions.containsAll(PERMISSIONS)) {
                    onPermissionsGranted()
                } else {
                    onPermissionsDenied()
                }
            }

        // Find Button and Set Click Listener
        val btnSaveSteps = findViewById<Button>(R.id.btnWriteSteps)
        btnSaveSteps.setOnClickListener {
            checkAndRequestPermissions() // Request permission on button click
        }
    }

    private fun checkAndRequestPermissions() {
        lifecycleScope.launch {
            if (!healthConnectManager.hasAllPermissions()) {
                permissionLauncher.launch(PERMISSIONS)
            } else {
                onPermissionsGranted()
            }
        }
    }

    private fun onPermissionsGranted() {
        saveSteps(1000) // Example: Save 1000 steps after permission granted
    }

    private fun onPermissionsDenied() {
        Log.e("HealthConnect", "Permission denied! Cannot save steps.")
    }

    private fun saveSteps(stepCount: Long) {
        lifecycleScope.launch {
            try {
                val client = healthConnectManager.healthConnectClient

                val stepsRecord = StepsRecord(
                    metadata = androidx.health.connect.client.records.metadata.Metadata(),
                    count = stepCount,
                    startTime = Instant.now().minusSeconds(3600),
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = Instant.now(),
                    endZoneOffset = ZoneOffset.UTC
                )

                client.insertRecords(listOf(stepsRecord)) // Use insertRecords()

                Log.d("HealthConnect", "Steps saved successfully!")
            } catch (e: Exception) {
                Log.e("HealthConnect", "Failed to save steps", e)
            }
        }
    }
}
