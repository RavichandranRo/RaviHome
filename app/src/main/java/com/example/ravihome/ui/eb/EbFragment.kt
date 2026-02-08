package com.example.ravihome.ui.eb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.ravihome.databinding.FragmentEbBinding
import com.example.ravihome.ui.util.PopupUtils
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EbFragment : Fragment() {

    private lateinit var binding: FragmentEbBinding
    private val viewModel: EbViewModel by viewModels()

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
        binding = FragmentEbBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var latestAmount: Float? = null

        fun recalc() {
            val prev = binding.etPrevious.text.toString().toFloatOrNull()
            val curr = binding.etCurrent.text.toString().toFloatOrNull()
            binding.tilCurrent.error = null
            latestAmount = null
            binding.btnRecordPayment.isEnabled = false

            if (prev == null || curr == null) {
                binding.tvUnits.text = "Units: --"
                binding.tvAmount.text = "Amount: --"
                return
            }

            val units = curr - prev
            if (units < 0) {
                binding.tilCurrent.error = "Current reading must be greater than previous"
                binding.tvUnits.text = "Units: --"
                binding.tvAmount.text = "Amount: --"
                return
            }

            val amount = viewModel.calculate(units)
            latestAmount = amount
            binding.btnRecordPayment.isEnabled = amount > 0f
            binding.tvUnits.text = "Units: %.1f".format(units)
            binding.tvAmount.text = "Amount: ₹%.2f".format(amount)
        }


        binding.etPrevious.addTextChangedListener { recalc() }
        binding.etCurrent.addTextChangedListener { recalc() }

        binding.btnRecordPayment.setOnClickListener {
            val amount = latestAmount ?: return@setOnClickListener
            viewModel.recordPayment(amount)
            PopupUtils.showAutoDismiss(
                requireContext(),
                "EB payment saved",
                "Payment recorded successfully."
            )        }
    }
}
