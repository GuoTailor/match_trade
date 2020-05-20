package com.mt.mtsocket.common

/**
 * Created by gyh on 2020/5/15.
 */
object NotifyReq {
    const val pushSellOrder = -9        // 推送卖单更新
    const val pushBuyOrder = -8         // 推送买单更新，没有方向的订单也使用该req
    const val pushTradeInfo = -7        // 推送交易信息更新
    const val notifyRoomClose = -6      // 房间关闭通知
    const val notifyTopThree = -5       // 前三档报价通知
    const val notifyResult = -4         // 报价、撤单、选择对手结果通知
    const val notifyNumberChange = -3   // 房间人数变化通知
    const val notifyTrade = -2          // 订单消费通知
    const val errorNotify = -1          // 错误通知
}