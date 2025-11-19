package com.example.smart_cards

import LearningStats
import WordProgress
import android.content.Intent
import android.graphics.fonts.Font
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOCUMENTS
import android.os.Environment.getExternalStoragePublicDirectory
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.font.FontFamily
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_cards.models.Card
import com.google.android.material.snackbar.Snackbar
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContentProviderCompat.requireContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.util.Date

class CardsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var exportButton: Button
    private lateinit var adapter: CardsAdapter
    private val cardList = mutableListOf<Card>()
    private var nextId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cards_activity_layout) // Убедись что layout называется activity_cards.xml

        initViews()
        setupRecyclerView()
        setupButton()
        setupSwipeToDelete()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        addButton = findViewById(R.id.Button)
        exportButton=findViewById<Button>(R.id.export_button)
    }

    private fun setupRecyclerView() {
        adapter = CardsAdapter(
            cards = cardList,
            onItemClick = { card ->
                val position = cardList.indexOf(card)
                showEditDialog(card, position)
            }
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
            exportPDF();
            exportStatisticsCSV()
            exportStatisticsXLS()
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
                    adapter.deleteCard(position)

                    showUndoSnackbar(deletedCard, position)
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
                    val newCard = Card(nextId++, title, description)
                    adapter.addCard(newCard)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun showEditDialog(card: Card, position: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_card, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.etTitle)
        val descEditText = dialogView.findViewById<EditText>(R.id.etDescription)

        titleEditText.setText(card.word)
        descEditText.setText(card.translate)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Редактировать карточку")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val newWord = titleEditText.text.toString()
                val newTranslate = descEditText.text.toString()

                if (newWord.isNotEmpty() && newTranslate.isNotEmpty()) {
                    val updatedCard = card.copy(word = newWord, translate = newTranslate)
                    adapter.updateCard(position, updatedCard)
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

    private fun showUndoSnackbar(deletedCard: Card, position: Int) {
        Snackbar.make(recyclerView, "Карточка удалена", Snackbar.LENGTH_LONG)
            .setAction("ОТМЕНА") {
                adapter.restoreCard(deletedCard, position)
            }
            .show()
    }


    private fun exportPDF(): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint()

        // Заголовок
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Мой словарь иностранных слов", 50f, 50f, paint)

        // Слова
        paint.textSize = 12f
        paint.isFakeBoldText = false
        var yPos = 80f
        cardList.forEach { word ->
            canvas.drawText("${word.word} - ${word.translate}", 50f, yPos, paint)
            yPos += 20f
        }

        document.finishPage(page)

        val file = File(
            getExternalFilesDir(null),
            "my_vocabulary.pdf"
        )

        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()


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
        // Генерируем реалистичные данные
        val wordProgress = generateRealisticData()

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "vocabulary_statistics.csv"
        )

        val csv = StringBuilder()

        // Заголовки для прогресса слов
        csv.append("=== ПРОГРЕСС ПО СЛОВАМ ===\n")
        csv.append("Слово;Перевод;Правильно;Неправильно;Процент;Последняя практика\n")

        wordProgress.forEach { progress ->
            val percentage = (progress.correctCount.toDouble() / (progress.correctCount + progress.wrongCount)) * 100
            csv.append("${progress.word};${progress.translation};${progress.correctCount};${progress.wrongCount};${"%.1f".format(percentage)}%;${progress.lastPracticed}\n")
        }
        return file
    }
    private fun exportStatisticsXLS():File{
        val learningStats = generateLearningStats()

        val workbook = XSSFWorkbook()
        val statsSheet = workbook.createSheet("Статистика обучения")
        var rowNum = 0

        var row = statsSheet.createRow(rowNum++)

        // Заголовки
        row = statsSheet.createRow(rowNum++)
        row.createCell(0).setCellValue("Дата")
        row.createCell(1).setCellValue("Изучено слов")
        row.createCell(2).setCellValue("Правильных ответов")
        row.createCell(3).setCellValue("Всего попыток")
        row.createCell(4).setCellValue("Длительность (мин)")

        // Данные
        learningStats.forEach { stats ->
            row = statsSheet.createRow(rowNum++)
            row.createCell(0).setCellValue(stats.date.toString())
            row.createCell(1).setCellValue(stats.wordsLearned.toDouble())
            row.createCell(2).setCellValue(stats.correctAnswers.toDouble())
            row.createCell(3).setCellValue(stats.totalAttempts.toDouble())
            row.createCell(4).setCellValue(stats.sessionDuration.toDouble())
        }

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "learning_statistics.xlsx"
        )

        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()

        Toast.makeText(this, "XLS сохранен!", Toast.LENGTH_LONG).show()
        return file

    }
}