# Health Connect Integration

## Overview
This Android application integrates with Google Health Connect to record and retrieve fitness data, including distance, calories burned, heart rate, and exercise sessions.

## Features
- Requests necessary Health Connect permissions.
- Saves workout data, including distance, calories burned, and heart rate.
- Detects if Health Connect is installed and prompts installation if missing.
- Uses `HealthConnectClient` for data storage and retrieval.

## Permissions
The following permissions are required in `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.health.READ_DISTANCE"/>
<uses-permission android:name="android.permission.health.WRITE_DISTANCE"/>
<uses-permission android:name="android.permission.health.READ_TOTAL_CALORIES_BURNED"/>
<uses-permission android:name="android.permission.health.WRITE_TOTAL_CALORIES_BURNED"/>
<uses-permission android:name="android.permission.health.READ_HEART_RATE"/>
<uses-permission android:name="android.permission.health.WRITE_HEART_RATE"/>
<uses-permission android:name="android.permission.health.READ_EXERCISE"/>
<uses-permission android:name="android.permission.health.WRITE_EXERCISE"/>
```

## Installation
1. Clone the repository:
   ```sh
   git clone https://github.com/bitf12m008/HealthConnectSample.git
   ```
2. Open the project in Android Studio.
3. Build and run the app on a physical device or emulator with Health Connect installed.

## Usage
1. Launch the app.
2. Click the "Save Workout" button.
3. If permissions are not granted, a request prompt appears.
4. If Health Connect is not installed, the app redirects to the Play Store.
5. Once permissions are granted, the workout is saved.

## Key Classes
- **`MainActivity`**: Handles UI interactions and triggers workout saving.
- **`HealthConnectManager`**: Manages permissions, Health Connect availability, and data insertion.

## Dependencies
Ensure the following dependencies are added to your `build.gradle`:
```gradle
dependencies {
    implementation 'androidx.health.connect:connect-client:1.0.0-alpha11'
}
```

