package com.example.smart_cards

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_cards.databinding.LearnFragmentLayoutBinding

class LearnFragment: Fragment() {
    private val adapter= LevelAdapter()
    lateinit var binding: LearnFragmentLayoutBinding
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
        recyclerView=view.findViewById<RecyclerView>(R.id.levels)
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
        adapter.generateLevels()
    }


}