package com.example.ravihome.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.ravihome.databinding.FragmentPaymentCreditBinding
import com.example.ravihome.ui.util.BillStorageUtils
import com.example.ravihome.ui.util.LocalHistoryStore
import com.example.ravihome.ui.util.PopupUtils
import com.example.ravihome.ui.util.ViewAllDialogUtils
import com.example.ravihome.ui.util.VoiceInputHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PaymentCreditFragment : Fragment() {

    private lateinit var binding: FragmentPaymentCreditBinding
    private val voiceInputHelper by lazy { VoiceInputHelper(this) }
    private val pickBill =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri ?: return@registerForActivityResult
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val path = BillStorageUtils.storeBill(requireContext(), uri)
            PopupUtils.showAutoDismiss(
                requireContext(),
                "Bill saved",
                "Stored in: $path"
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaymentCreditBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun refreshRecent() {
        val items = LocalHistoryStore.list(requireContext(), "payment_credit")
        binding.tvRecent.text =
            if (items.isEmpty()) "No recent payments" else items.take(3).joinToString("\n")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshRecent()

        binding.btnViewAll.setOnClickListener {
            ViewAllDialogUtils.show(
                requireContext(),
                "All Credit payments",
                "payment_credit",
                LocalHistoryStore.list(requireContext(), "payment_credit")
            ) { refreshRecent() }
        }
        voiceInputHelper.attachTo(binding.etAmount, "Speak amount")
        voiceInputHelper.attachTo(binding.etNote, "Speak note")
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
        binding.btnUploadBill.setOnClickListener {
            pickBill.launch(arrayOf("application/pdf", "image/*"))
        }
    }

    private fun showConfirmDialog(type: String, amount: Double, note: String) {
        val dialogView =
            layoutInflater.inflate(com.example.ravihome.R.layout.dialog_payment_confirm, null)
        dialogView.findViewById<android.widget.TextView>(com.example.ravihome.R.id.tvTitle).text =
            "Confirm $type payment"
        dialogView.findViewById<android.widget.TextView>(com.example.ravihome.R.id.tvAmount).text =
            "Amount: ₹%.2f".format(amount)
        dialogView.findViewById<android.widget.TextView>(com.example.ravihome.R.id.tvNote).text =
            if (note.isBlank()) "Note: --" else "Note: $note"

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm") { _, _ ->
                LocalHistoryStore.append(
                    requireContext(),
                    "payment_credit",
                    "Credit: ₹%.2f • ".format(amount) + (if (note.isBlank()) "No note" else note)
                )
                PaymentBalanceStore.applyCredit(requireContext(), amount)
                refreshRecent()
                showSuccessDialog(type, amount)
            }
            .show()
    }

    private fun showSuccessDialog(type: String, amount: Double) {
        val dialogView =
            layoutInflater.inflate(com.example.ravihome.R.layout.dialog_payment_success, null)
        dialogView.findViewById<android.widget.TextView>(com.example.ravihome.R.id.tvMessage).text =
            "$type payment of ₹%.2f completed".format(amount)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Done", null)
            .show()
    }
}