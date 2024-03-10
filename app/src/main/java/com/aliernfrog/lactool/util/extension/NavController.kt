package com.aliernfrog.lactool.util.extension

import androidx.navigation.NavController
import com.aliernfrog.lactool.util.Destination

/**
 * Pops back stack only if it exists.
 */
fun NavController.popBackStackSafe(onNoBackStack: () -> Unit = {}) {
    if (previousBackStackEntry != null) popBackStack()
    else onNoBackStack()
}

/**
 * Navigates to given [destination] and removes previous destinations from back stack.
 */
fun NavController.set(destination: Destination) {
    navigate(destination.route) { popUpTo(0) }
}