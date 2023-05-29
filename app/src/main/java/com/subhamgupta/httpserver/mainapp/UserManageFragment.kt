package com.subhamgupta.httpserver.mainapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.subhamgupta.httpserver.adapters.UserAdapter
import com.subhamgupta.httpserver.data.viewmodel.MainViewModel
import com.subhamgupta.httpserver.databinding.FragmentUserManageBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class UserManageFragment : Fragment() {
    private lateinit var binding: FragmentUserManageBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUserManageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.userRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewModel.fetchUsers()
        val adapter = UserAdapter()
        binding.userRecycler.adapter = adapter

        lifecycleScope.launchWhenStarted {
            viewModel.users.collect {
                adapter.setItems(it)
                Log.e("user manage","$it")
            }
        }
    }
}