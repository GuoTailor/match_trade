package com.mt.mtcommon

/**
 * Created by gyh on 2020/5/4.
 * @apiDefine RivalInfo
 * @apiParam {Array} rivals 对手
 */
class RivalInfo(var userId: Int? = null,
                var userName: String? = null,
                var roomId: String? = null,
                var mode: String? = null,
                var rivals: ArrayList<Int>? = null)