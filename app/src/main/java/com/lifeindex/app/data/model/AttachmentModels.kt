package com.lifeindex.app.data.model

data class AttachmentData(
    val id: String,
    val tracker_id: String,
    val tracker_title: String,
    val file_name: String,
    val mime_type: String,
    val file_size: Long,
    val created_at: String
)

data class AttachmentsResponse(
    val attachments: List<AttachmentData>,
    val count: Int = 0
)

data class AttachmentUploadResponse(
    val message: String,
    val attachment: AttachmentData
)

data class AttachmentDownloadResponse(
    val downloadUrl: String,
    val expiresIn: Int
)