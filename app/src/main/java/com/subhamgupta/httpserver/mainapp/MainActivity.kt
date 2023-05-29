package com.subhamgupta.httpserver.mainapp

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.subhamgupta.httpserver.MyNotificationListener
import com.subhamgupta.httpserver.R
import com.subhamgupta.httpserver.data.viewmodel.MainViewModel
import com.subhamgupta.httpserver.databinding.ActivityMainBinding
import com.subhamgupta.httpserver.domain.model.NotificationObj
import com.subhamgupta.httpserver.utils.AuthResponse
import com.subhamgupta.httpserver.utils.AuthStatus
import com.subhamgupta.httpserver.utils.ConfirmToAcceptLoginEvent
import com.subhamgupta.httpserver.utils.JsonHelper
import com.subhamgupta.httpserver.utils.NetworkScanner
import com.subhamgupta.httpserver.utils.Streaming
import com.subhamgupta.httpserver.utils.Transfer
import com.subhamgupta.httpserver.utils.getFileNameFromUri
import com.subhamgupta.httpserver.utils.receiveEvent
import com.subhamgupta.httpserver.utils.uriForMediaWithFilename
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.server.plugins.origin
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        viewModel.checkUser()
//        lifecycleScope.launchWhenStarted {
//            viewModel.userPresent.collect {
//                if (!it) {
//                    startActivity(Intent(this@MainActivity, RegisterUserActivity::class.java))
//                    finish()
//                } else {
//                    init()
//                }
//            }
//        }
    }

    private fun showQR(str: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap =
                barcodeEncoder.encodeBitmap(str, BarcodeFormat.QR_CODE, 500, 500)
            binding.qrImage.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val selectFileActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data?.clipData != null) {
                    val count = data.clipData?.itemCount ?: 0
                    for (i in 0 until count) {
                        val imageUri: Uri? = data.clipData?.getItemAt(i)?.uri
                        selectedFiles.add(
                            uriForMediaWithFilename(
                                contentResolver,
                                getFileNameFromUri(contentResolver, imageUri!!)!!
                            )
                        )
                    }
                    Log.e("folder selected", "$selectedFiles")
                    val no = NotificationObj(
                        msg = "files received",
                        files = selectedFiles
                    )
                    viewModel.sendNotification(no)
                    selectedFiles.clear()
                }
                //If single image selected
                else if (data?.data != null) {
                    val imageUri: Uri? = data.data
                }
            }
        }

    private fun browseFile() {
        val chooseFileIntent = Intent(Intent.ACTION_GET_CONTENT)
//        chooseFileIntent.action = Intent.ACTION_OPEN_DOCUMENT
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
        chooseFileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        chooseFileIntent.type = "*/*"
        selectFileActivityResult.launch(chooseFileIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    private val selectedFiles = mutableListOf<String>()


    private fun addUser() {
        val intent = Intent(this, BaseActivity::class.java)
        intent.putExtra("addUser", true)
        startActivity(intent)
    }

    private fun showUserManage() {
        val intent = Intent(this, BaseActivity::class.java)
        intent.putExtra("manageUser", true)
        startActivity(intent)
    }

    fun init() {
        viewModel.init()

        val serviceComponent = ComponentName(packageName, MyNotificationListener::class.java.name)
        val pm = applicationContext.packageManager
        pm.setComponentEnabledSetting(
            serviceComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        val ns = NetworkScanner(this)
        val mp = ns.scanNetwork()
        Log.e("network", "found $mp")
        lifecycleScope.launchWhenStarted {
            MyNotificationListener.listener.collect {
                Log.e("main notify", "$it")

                val notificationObj = NotificationObj(
                    notify = true,
                    notifyObj = it
                )
                viewModel.sendNotification(notificationObj)

            }
        }
        if (!MyNotificationListener.isNotificationListenerEnabled(this)) {
            MyNotificationListener.requestNotificationListenerPermission(this)
        }
        viewModel.getAllFolders()
        binding.openFileExplorerBtn.setOnClickListener {
            browseFile()
        }

        viewModel.startServer()
        binding.addUser.setOnClickListener {
            addUser()

        }
        binding.manageUser.setOnClickListener {
            showUserManage()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.hostAddress.collect {
                binding.hostAddress.text = it
                showQR(it)
            }
        }
        binding.receiveBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, BaseActivity::class.java)
            intent.putExtra("upload", true)
            startActivity(intent)
        }
        receiveEvent<Transfer> {
            val intent = Intent(this@MainActivity, BaseActivity::class.java)
            intent.putExtra("upload", true)
            startActivity(intent)
        }

        receiveEvent<Streaming> {
            showSnackBar(it.message)
        }
        binding.startServer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.startServer()
                "Server running".also { binding.startServer.text = it }
                binding.qrImage.visibility = View.VISIBLE
            } else {
                viewModel.stopServer()
                "Server stopped".also { binding.startServer.text = it }
                binding.qrImage.visibility = View.GONE
            }
        }
        receiveEvent<ConfirmToAcceptLoginEvent> { event ->
            event.let {
                val session = it.session
                val clientIp = it.session.call.request.origin.remoteAddress
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("New login request")
                    .setMessage("Confirm to login into the browser. ip=$clientIp")
                    .setPositiveButton("Accept") { _, _ ->
                        launch {
                            withContext(Dispatchers.IO) {
                                viewModel.updateUserStatus(it.user._id, "Logged In")
                                session.send(
                                    Frame.Text(
                                        JsonHelper.jsonEncode(
                                            AuthResponse(
                                                status = AuthStatus.AUTHENTICATED,
                                                token = it.token,
                                                username = it.user.username,
                                                email = it.user.email,
                                                expiresIn = it.tokenConfig.expiresIn,
                                                hasAccessTo = "A,B,C",
                                                userType = "Normal"
                                            )
                                        )
                                    )
                                )
                            }
                        }
                    }.setNegativeButton("Reject") { _, _ ->
                        launch {
                            withContext(Dispatchers.IO) {
                                session.close(
                                    CloseReason(
                                        CloseReason.Codes.TRY_AGAIN_LATER,
                                        "rejected"
                                    )
                                )
                            }
                        }
                    }
                    .create()
                    .show()


            }
        }
//        receiveEvent<Request>{
//            showSnackBar("Request from ${it.from}")
//        }
    }

    private fun showSnackBar(msg: String) {
        val snackBarView = Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
        val view = snackBarView.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snackBarView.setBackgroundTint(resources.getColor(R.color.colorSecondary))
            .setTextColor(resources.getColor(R.color.colorOnSecondary))
            .show()
    }

    override fun onDestroy() {
        viewModel.stopServer()
        super.onDestroy()
    }
}
