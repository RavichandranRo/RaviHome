package com.example.ravihome.ui.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

object BillStorageUtils {
    fun storeBill(context: Context, uri: Uri): String {
        val fileName = queryName(context, uri)
        val directory = File(context.filesDir, "bills").apply { mkdirs() }
        val outputFile = File(directory, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return outputFile.absolutePath
    }

    private fun queryName(context: Context, uri: Uri): String {
        val fallback = "bill_${System.currentTimeMillis()}"
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return fallback
        cursor.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                return it.getString(nameIndex) ?: fallback
            }
        }
        return fallback
    }
}