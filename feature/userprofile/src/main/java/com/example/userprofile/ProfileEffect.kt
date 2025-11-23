package com.example.userprofile

sealed class ProfileEffect {
    object SignedOut : ProfileEffect()
}