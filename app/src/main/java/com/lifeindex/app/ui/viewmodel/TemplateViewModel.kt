package com.lifeindex.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeindex.app.data.model.TemplateData
import com.lifeindex.app.data.model.TemplateFieldData
import com.lifeindex.app.data.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TemplateUiState(
    val isLoading: Boolean = false,
    val templates: List<TemplateData> = emptyList(),
    val fields: List<TemplateFieldData> = emptyList(),
    val errorMessage: String? = null
)

class TemplateViewModel : ViewModel() {

    private val repository = TemplateRepository()

    private val _uiState = MutableStateFlow(TemplateUiState())
    val uiState: StateFlow<TemplateUiState> = _uiState

    fun loadTemplates() {
        viewModelScope.launch {
            _uiState.value = TemplateUiState(isLoading = true)

            try {
                val response = repository.getTemplates()

                if (response.isSuccessful) {
                    _uiState.value = TemplateUiState(
                        templates = response.body()?.templates ?: emptyList()
                    )
                } else {
                    _uiState.value = TemplateUiState(
                        errorMessage = "Unable to load templates"
                    )
                }
            } catch (error: Exception) {
                _uiState.value = TemplateUiState(
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }

    fun loadTemplateFields(templateId: String) {
        viewModelScope.launch {
            try {
                val response = repository.getTemplateFields(templateId)

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        fields = response.body()?.fields ?: emptyList(),
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Unable to load template fields"
                    )
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Unable to connect to the server"
                )
            }
        }
    }
}