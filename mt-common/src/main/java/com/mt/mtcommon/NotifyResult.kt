package com.mt.mtcommon

/**
 * Created by gyh on 2020/5/14.
 */

const val addOrderNotify = "order"      // 报价结果通知
const val cancelOrderNotify = "cancel"  // 撤单结果通知
const val addRivalNotify = "rival"      // 添加对手结果通知
const val updateTopThree = "topThree"   // 前三档报价
const val updateFirstOrder = "firstOrder"   // 最新一笔报价

data class NotifyResult(var userId: Int? = null,
                        var roomId: String? = null,
                        var model: String? = null,
                        var obj: String,    // 通知的对象
                        var result: Boolean, // 操作结果
                        var data: Any? = null   // 结果数据
)

fun OrderParam.toNotifyResult(result: Boolean) = NotifyResult(this.userId, this.roomId, this.mode, addOrderNotify, result, this)

fun CancelOrder.toNotifyResult(result: Boolean) = NotifyResult(this.userId, this.roomId, this.mode, cancelOrderNotify, result, this)

fun RivalInfo.toNotifyResult(result: Boolean) = NotifyResult(this.userId, this.roomId, this.mode, addRivalNotify, result, this)

fun TopThree.toNotifyResult() = NotifyResult(roomId = this.roomId, model = this.mode, obj = updateTopThree, result = true, data = this)

fun OrderParam.toTopThreeNotify(data: Any) = NotifyResult(this.userId, this.roomId, this.mode, updateTopThree, true, data)

fun CancelOrder.toTopThreeNotify(data: Any) = NotifyResult(this.userId, this.roomId, this.mode, updateTopThree, true,  data)

fun OrderInfo.toFirstOrder(roomId: String, mode: String) = NotifyResult(this.userId, roomId, mode, updateFirstOrder, true,  this)