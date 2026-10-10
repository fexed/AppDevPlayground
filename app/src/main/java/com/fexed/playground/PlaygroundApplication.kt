package com.fexed.playground

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application root for Hilt's object graph.
 *
 * Dependency injection starts at the Android application lifecycle so dependencies such as the
 * database can be shared safely across activities and ViewModels instead of being constructed
 * ad hoc by presentation code.
 */
// Hilt generates the application component and uses this class as its Android lifecycle entry point.
@HiltAndroidApp
class PlaygroundApplication : Application()
