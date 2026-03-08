package com.example.ravihome.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.ravihome.databinding.FragmentPaymentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialFadeThrough

class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pagerAdapter = PaymentPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Credit" else "Debit"
        }.attach()
        maybePromptOpeningBalance()
        refreshBalance()

        binding.btnManageDenominations.setOnClickListener { openDenominationDialog() }
        binding.btnAddReceived.setOnClickListener { openAddAmountDialog() }
    }

    override fun onResume() {
        super.onResume()
        refreshBalance()
    }

    private fun maybePromptOpeningBalance() {
        if (!PaymentBalanceStore.shouldPromptOpeningBalance(requireContext())) return

        val input = EditText(requireContext()).apply { hint = "Enter opening balance" }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Opening balance for this month")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->
                val value = input.text?.toString()?.toDoubleOrNull() ?: 0.0
                PaymentBalanceStore.setOpeningBalance(requireContext(), value)
                refreshBalance()
            }
            .show()
    }

    private fun refreshBalance() {
        binding.tvBalance.text = "Current Balance: ₹%.2f".format(
            PaymentBalanceStore.getBalance(requireContext())
        )

        val counts = PaymentCashStore.getCounts(requireContext())
        val breakdown = counts.filterValues { it > 0 }
            .entries
            .sortedByDescending { it.key }
            .joinToString("  ") { "₹${it.key}×${it.value}" }
            .ifBlank { "No denominations saved" }

        binding.tvCashInHand.text =
            "Cash in hand: ₹${PaymentCashStore.totalInHand(requireContext())}"
        binding.tvCashBreakdown.text = breakdown
    }

    private fun openDenominationDialog() {
        val context = requireContext()
        val existing = PaymentCashStore.getCounts(context)

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 12, 24, 0)
        }

        val inputs = mutableMapOf<Int, TextInputEditText>()
        PaymentCashStore.denominations.forEach { denom ->
            val til = TextInputLayout(context).apply {
                hint = "₹$denom count"
            }
            val et = TextInputEditText(context).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setText((existing[denom] ?: 0).toString())
            }
            til.addView(et)
            container.addView(til)
            inputs[denom] = et
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("Cash denomination manager")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val counts = PaymentCashStore.denominations.associateWith { denom ->
                    inputs[denom]?.text?.toString()?.toIntOrNull()?.coerceAtLeast(0) ?: 0
                }
                PaymentCashStore.setAllCounts(context, counts)
                refreshBalance()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAddAmountDialog() {
        val amountInput = EditText(requireContext()).apply {
            hint = "Enter received amount"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add received cash")
            .setView(amountInput)
            .setPositiveButton("Add") { _, _ ->
                val amount = amountInput.text?.toString()?.toIntOrNull() ?: 0
                if (amount > 0) {
                    PaymentCashStore.addReceivedAmount(requireContext(), amount)
                    refreshBalance()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}