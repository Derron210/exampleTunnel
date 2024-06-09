package com.test.tunnelexampleandroid

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnManager
import android.net.VpnService
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import com.test.tunnelexampleandroid.ui.theme.TunnelExampleAndroidTheme

class MainActivity : ComponentActivity() {

    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private var ipAddress = ""
    private val isServiceWorking = MutableLiveData<Boolean>(false)

    private fun onClick(ipAddress: String) {
        val intent = VpnService.prepare(this)
        this.ipAddress = ipAddress
        if (intent != null) {
            startForResult.launch(intent)
        }
        startService()

    }

    private fun startService() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.putExtra("serverAddress", "163.172.61.111")
        startService(intent)
    }

    private fun onVpnServiceWorkingChanger() {
        MyVpnService.isWorking
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isServiceWorking.value = MyVpnService.isWorking
        MyVpnService.onIsWorkingChanged = { onVpnServiceWorkingChanger() }

        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    startService()
                }
            }
        setContent {
            TunnelExampleAndroidTheme {
                MainView {
                    onClick(it)
                }
            }
        }
    }
}