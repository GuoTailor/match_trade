package com.mt.mtcommon

/**
 * Created by gyh on 2020/4/26.
 */
enum class RoomEnum(val flag: String) {
    CLICK("C"),
    BICKER("B"),
    DOUBLE("D"),
    CONTINUE("E"),
    TIMING("I");

    companion object {
        /**
         * 通过房间标识获取房间枚举
         */
        @JvmStatic
        fun getRoomEnum(flag: String): RoomEnum {
            return when (flag) {
                CLICK.flag -> CLICK
                BICKER.flag -> BICKER
                DOUBLE.flag -> DOUBLE
                CONTINUE.flag -> CONTINUE
                TIMING.flag -> TIMING
                else -> throw IllegalStateException("不支持的房间号模式${flag}")
            }
        }

    }
}
