package com.example.smart_cards.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_cards.R
import com.example.smart_cards.databinding.LearnFragmentLayoutBinding
import com.example.smart_cards.models.Level
import com.example.smart_cards.threads.ThreadHandler

class LearnFragment: Fragment() {
    private val adapter= LevelAdapter()

    lateinit var binding: LearnFragmentLayoutBinding
    private lateinit var threadHandler: ThreadHandler
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.learn_fragment_layout, container, false )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        threadHandler = ThreadHandler { levels ->
            requireActivity().runOnUiThread {
                val levelObjects = levels.mapIndexed { index, cards ->
                    Level(number = (index + 1).toString())
                }
                adapter.updateLevels(levelObjects)
                println("UI обновлён: ${levelObjects.size} уровней")
            }
        }
        recyclerView=view.findViewById<RecyclerView>(R.id.levels)
        threadHandler.start()
        init()
    }

    companion object {
        val instance: LearnFragment by lazy {
            LearnFragment()
        }
    }
    private fun init()  {
        recyclerView.layoutManager= LinearLayoutManager(requireContext())
        recyclerView.adapter=adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        threadHandler.stop()
    }


}