package com.example.ravihome.ui.util

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object ViewAllDialogUtils {
    fun show(context: Context, title: String, rows: List<String>) {
        val message = if (rows.isEmpty()) {
            "No saved items yet."
        } else {
            rows.mapIndexed { index, raw ->
                val normalized = raw
                    .replace(" • ", "\n")
                    .replace("→", "\n→ ")
                "${index + 1})\n$normalized"
            }.joinToString("\n\n────────────\n\n")
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }
}