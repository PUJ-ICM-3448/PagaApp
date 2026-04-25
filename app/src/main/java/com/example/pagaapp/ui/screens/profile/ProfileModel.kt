package com.example.pagaapp.ui.screens.profile

import androidx.compose.ui.graphics.vector.ImageVector

data class ProfileModel(
    val name: String,
    val email: String,
    val initials: String,
    val memberSince: String,
    val totalTransactions: Int,
    val activeFriends: Int,
    val totalShared: Int,
    val expenses: Int,
    val settings: List<ProfileSettingModel>
)

data class ProfileSettingModel(
    val title: String,
    val icon: ImageVector
)