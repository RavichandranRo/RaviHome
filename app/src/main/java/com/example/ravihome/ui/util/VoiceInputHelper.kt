package com.example.ravihome.ui.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.ViewParent
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class VoiceInputHelper(private val fragment: Fragment) {

    private var activeField: EditText? = null
    private var onResult: ((String) -> Unit)? = null
    private val voiceLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                onResult?.invoke("")
                onResult = null
                return@registerForActivityResult
            }
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                activeField?.setText(spokenText)
                onResult?.invoke(spokenText)
            } else {
                onResult?.invoke("")
            }
            onResult = null
        }

    fun attachTo(field: EditText, prompt: String? = null) {
        findTextInputLayout(field.parent)?.apply {
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            setEndIconDrawable(android.R.drawable.ic_btn_speak_now)
            setEndIconOnClickListener { startVoiceInput(field, prompt) }
            endIconContentDescription = "Voice input"
        }

        field.setOnLongClickListener {
            startVoiceInput(field, prompt)
            true
        }
    }

    fun startForCommand(prompt: String, onText: (String) -> Unit) {
        activeField = null
        onResult = onText
        launchRecognizer(prompt)
    }

    private fun startVoiceInput(field: EditText, prompt: String?) {
        activeField = field
        onResult = null
        launchRecognizer(prompt ?: "Speak now")
    }

    private fun launchRecognizer(prompt: String) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
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

    private fun findTextInputLayout(parent: ViewParent?): TextInputLayout? {
        var node: ViewParent? = parent
        while (node != null) {
            if (node is TextInputLayout) return node
            node = node.parent
        }
        return null
    }
}
