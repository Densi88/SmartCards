import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_cards.R

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private val newsItems = listOf(
        NewsItem("Первая новость", "Это содержимое первой новости...", "2 часа назад"),
        NewsItem("Вторая новость", "Это содержимое второй новости...", "5 часов назад"),
        NewsItem("Третья новость", "Это содержимое третьей новости...", "1 день назад"),
        NewsItem("Четвертая новость", "Это содержимое четвертой новости...", "2 дня назад"),
        NewsItem("Пятая новость", "Это содержимое пятой новости...", "3 дня назад")
    )

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.postTitle)
        val content: TextView = itemView.findViewById(R.id.postContent)
        val date: TextView = itemView.findViewById(R.id.postDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_layout, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = newsItems[position]
        holder.title.text = item.title
        holder.content.text = item.content
        holder.date.text = item.date
    }

    override fun getItemCount() = newsItems.size

    data class NewsItem(
        val title: String,
        val content: String,
        val date: String
    )
}