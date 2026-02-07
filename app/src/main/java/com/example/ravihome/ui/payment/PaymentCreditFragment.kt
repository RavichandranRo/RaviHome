package com.example.ravihome.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ravihome.databinding.FragmentPaymentCreditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PaymentCreditFragment : Fragment() {

    private lateinit var binding: FragmentPaymentCreditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaymentCreditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnPay.setOnClickListener {
            val amountText = binding.etAmount.text?.toString()?.trim().orEmpty()
            val amount = amountText.toDoubleOrNull() ?: 0.0
            val note = binding.etNote.text?.toString()?.trim().orEmpty()
            if (amount <= 0) {
                binding.etAmount.error = "Enter a valid amount"
                return@setOnClickListener
            }
            showConfirmDialog("Credit", amount, note)
        }
    }

    private fun showConfirmDialog(type: String, amount: Double, note: String) {
        val dialogView = layoutInflater.inflate(com.example.ravihome.R.layout.dialog_payment_confirm, null)
        dialogView.findViewById<android.widget.TextView>(com.example.ravihome.R.id.tvTitle).text = "Confirm $type payment"
        dialogView.findViewById<android.widget.TextView>(com.example.ravihome.R.id.tvAmount).text = "Amount: ₹%.2f".format(amount)
        dialogView.findViewById<android.widget.TextView>(com.example.ravihome.R.id.tvNote).text =
            if (note.isBlank()) "Note: --" else "Note: $note"

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm") { _, _ ->
                showSuccessDialog(type, amount)
            }
            .show()
    }

    private fun showSuccessDialog(type: String, amount: Double) {
        val dialogView = layoutInflater.inflate(com.example.ravihome.R.layout.dialog_payment_success, null)
        dialogView.findViewById<android.widget.TextView>(com.example.ravihome.R.id.tvMessage).text =
            "$type payment of ₹%.2f completed".format(amount)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Done", null)
            .show()
    }
}