package com.mt.mtuser.dao

import com.mt.mtuser.entity.Notify
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

/**
 * Created by gyh on 2020/5/26.
 */
interface NotifyDao : CoroutineCrudRepository<Notify, Int> {

    @Query("select count(1) from mt_notify where create_time > :createTime and send_type = 'mass'")
    suspend fun countByCreateTime(createTime: Date): Long

    @Query("select * from mt_notify " +
            " where ((create_time > :createTime and send_type = 'mass')" +
            " or id in (:idList))" +
            " and msg_type = 'announce'" +
            " order by create_time desc limit 1")
    suspend fun findAnnounce(createTime: Date, idList: Iterable<Int>?): Notify?
}