package com.lifeindex.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifeindex.app.data.local.SessionManager
import com.lifeindex.app.data.model.AttachmentData
import com.lifeindex.app.data.model.TrackerData
import com.lifeindex.app.data.remote.RetrofitClient
import com.lifeindex.app.data.repository.AttachmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DocumentsUiState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val attachments: List<AttachmentData> = emptyList(),
    val trackers: List<TrackerData> = emptyList(),
    val messageCode: String? = null,
    val errorCode: String? = null
)

class DocumentsViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val repository = AttachmentRepository()
    private val api = RetrofitClient.api

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState

    fun load() {
        val token = sessionManager.getAccessToken()
        if (token == null) {
            _uiState.value = DocumentsUiState(errorCode = "AUTH")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorCode = null)
            try {
                val trackersResponse = api.getTrackers("Bearer $token")
                val attachmentsResponse = repository.listAttachments(token)

                if (!trackersResponse.isSuccessful || !attachmentsResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorCode = "LOAD"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    trackers = trackersResponse.body()?.trackers ?: emptyList(),
                    attachments = attachmentsResponse.body()?.attachments ?: emptyList(),
                    errorCode = null
                )
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorCode = "LOAD"
                )
            }
        }
    }

    fun uploadAttachment(context: Context, trackerId: String, uri: Uri) {
        val token = sessionManager.getAccessToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(errorCode = "AUTH")
            return
        }
        if (trackerId.isBlank()) {
            _uiState.value = _uiState.value.copy(errorCode = "TRACKER_REQUIRED")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploading = true,
                errorCode = null,
                messageCode = null
            )
            try {
                val response = repository.uploadAttachment(context, token, trackerId, uri)
                if (!response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        errorCode = when (response.code()) {
                            413 -> "FILE_TOO_LARGE"
                            415 -> "UNSUPPORTED_TYPE"
                            else -> "UPLOAD"
                        }
                    )
                    return@launch
                }

                refreshAttachmentList(token)
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    messageCode = "UPLOADED",
                    errorCode = null
                )
            } catch (error: IllegalArgumentException) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    errorCode = when (error.message) {
                        "FILE_TOO_LARGE" -> "FILE_TOO_LARGE"
                        "UNSUPPORTED_TYPE" -> "UNSUPPORTED_TYPE"
                        else -> "UPLOAD"
                    }
                )
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    errorCode = "UPLOAD"
                )
            }
        }
    }

    fun downloadAttachment(attachmentId: String, onReady: (String) -> Unit) {
        val token = sessionManager.getAccessToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(errorCode = "AUTH")
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.getDownloadUrl(token, attachmentId)
                val url = response.body()?.downloadUrl
                if (response.isSuccessful && !url.isNullOrBlank()) {
                    onReady(url)
                } else {
                    _uiState.value = _uiState.value.copy(errorCode = "DOWNLOAD")
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(errorCode = "DOWNLOAD")
            }
        }
    }

    fun deleteAttachment(attachmentId: String) {
        val token = sessionManager.getAccessToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(errorCode = "AUTH")
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.deleteAttachment(token, attachmentId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        attachments = _uiState.value.attachments.filterNot { it.id == attachmentId },
                        messageCode = "DELETED",
                        errorCode = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(errorCode = "DELETE")
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(errorCode = "DELETE")
            }
        }
    }

    private suspend fun refreshAttachmentList(token: String) {
        val response = repository.listAttachments(token)
        if (response.isSuccessful) {
            _uiState.value = _uiState.value.copy(
                attachments = response.body()?.attachments ?: emptyList()
            )
        }
    }
}