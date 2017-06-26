package com.weiwa.ljl.eden.network

import java.util.*

/**
 * Created by hzfd on 2017/5/17.
 */

class ContentPojo {
    var status: String? = null
    var uploaded: List<Uploaded>? = null
    private val additionalProperties = HashMap<String, Any>()

    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties.put(name, value)
    }

    val defaultId: String?
        get() = uploaded!![0].id
}

class Uploaded {
    var id: String? = null
    var filename: String? = null
    private val additionalProperties = HashMap<String, Any>()

    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties.put(name, value)
    }

}
