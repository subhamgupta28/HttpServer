package com.subhamgupta.httpserver.mainapp

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.subhamgupta.httpserver.R
import com.subhamgupta.httpserver.data.viewmodel.MainViewModel
import com.subhamgupta.httpserver.databinding.ActivityBaseBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BaseActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let {
            if (it.getBoolean("addUser")) {
                binding.toolbar.title = "Create New User"
                onShowUserAdd()
            }
            if (it.getBoolean("manageUser")) {
                binding.toolbar.title = "Manage User"
                onShowUserManage()
            }
            if (it.getBoolean("upload")) {
                binding.toolbar.title = "Received Files"
                onShowUpload()
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun onShowUpload() {
        val fragment = UploadFragment()
        binding.settingFragment.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .add(R.id.setting_fragment, fragment)
            .commit()
    }

    private fun onShowUserManage(){
        val fragment = UserManageFragment()
        binding.settingFragment.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .add(R.id.setting_fragment, fragment)
            .commit()
    }
    private fun onShowUserAdd(){
        val fragment = AddUserFragment()
        binding.settingFragment.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .add(R.id.setting_fragment, fragment)
            .commit()
    }
}