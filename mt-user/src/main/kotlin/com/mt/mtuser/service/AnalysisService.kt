package com.mt.mtuser.service

import com.mt.mtuser.dao.AnalysisDao
import com.mt.mtuser.entity.Analysis
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.from
import org.springframework.stereotype.Service
import org.springframework.web.util.HtmlUtils

/**
 * Created by gyh on 2020/7/19
 */
@Service
class AnalysisService {
    @Autowired
    lateinit var analysisDao: AnalysisDao
    @Autowired
    private lateinit var connect: DatabaseClient

    suspend fun addAnalysis(analysis: Analysis): Analysis {
        analysis.content = HtmlUtils.htmlEscape(analysis.content ?: "")
        return analysisDao.save(analysis)
    }

    suspend fun findAllAnalysis(query: PageQuery, userId : Int): PageView<Analysis> {
        val where = query.where().and("user_id").`is`(userId)
        return getPage(connect.select()
                .from<Analysis>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                .map {
                    it.content = HtmlUtils.htmlUnescape(it.content ?: "")
                    it
                },connect, query, where)
    }

}