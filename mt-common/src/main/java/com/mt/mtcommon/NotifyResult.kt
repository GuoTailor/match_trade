package com.mt.mtcommon

/**
 * Created by gyh on 2020/5/14.
 */

const val addOrderNotify = "order"      // 报价结果通知
const val cancelOrderNotify = "cancel"  // 撤单结果通知
const val addRivalNotify = "rival"      // 添加对手结果通知

data class NotifyResult(var userId: Int? = null,
                        var roomId: String? = null,
                        var flag: String? = null,
                        var obj: String,    // 通知的对象
                        var result: Boolean // 操作结果
)

fun OrderParam.toNotifyResult(result: Boolean) = NotifyResult(this.userId, this.roomId, this.flag, addOrderNotify, result)

fun CancelOrder.toNotifyResult(result: Boolean) = NotifyResult(this.userId, this.roomId, this.flag, cancelOrderNotify, result)

fun RivalInfo.toNotifyResult(result: Boolean) = NotifyResult(this.userId, this.roomId, this.flag, addRivalNotify, result)