package com.example.ravihome.ui.deposits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ravihome.databinding.FragmentDepositsBinding
import com.google.android.material.tabs.TabLayoutMediator

class DepositsFragment : Fragment() {

    private lateinit var binding: FragmentDepositsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDepositsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewPager.adapter = DepositPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Fixed" else "Recurring"
        }.attach()
    }
}