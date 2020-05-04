package com.mt.mtsocket.schedule

import com.mt.mtcommon.RoomRecord
import com.mt.mtcommon.toMillisOfDay
import org.quartz.Job
import org.quartz.JobDataMap
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/4/29.
 */
class MatchStartJobInfo : ScheduleJobInfo {
    override var cron: String
    override var className: Class<out Job>  // 定时任务执行类
    override var data: JobDataMap // 要传入的数据
    override var jobName: String    // 任务job的名称
    override var groupName: String = MatchTask.jobGroup  // 任务group的名称

    constructor(cron: String,
                jobName: String,
                data: JobDataMap = JobDataMap(),
                className: Class<out Job> = MatchTask::class.java,
                groupName: String = MatchTask.jobGroup) {
        this.cron = cron
        this.className = className
        this.data = data
        this.jobName = jobName
        this.groupName = groupName
    }

    constructor(roomRecord: RoomRecord, vararg data: Pair<String, *>) {
        cron = createCycle(roomRecord)
        this.jobName = roomRecord.roomId!!
        this.className = MatchTask::class.java
        this.data = JobDataMap(mapOf(*data, MatchTask.roomIdKey to roomRecord.roomId))
    }

    constructor(roomRecord: RoomRecord) {
        cron = createCycle(roomRecord)
        this.jobName = roomRecord.roomId!!
        this.className = MatchTask::class.java
        this.data = JobDataMap(mapOf(MatchTask.roomIdKey to roomRecord.roomId))
    }

    private fun createCycle(roomRecord: RoomRecord): String {
        val cycle = roomRecord.cycle!!
        // 第一次的运行时间为房间开始时间 + 报价时间 + 一个周期，也就是说第一个周期不执行
        // 如房间开始时间为12：30：00，周期为2秒，则第一次执行为12：30：02
        val startTime = millisToLocalTime((roomRecord.startTime!!.time + roomRecord.quoteTime.toMillisOfDay() + cycle.toMillisOfDay()))
        return if (roomRecord.cycle == LocalTime.MIN) {    // 如果周期是0，就只触发一次
            val cal = Calendar.getInstance()
            "%d %d %d %d %d ? %d".format(
                    startTime.second,
                    startTime.minute,
                    startTime.hour,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR)
            )
        } else {
            "${startTime.second}/${cycle.second} ${startTime.minute}/${cycle.minute} ${startTime.hour}/${cycle.hour} * * ?"
        }
    }

    private fun millisToLocalTime(millis: Long): LocalTime = LocalTime.ofSecondOfDay(millis / 1000)
}