package com.mt.mtcommon

/**
 * Created by gyh on 2020/5/2.
 */
object TradeState {
    const val SUCCESS = "success"   // 交易成功
    const val FAILED = "failed"     // 交易失败
    const val STAY = "stay"         // 订单待撮合

    fun getZhName(state: String?): String {
        return when (state) {
            SUCCESS -> "交易成功"
            FAILED -> "交易失败"
            STAY -> "订单待撮合"
            else -> error("不受支持的状态:$state")
        }
    }
}