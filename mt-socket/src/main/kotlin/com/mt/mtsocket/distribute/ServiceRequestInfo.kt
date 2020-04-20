package com.mt.mtsocket.distribute

import com.mt.mtsocket.common.Util

/**
 * Created by gyh on 2020/4/16.
 */
class ServiceRequestInfo(val order: String, val data: Any, val body: String, val req: Int) {

    private val map: Map<String, Any>? by lazy { Util.getParameterMap(this.body) }

    fun getParameterValues(name: String): Array<Any?>? {
        return if (map == null) null else arrayOf(map?.get(name))
    }
}