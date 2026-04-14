package com.example.smart_cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.layout.Layout
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_cards.databinding.LevelItemBinding
import com.example.smart_cards.db.dbAdapter
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

    fun updateLevels(newLevels: List<Level>) {
        levelList.clear()
        levelList.addAll(newLevels)
        notifyDataSetChanged()
    }

    class LevelsHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding= LevelItemBinding.bind(item)
        fun bind(level:Level)=with(binding){
            levelButton.text=level.number.toString()
        }

    }

    fun generateLevels(){
        val dbHelper=dbAdapter.getDbHelper()
        val db=dbHelper.readableDatabase
        val query="select number from levels"
        val cursor=db.rawQuery(query, null)
        while(cursor.moveToNext()){
            val number=cursor.getString(cursor.getColumnIndexOrThrow("number"))
            val level= Level(number)
            levelList.add(level)
        }
        notifyDataSetChanged()
    }
}