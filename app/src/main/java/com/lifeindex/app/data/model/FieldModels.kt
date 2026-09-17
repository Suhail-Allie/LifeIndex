package com.lifeindex.app.data.model

data class TrackerFieldData(
    val id: String,
    val field_name: String,
    val field_type: String,
    val is_required: Boolean,
    val field_options: Any?,
    val display_order: Int,
    val text_value: String?,
    val number_value: Double?,
    val date_value: String?,
    val boolean_value: Boolean?,
    val json_value: Any?
)

data class TrackerFieldsResponse(
    val fields: List<TrackerFieldData>
)

data class FieldValueRequest(
    val fieldDefinitionId: String,
    val textValue: String? = null,
    val numberValue: Double? = null,
    val dateValue: String? = null,
    val booleanValue: Boolean? = null,
    val jsonValue: Any? = null
)

data class UpdateTrackerFieldsRequest(
    val fields: List<FieldValueRequest>
)

data class UpdateTrackerFieldsResponse(
    val message: String
)