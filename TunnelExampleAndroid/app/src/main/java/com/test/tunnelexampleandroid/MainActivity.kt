package com.test.tunnelexampleandroid

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
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
        val intent = Intent(this, TunnelService::class.java)
        intent.putExtra("serverAddress", ipAddress)
        intent.action = if (isServiceWorking.value == false) TunnelService.ACTION_START else TunnelService.ACTION_STOP
        startService(intent)
    }

    private fun onVpnServiceWorkingChanger() {
        isServiceWorking.postValue(TunnelService.isWorking)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isServiceWorking.value = TunnelService.isWorking
        TunnelService.onIsWorkingChanged = { onVpnServiceWorkingChanger() }

        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    startService()
                }
            }
        setContent {
            TunnelExampleAndroidTheme {
                MainView(
                    onClickStart = { onClick(it) },
                    isServiceWorking = isServiceWorking
                )
            }
        }
    }
}