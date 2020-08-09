package com.mt.mtuser.dao

import com.mt.mtuser.entity.NotifyUser
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

/**
 * Created by gyh on 2020/5/26.
 */
interface NotifyUserDao : CoroutineCrudRepository<NotifyUser, Int> {

    @Query("select count(1) from mt_notify_user where user_id = :userID and status = 'unread' and msg_type = :msgType")
    suspend fun countByUnread(userId: Int, msgType: String): Long

    @Query("select * from mt_notify_user where user_id = :userId and msg_type = :msgType")
    fun findAllByUserId(userId: Int, msgType: String): Flow<NotifyUser>

    @Query("select msg_id from mt_notify_user where user_id = :userId and status = 'unread' and msg_type = '1'")
    fun findAllUnreadByUserId(userId: Int): Flow<Int>

    @Modifying
    @Query("update mt_notify_user set status = :status where id in (:id)")
    suspend fun setStatusById(id: Iterable<Int>, status: String): Int

    @Query("update mt_notify_user set status = :status where user_id = :userId and msg_id = :msgId and msg_type = '1'")
    suspend fun setStatusByUserIdAndMsgId(userId: Int, msgId: Int, status: String): Int
}