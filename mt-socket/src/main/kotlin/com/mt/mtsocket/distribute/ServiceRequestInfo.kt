package com.mt.mtsocket.distribute


/**
 * Created by gyh on 2020/4/16.
 */
class ServiceRequestInfo(val order: String, val data: Any? = null, val req: Int) {

    private val map: Map<*, *>? = data as? Map<*, *>?

    fun getParameterValues(name: String): Array<Any?> {
        return arrayOf(map?.get(name))
    }
}
