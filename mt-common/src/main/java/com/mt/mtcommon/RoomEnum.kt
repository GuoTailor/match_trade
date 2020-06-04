package com.mt.mtcommon

/**
 * Created by gyh on 2020/4/26.
 */
enum class RoomEnum(val mode: String) {
    CLICK("C"),     // 点选
    BICKER("B"),    // 抬杠
    DOUBLE("D"),    // 两两
    CONTINUE("E"),  // 连续
    TIMING("I");    // 定时

    companion object {
        /**
         * 通过房间标识获取房间枚举
         */
        @JvmStatic
        fun getRoomEnum(mode: String): RoomEnum {
            return when (mode) {
                CLICK.mode -> CLICK
                BICKER.mode -> BICKER
                DOUBLE.mode -> DOUBLE
                CONTINUE.mode -> CONTINUE
                TIMING.mode -> TIMING
                else -> throw IllegalStateException("不支持的房间号模式${mode}")
            }
        }

    }
}
