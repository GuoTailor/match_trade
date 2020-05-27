package com.mt.mtuser.service

import com.mt.mtuser.dao.NotifyDao
import com.mt.mtuser.dao.NotifyUserDao
import com.mt.mtuser.dao.UserDao
import com.mt.mtuser.entity.BaseUser
import com.mt.mtuser.entity.Notify
import com.mt.mtuser.entity.NotifyUser
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.data.r2dbc.core.from
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import org.springframework.web.util.HtmlUtils
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by gyh on 2020/5/26.
 */
@Service
class NotifyService {
    @Autowired
    private lateinit var notifyDao: NotifyDao

    @Autowired
    private lateinit var notifyUserDao: NotifyUserDao

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var connect: DatabaseClient

    suspend fun fundReadTime(userId: Int): Date {
        return connect.execute("select read_time from mt_user where id = :userId")
                .bind("userId", userId)
                .map { row, _ -> row.get("read_time", Date::class.java) }
                .awaitOne()!!
    }

    suspend fun getUnreadCount(): Long {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        val lastTime = fundReadTime(userId)
        return notifyDao.countByCreateTime(lastTime) + notifyUserDao.countByUnread(userId)
    }

    suspend fun getAllMsg(query: PageQuery): PageView<Notify> {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        val notifyUserList = notifyUserDao.findAllByUserId(userId).toList()
        val msgIdList = notifyUserList.map { it.msgId }
        val idList = notifyUserList.map { it.id!! }
        val lastReadTime = fundReadTime(userId)
        var where = query.where()
        where = if (msgIdList.isNotEmpty()) {
            where.and(where("id").`in`(msgIdList)
                    .or(where("send_type").`is`("mass")))
        } else {
            where.and(where("send_type").`is`("mass"))
        }
        val pageDate = getPage(connect.select()
                .from<Notify>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                .map { notify ->
                    HtmlUtils.htmlUnescape(notify.content ?: notify.title!!)
                    val notifyUser = notifyUserList.find { it.msgId == notify.id}
                    if ((notifyUser != null && notifyUser.status == NotifyUser.unread) || notify.createTime!! > lastReadTime) {
                        notify.readStatus = NotifyUser.unread
                    } else notify.readStatus = NotifyUser.read
                    notify
                }, connect, query, where)
        userDao.setReadTimeByUserId(userId, Date())
        if (idList.isNotEmpty()) {
            notifyUserDao.setStatusById(idList, NotifyUser.read)
        }
        return pageDate
    }

    suspend fun addMsg(msg: Notify) {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        msg.sendId = userId
        msg.content = HtmlUtils.htmlEscape(msg.content ?: "")
        msg.status = msg.status ?: Notify.statusProgress
        val notify = notifyDao.save(msg)
        if (msg.sendType == "assign") {
            val idList = msg.idList ?: error("请指定用户")
            val jobList = ArrayList<Deferred<*>>(idList.size)
            coroutineScope {
                idList.forEach {
                    val notifyUser = NotifyUser(it, notify.id!!)
                    jobList.add(async { notifyUserDao.save(notifyUser) })
                }
            }
            jobList.forEach { it.await() }
        }
    }
}