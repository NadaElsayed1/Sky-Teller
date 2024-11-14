package com.example.clearsky.favourite.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class ViewModelRule(val dispature: TestCoroutineDispatcher = TestCoroutineDispatcher()): TestWatcher(),
TestCoroutineScope by TestCoroutineScope(dispature)
{
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispature)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.setMain(dispature)
        cleanupTestCoroutines()
    }
}