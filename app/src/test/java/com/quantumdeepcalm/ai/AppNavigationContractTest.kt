package com.quantumdeepcalm.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationContractTest {

    @Test
    fun topLevelContractIsValid() {
        assertTrue(AppNavigationContract.hasValidTopLevelContract())
    }

    @Test
    fun topLevelRoutesAreUnique() {
        val routes = AppNavigationContract.topLevelDestinations.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun startRouteIsReachableFromTopLevelNavigation() {
        assertTrue(
            AppNavigationContract.topLevelDestinations.any {
                it.route == AppNavigationContract.START_ROUTE
            },
        )
    }
}
