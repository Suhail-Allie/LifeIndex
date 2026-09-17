package com.lifeindex.app.data.repository

import com.lifeindex.app.data.model.TemplateFieldsResponse
import com.lifeindex.app.data.model.TemplatesResponse
import com.lifeindex.app.data.remote.RetrofitClient
import retrofit2.Response

class TemplateRepository {

    private val apiService = RetrofitClient.api

    suspend fun getTemplates(): Response<TemplatesResponse> {
        return apiService.getTemplates()
    }

    suspend fun getTemplateFields(
        templateId: String
    ): Response<TemplateFieldsResponse> {
        return apiService.getTemplateFields(templateId)
    }
}