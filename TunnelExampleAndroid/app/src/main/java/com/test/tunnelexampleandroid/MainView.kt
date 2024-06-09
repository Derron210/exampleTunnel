package com.test.tunnelexampleandroid

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(onClickStart: (String) -> Unit,
            isServiceWorking: LiveData<Boolean>
             ) {
    var ipAddress by remember { mutableStateOf("255.255.255.255") }

    val isServiceWorking: Boolean by isServiceWorking.observeAsState(false)

    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Hello")

            TextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("Label") },
                enabled = !isServiceWorking
            )

            Button(onClick = { onClickStart(ipAddress) },
            ) {
                Text(text = if (isServiceWorking)  "Stop" else "Start")

            }
        }
    }
}

@Preview
@Composable
fun MainViewPreview() {
    val isServiceWorking = MutableLiveData<Boolean>(false)

    MainView(
        isServiceWorking = isServiceWorking,
        onClickStart = {}
    )
}