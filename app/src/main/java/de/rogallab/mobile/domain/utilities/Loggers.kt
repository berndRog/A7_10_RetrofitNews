package de.rogallab.mobile.domain.utilities

import android.util.Log
import de.rogallab.mobile.AppStart.Companion.IS_DEBUG
import de.rogallab.mobile.AppStart.Companion.IS_INFO

fun logError(tag: String, message: String) {
   val msg = formatMessage(message)
   Log.e(tag, msg)
}
fun logWarning(tag: String, message: String) {
   val msg = formatMessage(message)
   Log.w(tag, msg)
}
fun logInfo(tag: String, message: String) {
   val msg = formatMessage(message)
   if(IS_INFO) Log.i(tag, msg)
}

fun logDebug(tag: String, message: String) {
   val msg = formatMessage(message)
   if (IS_DEBUG) Log.d(tag, msg)
}

fun logVerbose(tag: String, message: String) {
   Log.v(tag, message)
}

private fun formatMessage(message: String) =
   String.format("%-90s %s", message, Thread.currentThread().toString())
