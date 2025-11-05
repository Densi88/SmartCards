package com.example.smart_cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.widget.TextView

class ProfileFragment: Fragment() {
    private val userViewModel: UserModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.username).text = userViewModel.userName
        view.findViewById<TextView>(R.id.levelText).text = "Уровень ${userViewModel.userLevel}"
        view.findViewById<TextView>(R.id.streakText).text = userViewModel.streakDays.toString()
        view.findViewById<TextView>(R.id.experienceText).text = userViewModel.experience.toString()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_fragment_layout, container, false)
    }

    companion object {
        val instance: ProfileFragment by lazy {
            ProfileFragment()
        }
    }
}