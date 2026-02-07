package com.nexxlabs.chhotu.domain.engine.ai.model

import com.google.gson.annotations.SerializedName

data class StructuredIntent(
    @SerializedName("intent_type")
    val intentType: IntentType,
    
    @SerializedName("target_app")
    val targetApp: String?,
    
    @SerializedName("action")
    val action: String?,
    
    @SerializedName("entities")
    val entities: Map<String, String>,
    
    @SerializedName("confidence")
    val confidence: Double
)
