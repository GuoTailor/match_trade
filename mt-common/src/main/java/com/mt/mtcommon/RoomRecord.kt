package com.mt.mtcommon

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
open class RoomRecord(
        @Id var id: Int? = null,
        open var roomId: String? = null,        // 房间id，四张房间表唯一
        open var model: String? = null,         // 模式对应撮合模式
        open var stockId: Int? = null,          // 股票id
        open var companyId: Int? = null,        // 公司id
        open var startTime: Date? = null,       // 启用时间
        open var endTime: Date? = null,         // 结束时间,房间结束时会重新计算
        open var quoteTime: LocalTime = LocalTime.MIN,  // 报价和选择身份时间
        open var secondStage: LocalTime? = null,// 第二阶段时间
        open var rival: Int? = null,            // 选择对手个数
        open var cycle: LocalTime? = null,      // 周期
        open var duration: LocalTime? = null    // 时长,房间结束时会重新计算
) {

    open fun computingTime(): RoomRecord {
        duration = startTime?.let { start ->
            endTime?.let { end ->
                LocalTime.ofSecondOfDay((end.time - start.time) / 1000 - quoteTime.toSecondOfDay())
            }
        }
        return this
    }


}

