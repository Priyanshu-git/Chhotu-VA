package com.nexxlabs.chhotu.domain.engine.ai.model

import com.google.gson.annotations.SerializedName

enum class IntentType {
    @SerializedName("OPEN_APP")
    OPEN_APP,
    
    @SerializedName("APP_ACTION")
    APP_ACTION,
    
    @SerializedName("SYSTEM_ACTION")
    SYSTEM_ACTION,
    
    @SerializedName("UNKNOWN")
    UNKNOWN
}
