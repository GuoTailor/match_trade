package com.mt.mtcommon

/**
 * Created by gyh on 2020/5/6.
 * 撤单消息
 * @apiDefine CancelOrder
 * @apiParam {Integer} [userId] 用户的id
 * @apiParam {String} roomId 房间Id
 */
data class CancelOrder(var userId: Int? = null, var roomId: String? = null)