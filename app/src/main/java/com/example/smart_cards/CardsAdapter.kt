package com.example.smart_cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_cards.models.Card

class CardsAdapter(
    private var cards: MutableList<Card>,
    private val onItemClick: (Card) -> Unit,
) : RecyclerView.Adapter<CardsAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val word: TextView = itemView.findViewById(R.id.tvWord)
        val translate: TextView = itemView.findViewById(R.id.tvTranslate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]

        holder.word.text = card.word
        holder.translate.text = card.translate


        holder.itemView.setOnClickListener {
            onItemClick(card)
        }
    }

    override fun getItemCount(): Int = cards.size

    // CRUD методы
    fun addCard(card: Card) {
        cards.add(0, card)
        notifyItemInserted(0)
    }

    fun updateCard(position: Int, card: Card) {
        cards[position] = card
        notifyItemChanged(position)
    }

    fun deleteCard(position: Int) {
        cards.removeAt(position)
        notifyItemRemoved(position)
    }

    fun restoreCard(card: Card, position: Int) {
        cards.add(position, card)
        notifyItemInserted(position)
    }
}