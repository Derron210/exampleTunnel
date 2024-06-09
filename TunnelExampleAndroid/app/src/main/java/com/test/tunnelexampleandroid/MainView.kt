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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(onClickStart: (String) -> Unit) {

    var ipAddress by remember { mutableStateOf("255.255.255.255") }

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
                label = { Text("Label") }
            )

            Button(onClick = { onClickStart(ipAddress) }

            ) {
                Text(text = "Start")

            }
        }
    }
}

@Preview
@Composable
fun MainViewPreview() {
    MainView {

    }
}