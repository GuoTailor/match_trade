package com.mt.mtuser.service.room

import com.mt.mtuser.entity.room.ClickMatch
import com.mt.mtuser.entity.room.DoubleMatch
import com.mt.mtuser.entity.room.TimelyMatch
import com.mt.mtuser.entity.room.TimingMatch

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

    fun getRoomDome(mode: String): Collection<String> {
        return when (mode) {
            "1" -> listOf(RoomEnum.CLICK.flag)
            "2" -> listOf(RoomEnum.CLICK.flag, RoomEnum.TIMING.flag)
            "3" -> listOf(RoomEnum.CLICK.flag, RoomEnum.TIMING.flag, RoomEnum.DOUBLE.flag)
            "4" -> listOf(RoomEnum.CLICK.flag, RoomEnum.TIMING.flag, RoomEnum.DOUBLE.flag, RoomEnum.TIMELY.flag)
            else -> throw java.lang.IllegalStateException("错误：${mode}和已有竞价模式{1：点选、2： 点选+定时、3：及时 +点选+两两撮合、4：全部}不匹配")
        }
    }

}

enum class RoomEnum(val flag: String) {
    CLICK("C"),
    DOUBLE("D"),
    TIMELY("E"),
    TIMING("I"),
}

