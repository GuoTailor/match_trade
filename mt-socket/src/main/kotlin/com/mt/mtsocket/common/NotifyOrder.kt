package com.mt.mtsocket.common

/**
 * Created by gyh on 2020/5/15.
 */
object NotifyOrder {
    const val connectSucceed = -99      // 连接成功通知
    const val rivalResult = -13         // 选择对手结果通知
    const val cancelResult = -12        // 撤单结果通知
    const val offerResult = -11         // 报价结果通知
    const val differentPlaceLogin = -10 // 用户账号在其他地方登录
    const val pushSellOrder = -9        // 推送卖单更新（已弃用）
    const val pushBuyOrder = -8         // 推送买/卖单更新
    const val pushTradeInfo = -7        // 推送交易信息更新
    const val notifyRoomClose = -6      // 房间关闭通知
    const val notifyTopThree = -5       // 前三档报价通知
    const val notifyFirstOrder = -4     // 最新一笔报价通知
    const val notifyNumberChange = -3   // 房间人数变化通知
    const val notifyTrade = -2          // 订单消费通知
    const val errorNotify = -1          // 错误通知
    const val requestReq = 0            // 正常响应
}