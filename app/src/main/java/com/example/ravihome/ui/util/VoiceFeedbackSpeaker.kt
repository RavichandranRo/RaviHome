package com.example.ravihome.ui.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object VoiceFeedbackSpeaker {
    private var tts: TextToSpeech? = null
    private var ready = false

    fun speak(context: Context, text: String) {
        val appContext = context.applicationContext
        if (tts == null) {
            tts = TextToSpeech(appContext) { status ->
                ready = status == TextToSpeech.SUCCESS
                if (ready) {
                    tts?.language = Locale.US
                    tts?.setPitch(1.28f)
                    tts?.setSpeechRate(0.92f)
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "app_voice_feedback")
                }
            }
            return
        }
        if (ready) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "app_voice_feedback")
        }
    }
}