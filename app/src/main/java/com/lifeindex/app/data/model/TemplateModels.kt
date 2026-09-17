package com.lifeindex.app.data.model

data class TemplateData(
    val id: String,
    val name: String,
    val description: String?,
    val tracker_type: String,
    val category_id: String?,
    val category_name: String?
)

data class TemplatesResponse(
    val templates: List<TemplateData>
)

data class TemplateFieldData(
    val id: String,
    val template_id: String,
    val field_name: String,
    val field_type: String,
    val is_required: Boolean,
    val field_options: Any?,
    val display_order: Int
)

data class TemplateFieldsResponse(
    val fields: List<TemplateFieldData>
)