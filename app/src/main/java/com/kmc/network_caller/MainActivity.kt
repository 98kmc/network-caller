package com.kmc.network_caller

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataSource: DataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Text(text = "Hello!")

            LaunchedEffect(Unit) {

                Log.d("MAIN", "onCreate: ${dataSource.fetchPost()}")
                Log.d("MAIN", "onCreate: ${dataSource.createPost()}")
            }
        }
    }
}