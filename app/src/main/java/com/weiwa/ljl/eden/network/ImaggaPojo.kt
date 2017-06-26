package com.weiwa.ljl.eden.network

import java.util.*

/**
 * Created by hzfd on 2017/5/17.
 */
class ImaggaPojo {
    var results: List<Result>? = null
    private val additionalProperties = HashMap<String, Any>()
    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties.put(name, value)
    }
}

class Result {
    var taggingId: Any? = null
    var image: String? = null
    var tags: List<Tag>? = null
    private val additionalProperties = HashMap<String, Any>()
    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties.put(name, value)
    }
}

class Tag {
    var confidence: Double? = null
    var tag: String? = null
    private val additionalProperties = HashMap<String, Any>()
    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties.put(name, value)
    }
}
