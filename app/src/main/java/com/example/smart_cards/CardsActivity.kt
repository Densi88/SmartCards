package com.example.smart_cards

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

class CardsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: Button
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
}