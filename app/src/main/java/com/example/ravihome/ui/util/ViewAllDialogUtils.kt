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
        showList(
            context = context,
            title = title,
            rows = rows,
            onRowDelete = { index ->
                LocalHistoryStore.removeAt(context, key, index)
                onChanged()
            },
            onClearAll = {
                LocalHistoryStore.clear(context, key)
                onChanged()
            }
        )
    }

    fun show(
        context: Context,
        title: String,
        rows: List<String>,
        onRowClick: ((index: Int) -> Unit)? = null,
        onRowDelete: ((index: Int) -> Unit)? = null
    ) {
        showList(
            context = context,
            title = title,
            rows = rows,
            onRowClick = onRowClick,
            onRowDelete = onRowDelete
        )
    }

    private fun showList(
        context: Context,
        title: String,
        rows: List<String>,
        onRowClick: ((index: Int) -> Unit)? = null,
        onRowDelete: ((index: Int) -> Unit)? = null,
        onClearAll: (() -> Unit)? = null
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
            .setTitle(title)
            .setItems(tableRows) { _, which ->
                onRowClick?.invoke(which)
            }
            .setNeutralButton("Delete Row") { _, _ ->
                if (onRowDelete == null) return@setNeutralButton
                MaterialAlertDialogBuilder(context)
                    .setTitle("Delete which row?")
                    .setItems(tableRows) { _, which ->
                        onRowDelete.invoke(which)
                        Toast.makeText(context, "Deleted row ${which + 1}", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .show()
            }
            .setNegativeButton("Clear All") { _, _ -> onClearAll?.invoke() }
            .setPositiveButton("Close", null)
            .show()
    }
}