package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class LiveVoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING
}

enum class CoachPersona(
    val id: String,
    val title: String,
    val description: String,
    val speechPitch: Float,
    val speechRate: Float
) {
    ZENITH(
        id = "zenith",
        title = "Coach Zenith",
        description = "Empatik, berstruktur & pakar neuroplastisiti BJ Fogg.",
        speechPitch = 1.0f,
        speechRate = 1.0f
    ),
    MAYA(
        id = "maya",
        title = "Coach Maya",
        description = "Lembut, ceria & memberikan dorongan emosi positif.",
        speechPitch = 1.15f,
        speechRate = 1.05f
    ),
    ALEX(
        id = "alex",
        title = "Coach Alex",
        description = "Tegas, berdisiplin tinggi & fokus kepada tindakan segera.",
        speechPitch = 0.92f,
        speechRate = 1.02f
    )
}

class GeminiLiveVoiceManager(private val context: Context) {

    private val _voiceState = MutableStateFlow(LiveVoiceState.IDLE)
    val voiceState: StateFlow<LiveVoiceState> = _voiceState.asStateFlow()

    private val _audioRmsLevel = MutableStateFlow(0.1f)
    val audioRmsLevel: StateFlow<Float> = _audioRmsLevel.asStateFlow()

    private val _interimTranscript = MutableStateFlow("")
    val interimTranscript: StateFlow<String> = _interimTranscript.asStateFlow()

    private val _selectedPersona = MutableStateFlow(CoachPersona.ZENITH)
    val selectedPersona: StateFlow<CoachPersona> = _selectedPersona.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onSpeechError: ((String) -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                val localeMs = Locale("ms", "MY")
                val result = tts?.setLanguage(localeMs)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.US
                }
                applyPersonaSettings(_selectedPersona.value)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _voiceState.value = LiveVoiceState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        _voiceState.value = LiveVoiceState.IDLE
                        _audioRmsLevel.value = 0.05f
                    }

                    override fun onError(utteranceId: String?) {
                        _voiceState.value = LiveVoiceState.IDLE
                        _audioRmsLevel.value = 0.05f
                    }
                })
            }
        }
    }

    fun setPersona(persona: CoachPersona) {
        _selectedPersona.value = persona
        applyPersonaSettings(persona)
    }

    private fun applyPersonaSettings(persona: CoachPersona) {
        tts?.setPitch(persona.speechPitch)
        tts?.setSpeechRate(persona.speechRate)
    }

    fun startListening() {
        stopSpeaking()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onSpeechError?.invoke("Pengecaman suara tidak disokong pada peranti ini.")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _voiceState.value = LiveVoiceState.LISTENING
                    _interimTranscript.value = "Mendengar..."
                }

                override fun onBeginningOfSpeech() {
                    _interimTranscript.value = ""
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Normalize RMS dB (typical range -2 to 10) to 0.0f - 1.0f
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.08f, 1.0f)
                    _audioRmsLevel.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _voiceState.value = LiveVoiceState.PROCESSING
                }

                override fun onError(error: Int) {
                    _voiceState.value = LiveVoiceState.IDLE
                    _audioRmsLevel.value = 0.05f
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Tiada suara dikesan. Sila cuba lagi."
                        SpeechRecognizer.ERROR_NETWORK -> "Masalah rangkaian internet."
                        SpeechRecognizer.ERROR_AUDIO -> "Ralat mikrofon audio."
                        else -> "Sesi input suara selesai."
                    }
                    if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                        onSpeechError?.invoke(errorMessage)
                    }
                }

                override fun onResults(results: Bundle?) {
                    _voiceState.value = LiveVoiceState.PROCESSING
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim()
                    if (!text.isNullOrBlank()) {
                        _interimTranscript.value = text
                        onSpeechRecognized?.invoke(text)
                    } else {
                        _voiceState.value = LiveVoiceState.IDLE
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()
                    if (!text.isNullOrBlank()) {
                        _interimTranscript.value = text
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ms-MY")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ms-MY")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Bercakap dengan Gemini Live...")
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceState.value = LiveVoiceState.IDLE
            onSpeechError?.invoke("Gagal memulakan mikrofon: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignore
        }
        _voiceState.value = LiveVoiceState.IDLE
        _audioRmsLevel.value = 0.05f
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        stopListening()
        if (!isTtsReady || tts == null) {
            onFinished?.invoke()
            return
        }

        // Clean markdown for natural spoken delivery
        val cleanedText = cleanMarkdownForSpeech(text)
        _voiceState.value = LiveVoiceState.SPEAKING
        _audioRmsLevel.value = 0.7f

        val utteranceId = "gemini_live_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        _voiceState.value = LiveVoiceState.IDLE
        _audioRmsLevel.value = 0.05f
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true

    fun cleanup() {
        stopListening()
        stopSpeaking()
        speechRecognizer?.destroy()
        speechRecognizer = null
        tts?.shutdown()
        tts = null
        isTtsReady = false
    }

    private fun cleanMarkdownForSpeech(raw: String): String {
        return raw
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("`+(.*?)`+"), "$1")
            .replace(Regex("#+\\s*"), "")
            .replace(Regex("•\\s*"), "")
            .replace(Regex("[-*+]\\s+"), "")
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            .replace(Regex("[\\r\\n]+"), ". ")
            .trim()
    }
}
