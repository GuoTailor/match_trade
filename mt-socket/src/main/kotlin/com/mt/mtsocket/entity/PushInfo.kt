package com.mt.mtsocket.entity

/**
 * Created by gyh on 2020/5/19.
 */
data class PushInfo(
        val roomId: String,
        val mode: String,
        val event: String
)

const val tradeUpdateEvent = "tradeEvent"
const val buyOrderUpdateEvent = "buyOrderEvent"
const val sellOrderUpdateEvent = "sellOrderEvent"

