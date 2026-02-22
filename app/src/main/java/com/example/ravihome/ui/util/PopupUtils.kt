package com.example.ravihome.ui.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.StringRes
import com.example.ravihome.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object PopupUtils {

    private const val AUTO_DISMISS_MS = 1600L

    fun showAutoDismiss(
        context: Context,
        title: String,
        message: String
    ) {
        val dialog = MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_RaviHome_AnimatedDialog
        )
            .setTitle("✅ $title")
            .setIcon(R.drawable.ic_launcher_foreground)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .create()

        dialog.show()
        VoiceFeedbackSpeaker.speak(context, "$title. $message")
        Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, AUTO_DISMISS_MS)
    }

    fun showAutoDismiss(
        context: Context,
        @StringRes titleRes: Int,
        @StringRes messageRes: Int
    ) {
        showAutoDismiss(context, context.getString(titleRes), context.getString(messageRes))
    }

    fun showUndo(
        context: Context,
        title: String,
        message: String,
        onUndo: () -> Unit
    ) {
        val dialog = MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_RaviHome_AnimatedDialog
        )
            .setTitle("✅ $title")
            .setIcon(R.drawable.ic_launcher_foreground)
            .setMessage(message)
            .setPositiveButton(R.string.undo) { _, _ -> onUndo() }
            .setNegativeButton(R.string.dismiss, null)
            .create()

        dialog.show()
        VoiceFeedbackSpeaker.speak(context, "$title. $message")
        Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, AUTO_DISMISS_MS)
    }
}