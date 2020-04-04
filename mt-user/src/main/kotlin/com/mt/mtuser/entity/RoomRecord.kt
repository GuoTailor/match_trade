package com.mt.mtuser.entity

import com.mt.mtuser.common.minus
import com.mt.mtuser.entity.room.BaseRoom
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/3/26.
 * 房间启用记录
 * @apiDefine ClickMatch
 * @apiParam {Integer} id
 * @apiParam {Integer} roomId 房间id
 * @apiParam {String} model 模式对应撮合模式
 * @apiParam {Integer} stockId 股票id
 * @apiParam {Integer} companyId 公司id
 * @apiParam {Date} startTime 启用时间
 * @apiParam {Date} endTime 结束时间
 */
@Table("mt_room_record ")
class RoomRecord(
        @Id
        var id: Int? = null,
        var roomId: String? = null,     // 房间id，四张房间表唯一
        var model: String? = null,      // 模式对应撮合模式
        var stockId: Int? = null,       // 股票id
        var companyId: Int? = null,     // 公司id
        var startTime: Date? = null,    // 启用时间
        var endTime: Date? = null,      // 结束时间
        @org.springframework.data.annotation.Transient
        var duration: LocalTime? = null  // 时长
) {
    constructor(room: BaseRoom) :
            this(roomId = room.roomId, model = room.flag, companyId = room.companyId, duration = room.time)


    fun computingTime(): RoomRecord{
        duration = startTime?.let {start ->
            endTime?.let { end ->
                LocalTime.ofSecondOfDay((start - end) / 1000)
            }
        }
        return this
    }
}

