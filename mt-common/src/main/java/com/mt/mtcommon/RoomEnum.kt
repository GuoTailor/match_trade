package com.mt.mtcommon

/**
 * Created by gyh on 2020/4/26.
 */
enum class RoomEnum(val flag: String) {
    CLICK("C"),
    BICKER("B"),
    DOUBLE("D"),
    TIMELY("E"),
    TIMING("I");

    companion object {
        /**
         * 通过房间号获取房间枚举
         */
        @JvmStatic
        fun getRoomEnum(flag: String): RoomEnum {
            return when (flag) {
                CLICK.flag -> CLICK
                BICKER.flag -> BICKER
                DOUBLE.flag -> DOUBLE
                TIMELY.flag -> TIMELY
                TIMING.flag -> TIMING
                else -> throw IllegalStateException("不支持的房间号模式${flag}")
            }
        }

        /**
         * 通过房间号获取房间模式
         */
        @JvmStatic
        fun getRoomModel(roomId: String): RoomEnum {
            return when (roomId[0].toString()) {
                CLICK.flag -> CLICK
                BICKER.flag -> BICKER
                DOUBLE.flag -> DOUBLE
                TIMELY.flag -> TIMELY
                TIMING.flag -> TIMING
                else -> throw IllegalStateException("不支持的房间号模式${roomId}")
            }
        }
    }
}
