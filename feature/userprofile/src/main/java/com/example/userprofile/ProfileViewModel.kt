package com.example.userprofile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase,
    private val updateName: UpdateNameUseCase,
    private val updatePhotoUseCase: UpdatePhotoUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui = _ui.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.Load -> load()
            is ProfileEvent.ChangeFirstName -> _ui.update { it.copy(firstName = event.value) }
            is ProfileEvent.ChangeLastName -> _ui.update { it.copy(lastName = event.value) }
            ProfileEvent.SaveName -> saveName()
            is ProfileEvent.UpdatePhoto -> updatePhoto(event.uri)
            ProfileEvent.SignOut -> signOut()
            ProfileEvent.ToggleEdit -> _ui.update { it.copy(isEditing = !it.isEditing) }
            ProfileEvent.ErrorConsumed -> _ui.update { it.copy(error = null) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }

            when (val res = getProfile()) {
                is ResultState.Success -> _ui.update {
                    it.copy(
                        isLoading = false,
                        firstName = res.data.firstName.orEmpty(),
                        lastName = res.data.lastName.orEmpty(),
                        email = res.data.email.orEmpty(),
                        phone = res.data.phone.orEmpty(),
                        photoUrl = res.data.photoUrl
                    )
                }

                is ResultState.Error -> _ui.update {
                    it.copy(isLoading = false, error = res.error)
                }
            }
        }
    }

    private fun saveName() {
        viewModelScope.launch {
            val current = ui.value
            when (val res = updateName(current.firstName, current.lastName)) {
                is ResultState.Success -> {
                    load()
                    _ui.update { it.copy(isEditing = false) }
                }

                is ResultState.Error -> _ui.update { it.copy(error = res.error) }
            }
        }
    }

    private fun updatePhoto(uri: Uri) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }

            when (val res = updatePhotoUseCase(uri)) {
                is ResultState.Success ->
                    _ui.update { it.copy(photoUrl = res.data, isLoading = false) }

                is ResultState.Error ->
                    _ui.update { it.copy(error = res.error, isLoading = false) }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _effect.emit(ProfileEffect.SignedOut)
        }
    }
}