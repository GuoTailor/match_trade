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
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.data.r2dbc.core.from
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import org.springframework.web.util.HtmlUtils
import java.time.LocalDateTime
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

    @Autowired
    lateinit var r2dbcService: R2dbcService

    suspend fun fundReadTime(userId: Int): LocalDateTime {
        return connect.execute("select read_time from mt_user where id = :userId")
                .bind("userId", userId)
                .map { row, _ -> row.get("read_time", LocalDateTime::class.java) }
                .awaitOne()!!
    }

    suspend fun getUnreadCount(): Long {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        val lastTime = fundReadTime(userId)
        return notifyDao.countByCreateTime(lastTime) + notifyUserDao.countByUnread(userId, NotifyUser.typeMsg)
    }

    /**
     * 获取了就标记为已读了，通过标记读取时间来看新消息是否为已读
     * 假如消息的发布时间大于读取时间就未读，假如消息读取时间大于消息发布时间就已读
     */
    suspend fun getAllMsg(query: PageQuery): PageView<Notify> {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        val notifyUserList = notifyUserDao.findAllByUserId(userId, NotifyUser.typeMsg).toList()
        val msgIdList = notifyUserList.map { it.msgId }
        val idList = notifyUserList.map { it.id!! }
        val lastReadTime = fundReadTime(userId)
        var where = query.where()
        where = if (msgIdList.isNotEmpty()) {
            where.and(where("id").`in`(msgIdList).or(where("send_type").`is`("mass")))
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
                    notify.content = HtmlUtils.htmlUnescape(notify.content ?: "")
                    val notifyUser = notifyUserList.find { it.msgId == notify.id }
                    if ((notifyUser != null && notifyUser.status == NotifyUser.unread) || notify.createTime!! > lastReadTime) {
                        notify.readStatus = NotifyUser.unread
                    } else notify.readStatus = NotifyUser.read
                    notify
                }, connect, query, where)
        userDao.setReadTimeByUserId(userId, LocalDateTime.now())
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
        if (msg.sendType == Notify.sendTypeAssign) {
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

    suspend fun getAnnounce(): Notify? {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        val announceList = notifyUserDao.findAllUnreadByUserId(userId).toList()
        val lastReadTime = fundReadTime(userId)
        val sql = "select * from mt_notify " +
                " where ((create_time > :createTime and send_type = 'mass')" +
                if (announceList.isEmpty()) ") " else " or id in (:idList))" +
                " and msg_type = 'announce' order by create_time desc limit 2"
        val notifyList = connect.execute(sql)
                .bind("createTime", lastReadTime)
                .run { if (announceList.isNotEmpty()) this.bind("idList", announceList) else this }
                .`as`(Notify::class.java)
                .fetch()
                .all()
                .map {
                    it.content = HtmlUtils.htmlUnescape(it.content ?: "")
                    it
                }.collectList()
                .awaitSingle()
        // 当公告只有一条时才能设置为已读，
        // 当有多条时不能设置最新的读取时间来标记为已读，因为他实际只读了一条
        if (notifyList.size == 1) {
            userDao.setReadTimeByUserId(userId, LocalDateTime.now())
            if (announceList.contains(notifyList[0].id)) {
                notifyUserDao.setStatusByUserIdAndMsgId(userId, notifyList[0].id!!, NotifyUser.read, NotifyUser.typeMsg)
            }
        }
        return if (notifyList.isEmpty()) null else notifyList[0]
    }

    suspend fun deleteMsg(msgId: Int) = notifyDao.deleteById(msgId)

    suspend fun updateMsg(msg: Notify): Int {
        return r2dbcService.dynamicUpdate(msg)
                .matching(where("id").`is`(msg.id!!))
                .fetch().awaitRowsUpdated()

    }
}