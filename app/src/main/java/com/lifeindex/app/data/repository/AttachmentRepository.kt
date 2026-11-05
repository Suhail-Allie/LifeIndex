package com.lifeindex.app.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.lifeindex.app.data.model.AttachmentDownloadResponse
import com.lifeindex.app.data.model.AttachmentUploadResponse
import com.lifeindex.app.data.model.AttachmentsResponse
import com.lifeindex.app.data.model.MessageResponse
import com.lifeindex.app.data.remote.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class AttachmentRepository {
    private val api = RetrofitClient.api
    private val maxFileSize = 10 * 1024 * 1024
    private val allowedTypes = setOf("application/pdf", "image/jpeg", "image/png")

    suspend fun listAttachments(token: String): Response<AttachmentsResponse> =
        api.getAttachments("Bearer $token")

    suspend fun uploadAttachment(
        context: Context,
        token: String,
        trackerId: String,
        uri: Uri
    ): Response<AttachmentUploadResponse> {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)?.lowercase()
            ?: throw IllegalArgumentException("UNSUPPORTED_TYPE")

        if (mimeType !in allowedTypes) {
            throw IllegalArgumentException("UNSUPPORTED_TYPE")
        }

        var fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "document"
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (column >= 0) fileName = cursor.getString(column) ?: fileName
            }
        }

        val input = resolver.openInputStream(uri)
            ?: throw IOException("Unable to open the selected file")
        val bytes = input.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                total += count
                if (total > maxFileSize) {
                    throw IllegalArgumentException("FILE_TOO_LARGE")
                }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }

        if (bytes.isEmpty()) throw IOException("The selected file is empty")

        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)
        return api.uploadAttachment("Bearer $token", trackerId, filePart)
    }

    suspend fun getDownloadUrl(
        token: String,
        attachmentId: String
    ): Response<AttachmentDownloadResponse> =
        api.getAttachmentDownloadUrl("Bearer $token", attachmentId)

    suspend fun deleteAttachment(
        token: String,
        attachmentId: String
    ): Response<MessageResponse> =
        api.deleteAttachment("Bearer $token", attachmentId)
}