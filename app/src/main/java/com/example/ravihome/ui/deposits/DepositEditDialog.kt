package com.example.ravihome.ui.deposits

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import com.example.ravihome.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DepositEditDialog {
    fun show(
        context: Context,
        entry: DepositEntry,
        onSave: (DepositEntry) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_deposit_edit, null)
        val amountInput = dialogView.findViewById<EditText>(R.id.etAmount)
        val durationInput = dialogView.findViewById<EditText>(R.id.etDuration)
        val rateInput = dialogView.findViewById<EditText>(R.id.etRate)
        val bankInput = dialogView.findViewById<EditText>(R.id.etBank)

        amountInput.setText(entry.amount.toString())
        durationInput.setText(entry.durationMonths.toString())
        rateInput.setText(entry.rate.toString())
        bankInput.setText(entry.bank)

        MaterialAlertDialogBuilder(context)
            .setTitle("Edit deposit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updated = entry.copy(
                    amount = amountInput.text?.toString()?.toDoubleOrNull() ?: entry.amount,
                    durationMonths = durationInput.text?.toString()?.toIntOrNull()
                        ?: entry.durationMonths,
                    rate = rateInput.text?.toString()?.toDoubleOrNull() ?: entry.rate,
                    bank = bankInput.text?.toString()?.trim().orEmpty().ifBlank { entry.bank }
                )
                onSave(updated)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}