package com.mt.mtsocket.socket

import org.springframework.stereotype.Component

/**
 * Created by gyh on 2020/4/8.
 * @param value websocket url路径
 */
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Component
annotation class WebSocketMapping(val value: String = "")