package fr.skyle.escapy.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest

/**
 * Base class of the tests of a ViewModel: its coroutines run at once, on the thread of the test. The
 * main dispatcher is set at construction, before the ViewModel of the subclass is built.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class MainDispatcherTest {

    /** The virtual time of the test, which runTest shares with the main dispatcher. */
    protected val mainScheduler = TestCoroutineScheduler()

    init {
        Dispatchers.setMain(UnconfinedTestDispatcher(mainScheduler))
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }
}
