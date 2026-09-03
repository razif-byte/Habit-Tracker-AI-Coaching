package com.example.data.remote

import com.example.BuildConfig
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.remote.openai.OpenAiApiService
import com.example.data.remote.openai.OpenAiChatRequest
import com.example.data.remote.openai.OpenAiMessage
import com.example.data.remote.openai.OpenAiRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AICoachingRepository(
    private val geminiApiService: GeminiApiService = RetrofitClient.geminiService,
    private val openAiApiService: OpenAiApiService = OpenAiRetrofitClient.openAiService
) {

    private val geminiSystemPrompt = """
        You are Coach Zenith, an elite behavioral scientist, habit architect, and empathetic AI coach powered by Google Gemini.
        You specialize in BJ Fogg's Tiny Habits, James Clear's Atomic Habits, and neuroplasticity-based habit retention.
        Your tone is encouraging, razor-sharp, practical, and highly actionable.
        You can communicate fluently in Bahasa Melayu and English.
        Always provide structured, clear advice (using bullet points and bold headers when helpful).
        When the user mentions feeling lazy or losing a streak, never shame them; provide a 2-Minute Rule reset.
        Keep responses concise (150-250 words) so they are easy to read on mobile.
    """.trimIndent()

    private val chatGptSystemPrompt = """
        You are Coach Zenith, an elite productivity and life architect powered by OpenAI ChatGPT (GPT-4o).
        Your expertise includes habit loops, behavioral economics, deep work scheduling, and holistic personal wellness.
        Provide structured, high-clarity, empathetic coaching with immediate next steps.
        You can communicate fluently in Bahasa Melayu and English.
        Use clear headers, numbered actionable steps, and motivational anchoring.
        Keep responses around 150-250 words.
    """.trimIndent()

    private val geminiLiveSystemPrompt = """
        You are Coach Zenith in Gemini Live real-time audio voice session.
        Speak directly and warmly as if speaking in a real live voice phone call.
        Keep answers short, punchy, conversational, and energetic (40 to 80 words max).
        DO NOT use any markdown asterisks, bold tags, hashes, or bullet symbols, because your answer will be synthesized directly into spoken voice through Text-to-Speech!
        Answer in the language the user used (Bahasa Melayu or English).
    """.trimIndent()

    suspend fun getCoachChatResponse(
        userMessage: String,
        recentHistory: List<ChatMessageEntity>,
        activeHabits: List<HabitEntity>,
        userGoal: String,
        engine: AIEngineType = AIEngineType.GEMINI_FLASH,
        customGeminiKey: String? = null,
        customOpenAiKey: String? = null
    ): String = withContext(Dispatchers.IO) {
        when (engine) {
            AIEngineType.GEMINI_FLASH -> {
                callGeminiChat(
                    userMessage = userMessage,
                    recentHistory = recentHistory,
                    activeHabits = activeHabits,
                    userGoal = userGoal,
                    customKey = customGeminiKey,
                    isLiveVoice = false
                )
            }
            AIEngineType.CHATGPT_4O -> {
                callOpenAiChat(
                    userMessage = userMessage,
                    recentHistory = recentHistory,
                    activeHabits = activeHabits,
                    userGoal = userGoal,
                    model = "gpt-4o",
                    customKey = customOpenAiKey
                )
            }
            AIEngineType.CHATGPT_MINI -> {
                callOpenAiChat(
                    userMessage = userMessage,
                    recentHistory = recentHistory,
                    activeHabits = activeHabits,
                    userGoal = userGoal,
                    model = "gpt-4o-mini",
                    customKey = customOpenAiKey
                )
            }
            AIEngineType.GEMINI_LIVE -> {
                callGeminiChat(
                    userMessage = userMessage,
                    recentHistory = recentHistory,
                    activeHabits = activeHabits,
                    userGoal = userGoal,
                    customKey = customGeminiKey,
                    isLiveVoice = true
                )
            }
        }
    }

    private suspend fun callGeminiChat(
        userMessage: String,
        recentHistory: List<ChatMessageEntity>,
        activeHabits: List<HabitEntity>,
        userGoal: String,
        customKey: String?,
        isLiveVoice: Boolean
    ): String {
        val rawKey = customKey?.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY
        val hasValidKey = !rawKey.isNullOrBlank() && rawKey != "your_api_key_here" && rawKey != "MY_GEMINI_API_KEY"

        if (!hasValidKey) {
            return if (isLiveVoice) {
                generateOfflineLiveVoiceReply(userMessage, activeHabits)
            } else {
                generateOfflineGeminiReply(userMessage, activeHabits, userGoal)
            }
        }

        return try {
            val habitSummary = if (activeHabits.isNotEmpty()) {
                "User's Active Habits:\n" + activeHabits.joinToString("\n") {
                    "- ${it.title} (${it.category}, Current Streak: ${it.currentStreak} days, Target: ${it.targetCount} ${it.unit})"
                }
            } else {
                "User has no active habits yet."
            }

            val systemInstr = if (isLiveVoice) geminiLiveSystemPrompt else geminiSystemPrompt
            val contextNote = if (isLiveVoice) {
                "User Habits: ${activeHabits.joinToString { it.title }}. Question: $userMessage"
            } else {
                "User Primary Goal: $userGoal\n$habitSummary\n\nUser Question: $userMessage"
            }

            val contents = mutableListOf<GeminiContent>()
            val historySlice = recentHistory.takeLast(4)
            for (msg in historySlice) {
                val role = if (msg.sender == "user") "user" else "model"
                contents.add(GeminiContent(parts = listOf(GeminiPart(text = msg.message)), role = role))
            }
            contents.add(GeminiContent(parts = listOf(GeminiPart(text = contextNote)), role = "user"))

            val request = GeminiRequest(
                contents = contents,
                generationConfig = GeminiGenerationConfig(
                    temperature = if (isLiveVoice) 0.6f else 0.7f,
                    maxOutputTokens = if (isLiveVoice) 250 else 800
                ),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstr)))
            )

            val response = geminiApiService.generateContent(rawKey, request)
            val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!candidateText.isNullOrBlank()) {
                candidateText.trim()
            } else {
                if (isLiveVoice) generateOfflineLiveVoiceReply(userMessage, activeHabits) else generateOfflineGeminiReply(userMessage, activeHabits, userGoal)
            }
        } catch (e: Exception) {
            if (isLiveVoice) generateOfflineLiveVoiceReply(userMessage, activeHabits) else generateOfflineGeminiReply(userMessage, activeHabits, userGoal)
        }
    }

    private suspend fun callOpenAiChat(
        userMessage: String,
        recentHistory: List<ChatMessageEntity>,
        activeHabits: List<HabitEntity>,
        userGoal: String,
        model: String,
        customKey: String?
    ): String {
        val rawKey = customKey?.takeIf { it.isNotBlank() } ?: runCatching { BuildConfig.OPENAI_API_KEY }.getOrNull()
        val hasValidKey = !rawKey.isNullOrBlank() && rawKey != "your_openai_api_key_here" && rawKey != "your_api_key_here"

        if (!hasValidKey) {
            return generateOfflineChatGptReply(userMessage, activeHabits, userGoal, model)
        }

        return try {
            val habitSummary = if (activeHabits.isNotEmpty()) {
                "User's Active Habits:\n" + activeHabits.joinToString("\n") {
                    "- ${it.title} (${it.category}, Current Streak: ${it.currentStreak} days)"
                }
            } else {
                "User has no active habits yet."
            }

            val messages = mutableListOf<OpenAiMessage>()
            messages.add(OpenAiMessage(role = "system", content = chatGptSystemPrompt))

            // Add past turns
            val historySlice = recentHistory.takeLast(4)
            for (msg in historySlice) {
                val role = if (msg.sender == "user") "user" else "assistant"
                messages.add(OpenAiMessage(role = role, content = msg.message))
            }

            val userPromptWithContext = "Goal: $userGoal\n$habitSummary\n\nQuestion: $userMessage"
            messages.add(OpenAiMessage(role = "user", content = userPromptWithContext))

            val request = OpenAiChatRequest(
                model = model,
                messages = messages,
                temperature = 0.7,
                maxTokens = 800
            )

            val response = openAiApiService.createChatCompletion("Bearer $rawKey", request)
            val reply = response.choices?.firstOrNull()?.message?.content
            if (!reply.isNullOrBlank()) {
                reply.trim()
            } else {
                generateOfflineChatGptReply(userMessage, activeHabits, userGoal, model)
            }
        } catch (e: Exception) {
            generateOfflineChatGptReply(userMessage, activeHabits, userGoal, model)
        }
    }

    suspend fun generateDeepHabitAnalysis(
        activeHabits: List<HabitEntity>,
        totalCompletions: Int,
        userName: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            Analyze the following habit routine for user '$userName':
            Total all-time completions: $totalCompletions
            Habits:
            ${activeHabits.joinToString("\n") { "- ${it.title} [${it.category}]: ${it.currentStreak} day streak (Best: ${it.bestStreak})" }}
            
            Provide a 3-part breakdown:
            1. **Strength Pattern**: What's working well.
            2. **Friction Risk**: Potential bottleneck or missed consistency area.
            3. **Habit Stacking Recommendation**: How to anchor these habits together in an unbroken chain (e.g. After X, I will do Y).
        """.trimIndent()

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "your_api_key_here") {
            return@withContext generateOfflineAnalysis(activeHabits, totalCompletions, userName)
        }

        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user")),
                generationConfig = GeminiGenerationConfig(temperature = 0.6f, maxOutputTokens = 600),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = geminiSystemPrompt)))
            )
            val response = geminiApiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: generateOfflineAnalysis(activeHabits, totalCompletions, userName)
        } catch (e: Exception) {
            generateOfflineAnalysis(activeHabits, totalCompletions, userName)
        }
    }

    suspend fun generateGoalBreakdown(goal: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            The user wants to achieve this goal: "$goal".
            Break this down into 3 concrete, micro-habits using the 2-Minute Rule.
            Format clearly with:
            - Habit Name
            - Recommended Frequency (e.g. Daily, 3x/week)
            - Micro-Starter version (first 2 minutes)
            - How to level it up once consistent
        """.trimIndent()

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "your_api_key_here") {
            return@withContext generateOfflineGoalBreakdown(goal)
        }

        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user")),
                generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 600),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = geminiSystemPrompt)))
            )
            val response = geminiApiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: generateOfflineGoalBreakdown(goal)
        } catch (e: Exception) {
            generateOfflineGoalBreakdown(goal)
        }
    }

    // High-fidelity fallback replies for Gemini
    private fun generateOfflineGeminiReply(
        query: String,
        activeHabits: List<HabitEntity>,
        userGoal: String
    ): String {
        val lower = query.lowercase()
        return when {
            lower.contains("procrastinat") || lower.contains("lazy") || lower.contains("malas") || lower.contains("hard") || lower.contains("unmotivated") -> {
                "💡 **Protokol Reset 2 Minit (Gemini 3.5 Flash)**\n\nApabila motivasi merudum, korteks prefrontal anda mengalami geseran keputusan. Kecilkan tabiat kepada saiz yang mustahil untuk gagal:\n\n• Jika senaman terasa berat, hanya sarungkan kasut sukan dan lakukan 5 kali push-up.\n• Jika membaca buku terasa malas, buka buku dan baca 1 muka surat sahaja.\n\n*Prinsip utama:* Tabiat 2 minit yang konsisten jauh lebih berkuasa daripada sesi 60 minit yang hanya dibuat sebulan sekali."
            }
            lower.contains("streak") || lower.contains("lost") || lower.contains("terlepas") || lower.contains("putus") -> {
                "🔥 **Peraturan Jangan Terlepas Dua Kali (Never-Miss-Twice)**\n\nTerlepas satu hari hanyalah kemalangan; tetapi terlepas dua hari berturut-turut adalah permulaan tabiat negatif baharu.\n\nHari ini, lakukan versi paling minimum untuk tabiat anda sebelum tidur. Mempertahankan momentum hari ini mengekalkan identiti anda sebagai insan yang berdisiplin!"
            }
            lower.contains("stack") || lower.contains("routine") || lower.contains("rutin") || lower.contains("pagi") -> {
                val firstHabit = activeHabits.firstOrNull()?.title ?: "Minum Air Pagi"
                val secondHabit = activeHabits.getOrNull(1)?.title ?: "Senaman Ringan"
                "🔗 **Formula Habit Stacking BJ Fogg**\n\nPautkan tabiat baharu pada rutin automatik sedia ada:\n\n1. **Pencetus (Trigger):** Sebaik sahaja saya selesai `$firstHabit`...\n2. **Tindakan (Action):** Saya akan segera melakukan `$secondHabit`.\n3. **Ganjaran (Reward):** Beri tepukan pada bahu sendiri atau senyuman kejayaan!\n\nIni memanfaatkan laluan neural sedia ada untuk meringankan beban mental anda."
            }
            else -> {
                "🎯 **Strategi Konsistensi Matlamat: '$userGoal'**\n\n1. **Reka Bentuk Persekitaran:** Jadikan petunjuk tabiat baik jelas di depan mata dan hapuskan halangan awal (cth. sediakan botol air malam sebelumnya).\n2. **Jejak Tanpa Menghukum Diri:** Fokus kepada hadir setiap hari, walau hanya untuk 60 saat.\n3. **Transformasi Identiti:** Setiap kali anda menanda siap, ia adalah undian untuk versi diri masa depan anda yang lebih hebat!"
            }
        }
    }

    // High-fidelity fallback replies for ChatGPT (OpenAI)
    private fun generateOfflineChatGptReply(
        query: String,
        activeHabits: List<HabitEntity>,
        userGoal: String,
        model: String
    ): String {
        val lower = query.lowercase()
        val modelTag = if (model.contains("mini")) "GPT-4o Mini" else "GPT-4o"
        return when {
            lower.contains("malas") || lower.contains("lazy") || lower.contains("procrastinat") || lower.contains("fokus") -> {
                "🧠 **Analisis ChatGPT ($modelTag): Mengatasi Halangan Dopamin**\n\nKeengganan bertindak bukanlah tanda kelemahan diri, tetapi ketidakseimbangan sistem dopamin dan rintangan tenaga awal (activation energy).\n\n**Pelan Tindakan 3 Langkah:**\n1. **Teknik 5 Saat:** Kira undur 5-4-3-2-1 dan terus gerakkan fizikal anda ke ruang aktiviti.\n2. **Pecahan Mikro:** Jangan fikirkan keseluruhan matlamat. Mulakan fasa 'Micro-Commitment' selama 120 saat sahaja.\n3. **Hapuskan Pilihan:** Tutup tab pelayar yang tidak relevan dan letakkan telefon dalam mod fokus."
            }
            lower.contains("streak") || lower.contains("putus") || lower.contains("gagal") -> {
                "📈 **Strategi Pemulihan Momentum ($modelTag)**\n\nKajian sains tingkah laku menunjukkan orang yang paling berjaya bukanlah mereka yang tidak pernah gagal, tetapi mereka yang pulih paling cepat daripada kegagalan.\n\n• **Audit Punca:** Adakah anda letih, kekurangan tidur, atau menetapkan sasaran terlalu tinggi?\n• **Reset Fleksibel:** Lakukan versi kecemasan (emergency minimum) bagi tabiat anda hari ini untuk memulihkan rantaian psikologi."
            }
            else -> {
                "⚡ **Bimbingan Strategik ChatGPT ($modelTag)**\n\nBagi matlamat: *\"$userGoal\"*:\n\n1. **Penetapan Sasaran Jelas:** Tentukan masa dan lokasi tepat bila setiap tabiat akan dilaksanakan.\n2. **Pengurangan Beban Kognitif:** Gunakan senarai semak harian dalam aplikasi ini untuk memantau kemajuan tanpa tekanan mental.\n3. **Peningkatan Bertahap 1%:** Fokus pada penambahbaikan kecil yang berterusan (Kaizen) setiap minggu."
            }
        }
    }

    // Spoken-friendly voice reply for Gemini Live
    private fun generateOfflineLiveVoiceReply(
        query: String,
        activeHabits: List<HabitEntity>
    ): String {
        val lower = query.lowercase()
        return when {
            lower.contains("semak") || lower.contains("status") || lower.contains("hari ini") -> {
                val total = activeHabits.size
                "Hebat Alex! Anda mempunyai $total tabiat aktif dalam senarai hari ini. Teruskan momentum dan selesaikan tabiat seterusnya sekarang!"
            }
            lower.contains("malas") || lower.contains("semangat") || lower.contains("motivasi") -> {
                "Ingat, anda tidak perlukan motivasi penuh untuk bermula. Cuma mulakan selama dua minit sahaja sekarang. Selepas dua minit, momentum akan mengambil alih secara automatik!"
            }
            lower.contains("siapa") || lower.contains("nama") -> {
                "Saya Coach Zenith dalam mod Gemini Live. Saya berada di sini untuk berbual secara langsung dan membantu anda mencapai disiplin diri yang luar biasa."
            }
            else -> {
                "Saya dengar apa yang anda katakan. Kunci konsistensi adalah melakukan langkah kecil setiap hari tanpa henti. Apa tabiat yang anda ingin selesaikan dalam masa lima minit ini?"
            }
        }
    }

    private fun generateOfflineAnalysis(
        activeHabits: List<HabitEntity>,
        totalCompletions: Int,
        userName: String
    ): String {
        val avgStreak = if (activeHabits.isNotEmpty()) activeHabits.map { it.currentStreak }.average().toInt() else 0
        return """
            📊 **Analisis Tingkah Laku AI untuk $userName**
            
            • **Corak Kekuatan:** Anda telah membina momentum yang mantap dengan purata streak aktif sebanyak $avgStreak hari merentasi tabiat semasa. Jumlah penyelesaian seumur hidup: $totalCompletions!
            
            • **Risiko Geseran:** Tabiat waktu malam cenderung mempunyai kadar keciciran yang lebih tinggi disebabkan kehabisan tenaga keazaman (willpower depletion). Pastikan peringatan ditetapkan sekurang-kurangnya 1 jam sebelum anda berehat.
            
            • **Pelan Habit Stacking:**
              1. **Rutin Pagi:** Bangun ➔ Hidrat (Minum 2L Air) ➔ 10-min Senaman Fizikal.
              2. **Rutin Malam:** Malapkan lampu ➔ 15 Muka Surat Membaca ➔ Semakan Pantas Tabiat.
        """.trimIndent()
    }

    private fun generateOfflineGoalBreakdown(goal: String): String {
        return """
            🚀 **Pelan Tabiat Atomik untuk: "$goal"**
            
            1. **Tabiat 1: Pencetus Mikro (Setiap Hari)**
               • *Permulaan mikro:* Luangkan tepat 2 minit memulakan tindakan teras setiap pagi.
               • *Peningkatan:* Kembangkan kepada 15-20 minit sebaik sahaja anda mencapai 7 hari streak tanpa putus.
               
            2. **Tabiat 2: Persediaan Persekitaran (Setiap Hari - Waktu Malam)**
               • *Permulaan mikro:* Sediakan bahan atau peralatan yang diperlukan malam sebelumnya.
               • *Peningkatan:* Cipta zon ruang kerja khas yang bebas daripada sebarang gangguan.
               
            3. **Tabiat 3: Semakan Refleksi (Mingguan)**
               • *Permulaan mikro:* Semak skor konsistensi mingguan anda setiap hari Ahad selama 3 minit.
        """.trimIndent()
    }
}
