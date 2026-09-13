package com.example.alarmbuddy.ui

import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.alarmbuddy.ui.theme.HighlightColor

// To create consistent buttons across the application.
@Composable
fun MainButtonColors() = ButtonDefaults.buttonColors(
    containerColor = HighlightColor,
    contentColor = Color.White,
)
