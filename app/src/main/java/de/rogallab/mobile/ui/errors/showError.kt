package de.rogallab.mobile.ui.errors

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.navigation.NavEvent

suspend fun showError(
   snackbarHostState: SnackbarHostState,  // State ↓
   params: ErrorParams,                   // State ↓
   onNavigate: (NavEvent) -> Unit,        // Event ↑
) {
   // Show Snackbar
   snackbarHostState.showSnackbar(
      message = params.throwable?.message ?: params.message,
      actionLabel = params.actionLabel,
      withDismissAction = params.withUndoAction,
      duration = params.duration
   ).also { result: SnackbarResult ->
      // Handle Snackbar action
      when (result) {
         SnackbarResult.ActionPerformed -> {
            logVerbose("showError", "Snackbar action performed")
            params.onUndoAction()
         }
         SnackbarResult.Dismissed -> {
            logVerbose("showError", "Snackbar dismissed")
         }
      }
   }

   // navigate to target
   params.navEvent?.let { navEvent ->
      onNavigate(navEvent)
   }
}