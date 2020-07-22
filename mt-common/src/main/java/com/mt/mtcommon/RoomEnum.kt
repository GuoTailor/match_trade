package com.mt.mtcommon

/**
 * Created by gyh on 2020/4/26.
 */
enum class RoomEnum(val mode: String, val details: String) {
    CLICK("C", "点选交易"),     // 点选
    BICKER("B", "抬杠交易"),    // 抬杠
    DOUBLE("D", "两两交易"),    // 两两
    CONTINUE("E", "连续交易"),  // 连续
    TIMING("I", "定时交易");    // 定时

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
