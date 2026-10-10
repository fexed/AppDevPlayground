package com.fexed.playground.presentation.tasks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Replaces Android's main dispatcher with a controllable dispatcher in local JVM tests.
 *
 * ViewModels normally launch work on Main. Sharing this test dispatcher with `runTest` makes that
 * work deterministic and lets the test scheduler advance it without an emulator.
 */
// The coroutine test library marks setMain/resetMain experimental; opt in only for this helper.
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    /** Installs the test dispatcher before each test creates its ViewModel. */
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    /** Restores the process dispatcher so configuration cannot leak between tests. */
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
