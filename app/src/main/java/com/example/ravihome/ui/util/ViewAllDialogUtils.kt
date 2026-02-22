package com.example.ravihome.ui.util

import android.content.Context
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object ViewAllDialogUtils {
    fun show(
        context: Context,
        title: String,
        key: String,
        rows: List<String>,
        onChanged: () -> Unit = {}
    ) {
        if (rows.isEmpty()) {
            MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage("No saved items yet.")
                .setPositiveButton("Close", null)
                .show()
            return
        }
        val tableRows = rows.mapIndexed { index, row ->
            "%2d | %s".format(index + 1, row.replace(" • ", " | "))
        }.toTypedArray()

        MaterialAlertDialogBuilder(context)
            .setTitle("$title (tap row to delete)")
            .setItems(tableRows) { _, which ->
                LocalHistoryStore.removeAt(context, key, which)
                Toast.makeText(context, "Deleted row ${which + 1}", Toast.LENGTH_SHORT).show()
                onChanged()
            }
            .setNeutralButton("Clear All") { _, _ ->
                LocalHistoryStore.clear(context, key)
                onChanged()
            }
            .setPositiveButton("Close", null)
            .show()
    }

    fun show(context: Context, title: String, rows: List<String>) {
        val message = if (rows.isEmpty()) {
            "No saved items yet."
        } else {
            buildString {
                append("# | Details\n")
                append("--------------------------------\n")
                rows.forEachIndexed { index, row ->
                    append("${index + 1} | ${row.replace(" • ", " | ")}\n")
                }
            }
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }
}