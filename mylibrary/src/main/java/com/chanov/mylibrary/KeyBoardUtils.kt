package com.chanov.mylibrary

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.WindowManager.LayoutParams
import android.view.inputmethod.InputMethodManager

fun View.showKeyboard() {
  getInputMethodService().showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard() {
  getInputMethodService().hideSoftInputFromWindow(windowToken, 0)
}

fun View.showFocusAndKeyboardDialog(dialog: Dialog?) {
  requestFocus()
  dialog?.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)
}

fun View.requestFocusAndShowKeyboard() {
  requestFocus()
  showKeyboard()
}

/**
 * IMPORTANT: Set parent container:
 * android:focusable="true"
 * android:focusableInTouchMode="true"
 */
fun View.clearFocusAndHideKeyboard() {
  clearFocus()
  hideKeyboard()
}

private fun View.getInputMethodService() =
  (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)