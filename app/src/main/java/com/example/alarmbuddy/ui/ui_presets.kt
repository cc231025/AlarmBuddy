package com.example.alarmbuddy.ui

import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.alarmbuddy.R

//A very empty file, but for future projects I could gather a lot more presets here to keep my UI consistent

// To create consistent buttons acreoss my application
@Composable
fun MainButtonColors() = ButtonDefaults.buttonColors(
    containerColor = colorResource(id = R.color.highlight),
    contentColor = Color.White
)