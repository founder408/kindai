package com.kindai.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoadingView(
    message: String = "Yuklanmoqda...",
    modifier: Modifier = Modifier
) {
    KindLoadingState(message = message, modifier = modifier)
}
