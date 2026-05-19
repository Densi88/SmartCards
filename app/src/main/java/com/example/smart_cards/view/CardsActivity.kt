package com.example.smart_cards.view

import LearningStats
import WordProgress
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_cards.models.Card
import com.google.android.material.snackbar.Snackbar
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.os.Environment
import android.util.Log
import com.example.smart_cards.db.dbAdapter
import com.example.smart_cards.db.dbOpenHelper
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import com.example.smart_cards.R
import com.example.smart_cards.repository.CardsRepository
import com.example.smart_cards.viewModels.CardsViewModel
import okhttp3.OkHttpClient

class CardsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var exportButton: Button
    private lateinit var importButton: Button
    private lateinit var viewModel: CardsViewModel

    private var exportJob: Job? = null
    private var readStatistics: List<String>? = null
    private val client: OkHttpClient = OkHttpClient()

    private lateinit var statsButton: Button
    private lateinit var adapter: CardsAdapter
    private lateinit var dbHelper: dbOpenHelper
    private val cardList = mutableListOf<Card>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cards_activity_layout)

        initViews()

        // Инициализируем БД и ViewModel
        dbAdapter.init(applicationContext)
        val repository = CardsRepository(dbAdapter)
        viewModel = CardsViewModel(repository)

        setupRecyclerView()
        setupButton()
        setupSwipeToDelete()
        dbHelper = dbAdapter.getDbHelper()

        downloadCoro()
        readCoro()

        // Наблюдаем за списком карточек
        lifecycleScope.launch {
            viewModel.cards.collect { cards ->
                cardList.clear()
                cardList.addAll(cards)
                adapter.updateList(cards)
            }
        }

        // Наблюдаем за событиями
        lifecycleScope.launch {
            viewModel.event.collect { event ->
                when (event) {
                    is CardsViewModel.CardEvent.ShowMessage -> {
                        Toast.makeText(this@CardsActivity, event.message, Toast.LENGTH_SHORT).show()
                    }
                    is CardsViewModel.CardEvent.ShowError -> {
                        Toast.makeText(this@CardsActivity, event.message, Toast.LENGTH_LONG).show()
                    }
                    null -> {}
                }
                viewModel.consumeEvent()
            }
        }

        // Наблюдаем за загрузкой
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // Можно показать/скрыть прогресс-бар
                if (isLoading) {
                    // Показать прогресс
                } else {
                    // Скрыть прогресс
                }
            }
        }
    }

    private fun downloadCoro() {
        exportJob = GlobalScope.launch(Dispatchers.Main) {
            while (isActive) {
                Toast.makeText(this@CardsActivity, "Экспорт начат...", Toast.LENGTH_SHORT).show()

                val file = withContext(Dispatchers.IO) {
                    exportStatisticsCSV()
                }

                Toast.makeText(this@CardsActivity, "Экспорт завершён: ${file.name}", Toast.LENGTH_LONG).show()
                println("ЭКСПОРТ ВЫПОЛНЕН: ${System.currentTimeMillis()}")

                delay(30000)
            }
        }
    }

    private fun readCoro() {
        lifecycleScope.launch(Dispatchers.Main) {
            while (isActive) {
                val lines = withContext(Dispatchers.IO) {
                    val file = File(filesDir, "vocabulary_statistics.csv")
                    if (!file.exists()) return@withContext null
                    file.readLines()
                }

                readStatistics = lines

                if (lines == null) {
                    Toast.makeText(this@CardsActivity, "Файл не найден. Сначала экспортируйте статистику.", Toast.LENGTH_LONG).show()
                } else if (lines.isEmpty()) {
                    Toast.makeText(this@CardsActivity, "Файл пуст", Toast.LENGTH_LONG).show()
                } else {
                    println("ЧТЕНИЕ ВЫПОЛНЕНО: ${lines.size} строк")
                }

                delay(30000)
            }
        }
    }

    private fun showStatisticsDialog(lines: List<String>) {
        val message = buildString {
            appendLine("СТАТИСТИКА")
            appendLine("Всего строк: ${lines.size}")
            appendLine("")

            lines.take(15).forEachIndexed { index, line ->
                appendLine("${index + 1}. $line")
            }

            if (lines.size > 15) {
                appendLine("... и ещё ${lines.size - 15} строк")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("CSV файл")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        addButton = findViewById(R.id.Button)
        exportButton = findViewById<Button>(R.id.export_button)
        importButton = findViewById<Button>(R.id.import_button)
        statsButton = findViewById<Button>(R.id.show_statistics_button)
    }

    private fun setupRecyclerView() {
        adapter = CardsAdapter(
            cards = cardList,
            onItemClick = { card ->
                showEditDialog(card)
            },
            scope = lifecycleScope,
            client = client
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupButton() {
        addButton.text = "Добавить карточку"
        addButton.setOnClickListener {
            showAddDialog()
        }
        exportButton.setOnClickListener {
            exportStatisticsCSV()
            exportPDF()
            exportStatisticsXLS()
        }
        importButton.setOnClickListener {
            val text: String = readTextFile()
            importPdfVocabulary(text)
        }
        statsButton.setOnClickListener {
            if (readStatistics == null) {
                Toast.makeText(this, "Данные ещё не загружены. Подождите...", Toast.LENGTH_SHORT).show()
            } else if (readStatistics!!.isEmpty()) {
                Toast.makeText(this, "Нет данных для отображения", Toast.LENGTH_SHORT).show()
            } else {
                showStatisticsDialog(readStatistics!!)
            }
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val deletedCard = cardList[position]
                    viewModel.deleteCard(deletedCard.word)
                    showUndoSnackbar(deletedCard)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_card, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.etTitle)
        val descEditText = dialogView.findViewById<EditText>(R.id.etDescription)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Добавить карточку")
            .setView(dialogView)
            .setPositiveButton("Добавить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val title = titleEditText.text.toString()
                val description = descEditText.text.toString()

                if (title.isNotEmpty() && description.isNotEmpty()) {
                    viewModel.addCard(title, description)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun showEditDialog(card: Card) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_card, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.etTitle)
        val descEditText = dialogView.findViewById<EditText>(R.id.etDescription)

        titleEditText.setText(card.word)
        descEditText.setText(card.translate)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Редактировать карточку")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val newWord = titleEditText.text.toString()
                val newTranslate = descEditText.text.toString()

                if (newWord.isNotEmpty() && newTranslate.isNotEmpty()) {
                    viewModel.updateCard(card.word, newWord, newTranslate)
                } else {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun showUndoSnackbar(deletedCard: Card) {
        Snackbar.make(recyclerView, "Карточка удалена", Snackbar.LENGTH_LONG)
            .setAction("ОТМЕНА") {
                viewModel.loadCards()
            }
            .show()
    }

    private fun createTextFileForImport(pdfFile: File) {
        try {
            val textFile = File(pdfFile.parent, "my_vocabulary.txt")
            val content = StringBuilder()

            content.appendLine("Мой словарь иностранных слов")
            content.appendLine()

            cardList.forEach { card ->
                content.appendLine("${card.word} - ${card.translate}")
            }

            textFile.writeText(content.toString(), Charsets.UTF_8)
            Log.d("EXPORT", "Текстовый файл создан: ${textFile.absolutePath}")
            Log.d("EXPORT", "Содержимое: $content")

        } catch (e: Exception) {
            Log.e("EXPORT", "Ошибка создания текстового файла", e)
        }
    }

    private fun exportPDF(): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint()

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Мой словарь иностранных слов", 50f, 50f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        var yPos = 80f
        cardList.forEach { word ->
            canvas.drawText("${word.word} - ${word.translate}", 50f, yPos, paint)
            yPos += 20f
        }

        document.finishPage(page)

        val file = File(filesDir, "my_vocabulary.pdf")

        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()
        createTextFileForImport(file)

        Toast.makeText(this, "PDF создан: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        return file
    }

    fun generateRealisticData(): List<WordProgress> {
        return cardList.map { word ->
            WordProgress(
                word = word.word,
                translation = word.translate,
                correctCount = (0..20).random(),
                wrongCount = (0..5).random(),
                lastPracticed = Date(System.currentTimeMillis() - (0..7 * 24 * 3600 * 1000).random()),
                difficulty = (1..5).random()
            )
        }
    }

    fun generateLearningStats(): List<LearningStats> {
        return List(30) { i ->
            LearningStats(
                date = Date(System.currentTimeMillis() - (i * 24 * 3600 * 1000)),
                wordsLearned = (5..15).random(),
                correctAnswers = (20..40).random(),
                totalAttempts = (25..50).random(),
                sessionDuration = (10..30).random()
            )
        }
    }

    private fun exportStatisticsCSV(): File {
        val wordProgress = generateRealisticData()

        val file = File(filesDir, "vocabulary_statistics.csv")

        val csv = StringBuilder()

        csv.append("=== ПРОГРЕСС ПО СЛОВАМ ===\n")
        csv.append("Слово;Перевод;Правильно;Неправильно;Процент;Последняя практика\n")

        wordProgress.forEach { progress ->
            val percentage = (progress.correctCount.toDouble() / (progress.correctCount + progress.wrongCount)) * 100
            csv.append("${progress.word};${progress.translation};${progress.correctCount};${progress.wrongCount};${"%.1f".format(percentage)}%;${progress.lastPracticed}\n")
        }

        file.writeText(csv.toString())

        return file
    }

    private fun exportStatisticsXLS(): File {
        val learningStats = generateLearningStats()

        val workbook = XSSFWorkbook()
        val statsSheet = workbook.createSheet("Статистика обучения")
        var rowNum = 0

        var row = statsSheet.createRow(rowNum++)

        row = statsSheet.createRow(rowNum++)
        row.createCell(0).setCellValue("Дата")
        row.createCell(1).setCellValue("Изучено слов")
        row.createCell(2).setCellValue("Правильных ответов")
        row.createCell(3).setCellValue("Всего попыток")
        row.createCell(4).setCellValue("Длительность (мин)")

        learningStats.forEach { stats ->
            row = statsSheet.createRow(rowNum++)
            row.createCell(0).setCellValue(stats.date.toString())
            row.createCell(1).setCellValue(stats.wordsLearned.toDouble())
            row.createCell(2).setCellValue(stats.correctAnswers.toDouble())
            row.createCell(3).setCellValue(stats.totalAttempts.toDouble())
            row.createCell(4).setCellValue(stats.sessionDuration.toDouble())
        }

        val file = File(filesDir, "vocabulary_statistics.xlsx")

        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        return file
    }

    private fun importPdfVocabulary(text: String) {
        val lines = text.lines()
        var startParsing = false
        Log.d("DEBUG_IMPORT", "=== НАЧАЛО ИМПОРТА ===")
        Log.d("DEBUG_IMPORT", "Размер cardList ДО очистки: ${cardList.size}")

        // Очищаем через ViewModel
        cardList.forEach { card ->
            viewModel.deleteCard(card.word)
        }
        for (line in lines) {
            when {
                line.contains("Мой словарь иностранных слов") -> {
                    startParsing = true
                    continue
                }
                startParsing && line.contains(" - ") -> {
                    val parts = line.split(" - ", limit = 2)
                    if (parts.size == 2) {
                        val word = parts[0].trim()
                        val translate = parts[1].trim()
                        Log.d("DEBUG_IMPORT", "Найдена пара: '$word' - '$translate'")
                        viewModel.addCard(word, translate)
                    }
                }
            }
        }
    }

    private fun readTextFile(): String {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val textFile = File(downloadsDir, "my_vocabulary.txt")

            if (textFile.exists()) {
                val text = textFile.readText(Charsets.UTF_8)
                Log.d("READ_FILE", "Файл прочитан: ${text.length} символов")
                text
            } else {
                Log.d("READ_FILE", "Файл не найден")
                ""
            }
        } catch (e: Exception) {
            Log.e("READ_FILE", "Ошибка чтения файла", e)
            ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exportJob?.cancel()
    }
}