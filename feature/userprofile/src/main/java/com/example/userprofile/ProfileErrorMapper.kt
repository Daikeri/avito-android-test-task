package com.example.userprofile

import android.content.Context

fun ProfileError.toMessage(): String =
    when (this) {
        ProfileError.Network ->
            "Ошибка сети. Проверьте подключение к интернету."

        ProfileError.NotAuthorized ->
            "Пользователь не авторизован."

        ProfileError.NotFound ->
            "Данные профиля не найдены."

        is ProfileError.Unknown ->
            this.message ?: "Произошла неизвестная ошибка."

        ProfileError.InvalidName ->
            "Имя и фамилия не могут быть пустыми."

        ProfileError.UploadFailed ->
            "Не удалось загрузить изображение. Попробуйте ещё раз."
    }