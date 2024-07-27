package example.com.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class AnnotationResult(
    val rac: String,
    val lap: Int,
    val rap: Int,
    val refkey: String,
    val vcfId: String,
    val clnsig: String,
    val clnrevstat: String,
    val clnvc: String
)