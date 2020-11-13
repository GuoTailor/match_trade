package com.mt.mtuser.service

import com.mt.mtuser.dao.AnalysisDao
import com.mt.mtuser.dao.NotifyUserDao
import com.mt.mtuser.entity.Analysis
import com.mt.mtuser.entity.BaseUser
import com.mt.mtuser.entity.NotifyUser
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.data.r2dbc.core.from
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import org.springframework.web.util.HtmlUtils
import java.time.LocalDateTime

/**
 * Created by gyh on 2020/7/19
 */
@Service
class AnalysisService {
    @Autowired
    lateinit var analysisDao: AnalysisDao

    @Autowired
    private lateinit var connect: DatabaseClient

    @Autowired
    private lateinit var notifyUserDao: NotifyUserDao

    @Autowired
    private lateinit var roleService: RoleService

    @Autowired
    private lateinit var r2dbcService: R2dbcService

    suspend fun addAnalysis(analysis: Analysis): Analysis {
        analysis.content = HtmlUtils.htmlEscape(analysis.content ?: "")
        analysis.userId = BaseUser.getcurrentUser().awaitSingle().id
        val result = analysisDao.save(analysis)
        val adminRoleId = roleService.getRoles().find { it.name == Stockholder.ADMIN }!!.id!!
        roleService.findByRoleIdAndCompanyId(adminRoleId, analysis.companyId!!)?.let {
            notifyUserDao.save(NotifyUser(it.userId!!, result.id!!, NotifyUser.typeAnalysis))
        }
        return result
    }

    suspend fun findAnalysisById(id: Int): Analysis? {
        return analysisDao.findById(id)
    }

    suspend fun findUnreadCount(): Long {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        return notifyUserDao.countByUnread(userId, NotifyUser.typeAnalysis)
    }

    suspend fun getAllAnalysisByCompany(query: PageQuery): PageView<Analysis> {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        val where = query.where().and("company_id").`is`(companyId)
        val pageDate = getPage(connect.select()
                .from<Analysis>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                .map {
                    it.content = HtmlUtils.htmlUnescape(it.content ?: "")
                    it
                }, connect, query, where)
        pageDate.item?.forEach {
            notifyUserDao.setStatusByUserIdAndMsgId(userId, it.id!!, NotifyUser.read, NotifyUser.typeAnalysis)
        }
        return pageDate
    }

    suspend fun findAllAnalysis(query: PageQuery, userId: Int?): PageView<Analysis> {
        val id = userId ?: BaseUser.getcurrentUser().awaitSingle().id
        val where = if (id != null) query.where("ma").and("ma.user_id").`is`(id) else query.where()
        return getPage(connect.execute("select ma.*, mc.name from mt_analysis ma left join mt_company mc on ma.company_id = mc.id where $where ${query.toPageSql()}")
                .map { r, _ ->
                    val analysis = Analysis()
                    analysis.id = r.get("id", java.lang.Integer::class.java)?.toInt()
                    analysis.userId = r.get("user_id", java.lang.Integer::class.java)?.toInt()
                    analysis.companyId = r.get("company_id", java.lang.Integer::class.java)?.toInt()
                    analysis.content = HtmlUtils.htmlUnescape(r.get("content", String::class.java) ?: "")
                    analysis.type = r.get("type", String::class.java)
                    analysis.time = r.get("time", String::class.java)
                    analysis.createTime = r.get("create_time", LocalDateTime::class.java)
                    analysis.title = r.get("title", String::class.java)
                    analysis.companyName = r.get("name", String::class.java)
                    analysis
                }.all(), connect, query, "mt_analysis ma", where)
    }

    suspend fun updateAnalysis(analysis: Analysis): Int {
        analysis.content = HtmlUtils.htmlEscape(analysis.content ?: "")
        analysis.userId = BaseUser.getcurrentUser().awaitSingle().id
        return r2dbcService.dynamicUpdate(analysis)
                .matching(where("id").`is`(analysis.id!!))
                .fetch().awaitRowsUpdated()
    }

    suspend fun deleteAnalysis(id: Int) {
        analysisDao.deleteById(id)
    }

    suspend fun countByCompanyId(companyId: Int) = analysisDao.countByCompanyId(companyId)

}