package com.mt.mtuser.service.room

import com.mt.mtuser.common.Util
import com.mt.mtuser.entity.room.*

/**
 * Created by gyh on 2020/3/24.
 */
object RoomExtend {

    inline fun <reified T : Any> getFlag(): String {
        return when (T::class) {
            ClickMatch::class -> RoomEnum.CLICK.flag
            DoubleMatch::class -> RoomEnum.DOUBLE.flag
            TimelyMatch::class -> RoomEnum.TIMELY.flag
            TimingMatch::class -> RoomEnum.TIMING.flag
            else -> throw IllegalStateException("不支持的撮合模式${T::class.simpleName}")
        }
    }

}

enum class RoomEnum(val flag: String) {
    CLICK("C"),
    DOUBLE("D"),
    TIMELY("E"),
    TIMING("I")
}

inline fun <reified T : BaseRoom<T>> BaseRoom<T>.createRoomNumber(): T {
    this.roomNumber = RoomExtend.getFlag<T>() + Util.getRandomInt(6)
    return this as T
}