package com.example.ravihome.ui.export

import android.app.AlertDialog
import android.content.Context

object ExportDialog {

    fun show(
        context: Context,
        onExport: (range: ExportRange, format: ExportFormat) -> Unit
    ) {
        val formats = ExportFormat.values().map { it.label }.toTypedArray()
        val ranges = ExportRange.values().map { it.label }.toTypedArray()

        var selectedFormat = ExportFormat.CSV
        var selectedRange = ExportRange.TODAY

        AlertDialog.Builder(context)
            .setTitle("Export Data")
            .setSingleChoiceItems(formats, 0) { _, which ->
                selectedFormat = ExportFormat.values()[which]
            }
            .setPositiveButton("Next") { _, _ ->
                AlertDialog.Builder(context)
                    .setTitle("Select Duration")
                    .setSingleChoiceItems(ranges, 0) { _, which ->
                        selectedRange = ExportRange.values()[which]
                    }
                    .setPositiveButton("Export") { _, _ ->
                        onExport(selectedRange, selectedFormat)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
