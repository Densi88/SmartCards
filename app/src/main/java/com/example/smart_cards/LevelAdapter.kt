package com.example.smart_cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.layout.Layout
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_cards.databinding.LevelItemBinding
import com.example.smart_cards.models.Level

class LevelAdapter: RecyclerView.Adapter<LevelAdapter.LevelsHolder>() {
    val levelList= ArrayList<Level>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LevelsHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.level_item, parent, false)
        return LevelsHolder(view)
    }

    override fun onBindViewHolder(
        holder: LevelsHolder,
        position: Int
    ) {
        holder.bind(levelList[position])
    }

    override fun getItemCount(): Int {
       return levelList.size
    }

    class LevelsHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding= LevelItemBinding.bind(item)
        fun bind(level:Level)=with(binding){
            levelButton.text=level.number.toString()
            levelTopic.text=level.title
        }

    }

    fun generateLevels(){
        for (i in 0 until 5){
            val level= Level(i, "Заглушка")
            levelList.add(level)
        }
        notifyDataSetChanged()
    }
}