package com.snapmechanic.app.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

/**
 * Extension functions — these add helper methods to existing Android classes
 * so we don't repeat the same boilerplate in every Activity.
 */

// Show a short Toast message from any Context
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

// Show a long Toast message
fun Context.toastLong(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// Show a Snackbar from any View — used for error messages
fun View.snackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
}

// Show a Snackbar with an action button (e.g., "Retry")
fun View.snackbarAction(message: String, actionText: String, action: () -> Unit) {
    Snackbar.make(this, message, Snackbar.LENGTH_INDEFINITE)
        .setAction(actionText) { action() }
        .show()
}

// Make a view visible
fun View.visible() {
    visibility = View.VISIBLE
}

// Make a view invisible (still takes space in layout)
fun View.invisible() {
    visibility = View.INVISIBLE
}

// Make a view gone (no space in layout)
fun View.gone() {
    visibility = View.GONE
}

// Hide the soft keyboard from any Activity
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let { view ->
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

// Format rating to 1 decimal place string
fun Double.toRatingString(): String = String.format("%.1f", this)

// Format distance string
fun Double.toDistanceString(): String = String.format("%.1f km away", this)
