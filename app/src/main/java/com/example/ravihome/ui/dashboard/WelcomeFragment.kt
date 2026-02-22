package com.example.ravihome.ui.dashboard

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ravihome.R
import com.example.ravihome.databinding.FragmentWelcomeBinding
import com.example.ravihome.ui.util.PopupUtils
import com.example.ravihome.ui.util.VoiceInputHelper

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private val voiceInputHelper by lazy { VoiceInputHelper(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        animateFloatingCards()
        PopupUtils.showAutoDismiss(
            requireContext(),
            "Welcome",
            "Welcome to Ravi Home. Please choose a module."
        )

        binding.cardPlanned.setOnClickListener {
            navigate(
                R.id.plannedWorksFragment,
                "Planned Works"
            )
        }
        binding.cardCompleted.setOnClickListener {
            navigate(
                R.id.completedWorksFragment,
                "Completed Works"
            )
        }
        binding.cardEb.setOnClickListener { navigate(R.id.ebFragment, "EB") }
        binding.cardTravel.setOnClickListener { navigate(R.id.travelFragment, "Travel") }
        binding.cardPayment.setOnClickListener { navigate(R.id.paymentFragment, "Payments") }
        binding.cardDeposits.setOnClickListener { navigate(R.id.depositsFragment, "Deposits") }

        binding.btnSpeakNavigate.setOnClickListener {
            PopupUtils.showAutoDismiss(
                requireContext(),
                "Voice navigation",
                "Listening for module name"
            )
            voiceInputHelper.startForCommand("Speak a view name like travel or payments") { spoken ->
                if (spoken.isBlank()) {
                    PopupUtils.showAutoDismiss(
                        requireContext(),
                        "Navigation failed",
                        "No voice command detected."
                    )
                } else {
                    routeByVoice(spoken)
                }
            }
        }

        binding.btnExit.setOnClickListener {
            PopupUtils.showAutoDismiss(requireContext(), "Exiting", "Closing Ravi Home.")
            requireActivity().finish()
        }
    }

    private fun routeByVoice(spoken: String) {
        val text = spoken.lowercase()
        when {
            "planned" in text -> navigate(R.id.plannedWorksFragment, "Planned Works")
            "completed" in text -> navigate(R.id.completedWorksFragment, "Completed Works")
            "eb" in text || "electric" in text -> navigate(R.id.ebFragment, "EB")
            "travel" in text || "train" in text || "bus" in text -> navigate(
                R.id.travelFragment,
                "Travel"
            )

            "payment" in text || "pay" in text -> navigate(R.id.paymentFragment, "Payments")
            "deposit" in text -> navigate(R.id.depositsFragment, "Deposits")
            "exit" in text -> {
                PopupUtils.showAutoDismiss(requireContext(), "Exiting", "Closing Ravi Home.")
                requireActivity().finish()
            }

            else -> PopupUtils.showAutoDismiss(
                requireContext(),
                "Navigation failed",
                "Could not map '$spoken' to a view."
            )
        }
    }

    private fun navigate(destination: Int, label: String) {
        PopupUtils.showAutoDismiss(requireContext(), "Navigating", "Navigating to $label")
        findNavController().navigate(destination)
    }

    private fun animateFloatingCards() {
        listOf(
            binding.cardPlanned,
            binding.cardCompleted,
            binding.cardEb,
            binding.cardTravel,
            binding.cardPayment,
            binding.cardDeposits
        ).forEachIndexed { index, card ->
            val floatAnimator = ObjectAnimator.ofPropertyValuesHolder(
                card,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -10f, 0f),
                PropertyValuesHolder.ofFloat(View.ROTATION, 0f, 1.2f, 0f)
            ).apply {
                duration = 2200L + (index * 120L)
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
            }
            floatAnimator.start()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
