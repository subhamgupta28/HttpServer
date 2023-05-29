package com.subhamgupta.httpserver.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.subhamgupta.httpserver.R
import com.subhamgupta.httpserver.data.viewmodel.MainViewModel
import com.subhamgupta.httpserver.databinding.FragmentSignUpBinding
import com.subhamgupta.httpserver.mainapp.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_signUpFragment_to_authFragment)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        lifecycleScope.launchWhenStarted {
            viewModel.userState.collect{
                Log.e("user", "$it")
                if (it["isRegistered"] == true){
                    findNavController().navigate(R.id.action_signUpFragment_to_authFragment)
                }
            }
        }
        binding.createAcc.setOnClickListener {
            val username = binding.name.text
            val password = binding.pass.text
            val email = binding.email.text
            Log.e("user", "$username $email $password")
            if (username.isNullOrEmpty() || email.isNullOrEmpty() || password.isNullOrEmpty()){

            }else{

                viewModel.registerUser(username.toString(), email.toString(), password.toString())
            }
        }
    }

}