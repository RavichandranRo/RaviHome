package com.example.ravihome.ui.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.util.Locale

class VoiceInputHelper(private val fragment: Fragment) {

    private var activeField: EditText? = null
    private val voiceLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }
            val spokenText =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                activeField?.setText(spokenText)
            }
        }

    fun attachTo(field: EditText, prompt: String? = null) {
        field.setOnLongClickListener {
            startVoiceInput(field, prompt)
            true
        }
    }

    private fun startVoiceInput(field: EditText, prompt: String?) {
        activeField = field
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt ?: "Speak now")
        }
        try {
            voiceLauncher.launch(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                fragment.requireContext(),
                "Voice input not available on this device.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}