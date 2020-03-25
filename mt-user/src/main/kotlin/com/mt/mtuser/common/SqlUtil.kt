package com.mt.mtuser.common

import com.mt.mtuser.entity.User
import org.springframework.data.r2dbc.query.Update
import java.beans.Introspector

/**
 * Created by gyh on 2020/3/25.
 */

inline fun <reified T : Any> T.update(): Update {
    var up: Update? = null
    val beanInfo = Introspector.getBeanInfo(T::class.java)
    val proDescriptors = beanInfo.propertyDescriptors
    if (proDescriptors != null && proDescriptors.isNotEmpty()) {
        for (propDesc in proDescriptors) {
            val o = propDesc.readMethod.invoke(this)
            if (o != null) {
                if (up == null) {
                    up = Update.update(propDesc.name.humpToUnderline(), o)
                } else {
                    up.set(propDesc.name.humpToUnderline(), o)
                }
            }
        }
    }
    return up ?: throw IllegalStateException("没有可更新的字段")
}

/***
 * 驼峰命名转为下划线命名
 */

fun String.humpToUnderline(): String {
    val sb = StringBuilder(this)
    var temp = 0//定位
    if (!this.contains("_")) {
        for (i in this.indices) {
            if (Character.isUpperCase(this[i])) {
                sb.insert(i + temp, "_")
                temp += 1
            }
        }
    }
    return sb.toString().toLowerCase()
}