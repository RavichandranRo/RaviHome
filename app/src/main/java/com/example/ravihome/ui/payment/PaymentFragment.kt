package com.example.ravihome.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.ravihome.databinding.FragmentPaymentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
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
    }
}