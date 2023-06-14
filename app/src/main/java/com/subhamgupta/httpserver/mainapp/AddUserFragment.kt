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
import com.subhamgupta.httpserver.adapters.AccessItemAdapter
import com.subhamgupta.httpserver.data.viewmodel.MainViewModel
import com.subhamgupta.httpserver.databinding.FragmentAddUserBinding
import com.subhamgupta.httpserver.domain.model.FolderObj
import com.subhamgupta.httpserver.domain.objects.getFolders
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddUserFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentAddUserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddUserBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addUser()
    }

    private fun addUser(){
        val selectedFolder = mutableSetOf<FolderObj>()
        binding.folderRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = AccessItemAdapter(selectedFolder)
        Log.e("session add", "")

        binding.folderRecycler.adapter = adapter
        lifecycleScope.launchWhenStarted {
            adapter.setFolderList(getFolders())
        }

        binding.createProfile.setOnClickListener {
            val username = binding.username.text
            val password = binding.password.text
            if (!username.isNullOrEmpty() || !password.isNullOrEmpty()) {
                Log.e("folders", "$selectedFolder")

            }
        }
    }

}