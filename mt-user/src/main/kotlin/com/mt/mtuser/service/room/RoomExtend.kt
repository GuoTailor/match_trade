package com.mt.mtuser.service.room

import com.mt.mtcommon.RoomEnum
import com.mt.mtuser.entity.room.*

/**
 * Created by gyh on 2020/3/24.
 */
object RoomExtend {

    /**
     * 公司能开的房间模式号对应的房间模式列表
     */
    fun getRoomModels(mode: String): Collection<String> {
        return when (mode) {
            "1" -> listOf(RoomEnum.CLICK.flag)
            "2" -> listOf(RoomEnum.CLICK.flag, RoomEnum.TIMING.flag)
            "3" -> listOf(RoomEnum.CLICK.flag, RoomEnum.TIMING.flag, RoomEnum.DOUBLE.flag)
            "4" -> listOf(RoomEnum.CLICK.flag, RoomEnum.BICKER.flag, RoomEnum.TIMING.flag, RoomEnum.DOUBLE.flag, RoomEnum.TIMELY.flag)
            else -> throw java.lang.IllegalStateException("错误：${mode}和已有竞价模式{1：点选、2： 点选+定时、3：及时 +点选+两两撮合、4：全部}不匹配")
        }
    }

    /**
     * 通过房间号获取房间模式
     */
    fun getRoomModel(roomId: String): RoomEnum {
        return when (roomId[0].toString()) {
            RoomEnum.CLICK.flag -> RoomEnum.CLICK
            RoomEnum.BICKER.flag -> RoomEnum.BICKER
            RoomEnum.DOUBLE.flag -> RoomEnum.DOUBLE
            RoomEnum.TIMELY.flag -> RoomEnum.TIMELY
            RoomEnum.TIMING.flag -> RoomEnum.TIMING
            else -> throw IllegalStateException("不支持的房间号模式${roomId}")
        }
    }

    /**
     * 通过房间号获取房间枚举
     */
    fun getRoomEnum(flag: String): RoomEnum {
        return when (flag) {
            RoomEnum.CLICK.flag -> RoomEnum.CLICK
            RoomEnum.BICKER.flag -> RoomEnum.BICKER
            RoomEnum.DOUBLE.flag -> RoomEnum.DOUBLE
            RoomEnum.TIMELY.flag -> RoomEnum.TIMELY
            RoomEnum.TIMING.flag -> RoomEnum.TIMING
            else -> throw IllegalStateException("不支持的房间号模式${flag}")
        }
    }

    /**
     * 更改房间模式时需要更改房间id的标识
     */
    fun <T : BaseRoom> replaceRoomFlag(roomId: String, newRoom: T): String {
        return when (roomId[0].toString()) {
            RoomEnum.CLICK.flag -> roomId.replace(RoomEnum.CLICK.flag, newRoom.flag)
            RoomEnum.BICKER.flag -> roomId.replace(RoomEnum.BICKER.flag, newRoom.flag)
            RoomEnum.DOUBLE.flag -> roomId.replace(RoomEnum.DOUBLE.flag, newRoom.flag)
            RoomEnum.TIMELY.flag -> roomId.replace(RoomEnum.TIMELY.flag, newRoom.flag)
            RoomEnum.TIMING.flag -> roomId.replace(RoomEnum.TIMING.flag, newRoom.flag)
            else -> throw IllegalStateException("不支持的房间号模式${roomId}")
        }
    }

}
