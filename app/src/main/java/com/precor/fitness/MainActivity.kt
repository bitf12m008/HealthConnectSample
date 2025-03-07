package com.precor.fitness

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import java.time.Instant
import java.time.ZoneOffset

class MainActivity : AppCompatActivity() {
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var permissionLauncher: ActivityResultLauncher<Set<String>>
    private var pendingIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        healthConnectManager = HealthConnectManager(this)

        permissionLauncher =
            registerForActivityResult(healthConnectManager.requestPermissionsActivityContract()) { grantedPermissions ->
                if (grantedPermissions.containsAll(PERMISSIONS)) {
                    onPermissionsGranted()
                } else {
                    onPermissionsDenied()
                }
            }

        val btnSaveWorkout = findViewById<Button>(R.id.btnSaveWorkout)
        btnSaveWorkout.setOnClickListener {
            Toast.makeText(this@MainActivity, "Scan QR Code to save workout data.", Toast.LENGTH_LONG).show()
        }

        if (intent.action == Intent.ACTION_VIEW) {
            pendingIntent = intent
            checkAndRequestPermissions()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent?.action == Intent.ACTION_VIEW) {
            pendingIntent = intent
            checkAndRequestPermissions()
        }
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                val totalDistance = uri.getQueryParameter("totalDistance")?.toDoubleOrNull() ?: 0.0
                val caloriesBurned = uri.getQueryParameter("caloriesBurned")?.toDoubleOrNull() ?: 0.0
                val timeElapsed = uri.getQueryParameter("timeElapsed")?.toLongOrNull() ?: 0L
                val avgHeartRate = uri.getQueryParameter("avgHeartRate")?.toDoubleOrNull() ?: 0.0
                val workoutTitle = uri.getQueryParameter("workoutTitle") ?: "Unknown Workout"


                saveManualWorkout(totalDistance, caloriesBurned, timeElapsed, avgHeartRate, workoutTitle)
            }
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
        Log.e("HealthConnect", "Permission granted! Saving workout.")
        pendingIntent?.let { handleIntent(it) }
        pendingIntent = null
    }

    private fun onPermissionsDenied() {
        Log.e("HealthConnect", "Permission denied! Cannot save workout.")
        Toast.makeText(this, "Permission denied! Cannot save workout.", Toast.LENGTH_LONG).show()
    }

    private fun saveManualWorkout(
        totalDistance: Double,
        caloriesBurned: Double,
        timeElapsed: Long,
        avgHeartRate: Double,
        workoutTitle: String,
    ) {
        lifecycleScope.launch {
            try {
                val client = healthConnectManager.healthConnectClient
                val now = Instant.now()
                val startTime = now.minusSeconds(timeElapsed)

                val distanceRecord = DistanceRecord(
                    metadata = androidx.health.connect.client.records.metadata.Metadata(),
                    distance = Length.meters(totalDistance),
                    startTime = startTime,
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = now,
                    endZoneOffset = ZoneOffset.UTC
                )

                val caloriesRecord = TotalCaloriesBurnedRecord(
                    metadata = androidx.health.connect.client.records.metadata.Metadata(),
                    energy = Energy.kilocalories(caloriesBurned),
                    startTime = startTime,
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = now,
                    endZoneOffset = ZoneOffset.UTC
                )

                val heartRateRecord = HeartRateRecord(
                    metadata = androidx.health.connect.client.records.metadata.Metadata(),
                    samples = listOf(
                        HeartRateRecord.Sample(
                            time = startTime.plusSeconds(timeElapsed / 2),
                            beatsPerMinute = avgHeartRate.toLong()
                        )
                    ),
                    startTime = startTime,
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = now,
                    endZoneOffset = ZoneOffset.UTC
                )

                val workoutRecord = ExerciseSessionRecord(
                    metadata = androidx.health.connect.client.records.metadata.Metadata(),
                    title = workoutTitle,
                    startTime = startTime,
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = now,
                    endZoneOffset = ZoneOffset.UTC,
                    exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING
                )

                client.insertRecords(
                    listOf(distanceRecord, caloriesRecord, heartRateRecord, workoutRecord)
                )

                Log.d("HealthConnect", "$workoutTitle workout saved successfully!")
                Toast.makeText(this@MainActivity, "$workoutTitle workout saved successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("HealthConnect", "Failed to save manual workout", e)
                Toast.makeText(this@MainActivity, "Failed to save manual workout", Toast.LENGTH_LONG).show()
            }
        }
    }
}
