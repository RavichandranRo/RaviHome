package com.example.ravihome.ui.eb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.ravihome.databinding.FragmentEbBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EbFragment : Fragment() {

    private lateinit var binding: FragmentEbBinding
    private val viewModel: EbViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEbBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.etCurrent.addTextChangedListener {
            val prev = binding.etPrevious.text.toString().toFloatOrNull() ?: return@addTextChangedListener
            val curr = it.toString().toFloatOrNull() ?: return@addTextChangedListener
            val units = curr - prev
            binding.tvUnits.text = "Units: $units"
            binding.tvAmount.text = "Amount: ₹${viewModel.calculate(units)}"
        }
    }
}
