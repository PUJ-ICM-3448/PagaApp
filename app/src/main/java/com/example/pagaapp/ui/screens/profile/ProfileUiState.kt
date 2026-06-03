package com.example.pagaapp.ui.screens.profile

import android.graphics.Bitmap

data class ProfileUiState(
    val profile: ProfileModel? = null,
    val isLoading: Boolean = false,
    val profileBitmap: Bitmap? = null
)
