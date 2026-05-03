package com.example.smart_cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_cards.models.Card
import kotlinx.coroutines.*
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder

class CardsAdapter(
    private var cards: List<Card>,
    private val onItemClick: (Card) -> Unit,
    private val scope: CoroutineScope,
    private val client: OkHttpClient
) : RecyclerView.Adapter<CardsAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val word: TextView = itemView.findViewById(R.id.tvWord)
        val translate: TextView = itemView.findViewById(R.id.tvTranslate)
        val translateButton: Button = itemView.findViewById(R.id.translate_button)
        val dictionaryButton: Button = itemView.findViewById(R.id.dictionary_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    fun updateList(newCards: List<Card>) {
        cards = newCards
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]

        holder.word.text = card.word
        holder.translate.text = card.translate

        holder.itemView.setOnClickListener {
            onItemClick(card)
        }

        holder.translateButton.setOnClickListener {
            translate(card.word, holder) // Передаем holder для обновления UI
        }
        holder.dictionaryButton.setOnClickListener {
            getDictionary(card.word, holder)
        }
    }

    override fun getItemCount(): Int = cards.size
    private suspend fun makeTranslate(word: String): String {
        return withContext(Dispatchers.IO) {
            // Экранируем слово для URL
            val encodedWord = URLEncoder.encode(word, "UTF-8")
            val url = "https://api.mymemory.translated.net/get?q=$encodedWord&langpair=en|ru"

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code ${response.code}")
                }

                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody)

                // Парсим ответ MyMemory
                // Структура ответа: { "responseData": { "translatedText": "..." } }
                val responseData = jsonResponse.optJSONObject("responseData")
                var translatedText = responseData?.optString("translatedText") ?: ""

                // MyMemory может возвращать HTML сущности, декодируем их
                translatedText = translatedText
                    .replace("&#39;", "'")
                    .replace("&quot;", "\"")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")

                translatedText.takeIf { it.isNotEmpty() && it != word }
                    ?: "Перевод не найден"
            }
        }
    }
    fun translate(word: String, holder: CardViewHolder) {
        scope.launch(Dispatchers.Main) {
            try {
                Toast.makeText(
                    holder.itemView.context,
                    "Перевожу '$word'...",
                    Toast.LENGTH_SHORT
                ).show()

                // Выполняем перевод
                val translation = makeTranslate(word)
                Toast.makeText(
                    holder.itemView.context,
                    "Перевод: $translation",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                holder.translate.text = "Ошибка"
                Toast.makeText(
                    holder.itemView.context,
                    "Ошибка: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                println("Ошибка ${e.message}")
            }

        }
    }

    private suspend fun makeDictionary(word: String): String {
        return withContext(Dispatchers.IO) {
            val translation=makeTranslate(word)
            val url = "https://ru.wiktionary.org/w/api.php?" +
                    "action=query" +
                    "&prop=extracts" +
                    "&explaintext=true" +
                    "&titles=${URLEncoder.encode(translation, "UTF-8")}" +
                    "&format=json"

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "SmartCards/1.0 (Android; smartcards@example.com)")  // ✅ Добавили
                .build()


            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code ${response.code}")
                }

                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody)

                val query = jsonResponse.getJSONObject("query")
                val pages = query.getJSONObject("pages")

                // Получаем первый (и единственный) page
                val pageId = pages.keys().next()
                val page = pages.getJSONObject(pageId)

                // Проверяем, существует ли страница
                if (page.has("missing")) {
                    return@withContext "Определение для слова '${word}' не найдено"
                }

                val extract = page.optString("extract")

                if (extract.isNullOrEmpty()) {
                    return@withContext "Определение не найдено"
                }

                return@withContext extract
            }
        }
    }
    fun getDictionary(word: String, holder: CardViewHolder) {
        scope.launch(Dispatchers.Main) {
            try {
                Toast.makeText(
                    holder.itemView.context,
                    "Ищу определение '$word'...",
                    Toast.LENGTH_SHORT
                ).show()

                val definition = makeDictionary(word)
                showDefinitionDialog(holder.itemView.context, word, definition)
            } catch (e: Exception) {
                Toast.makeText(
                    holder.itemView.context,
                    "Ошибка: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showDefinitionDialog(context: android.content.Context, word: String, definition: String) {
        val scrollView = android.widget.ScrollView(context)
        val textView = android.widget.TextView(context).apply {
            setText(definition)
            setPadding(48, 32, 48, 32)
            textSize = 14f
            setTextColor(android.graphics.Color.BLACK)
        }
        scrollView.addView(textView)

        val dialog = android.app.AlertDialog.Builder(context)
            .setTitle("Определение: $word")
            .setView(scrollView)
            .setPositiveButton("Закрыть") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Копировать") { dialog, _ ->
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("definition", definition)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Определение скопировано", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
}