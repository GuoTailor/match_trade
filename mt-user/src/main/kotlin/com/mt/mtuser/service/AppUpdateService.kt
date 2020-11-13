package com.mt.mtuser.service

import com.mt.mtuser.dao.AppUpdateDao
import com.mt.mtuser.entity.AppUpdate
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.from
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/8/4
 */
@Service
class AppUpdateService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var fileService: FileService

    @Autowired
    lateinit var appUpdateDao: AppUpdateDao

    @Autowired
    private lateinit var connect: DatabaseClient

    fun uploadWgt(filePart: FilePart, appUpdate: AppUpdate): Mono<AppUpdate> {
        logger.info("{}", appUpdate.forceUpdate)
        return fileService.uploadFile(filePart)
                .flatMap {
                    appUpdate.downloadUrlAndroid = it
                    if (it.substring(it.lastIndexOf('.')) == ".wgt") {
                        appUpdate.downloadUrlIos = it
                    } else {
                        appUpdate.downloadUrlIos = "未知"
                    }
                    appUpdateDao.save(appUpdate)
                }
    }

    fun appVersion(version: String, type: String): Mono<AppUpdate> {
        return appUpdateDao.getVersionByVersionCode(version)
                .map { it.downloadUrl = if (type == "ios") it.downloadUrlIos else it.downloadUrlAndroid; it }
    }

    suspend fun findAll(query: PageQuery): PageView<AppUpdate> {
        return getPage(connect.select()
                .from<AppUpdate>()
                .matching(query.where())
                .page(query.page())
                .fetch()
                .all(), connect, query)
    }

    fun deleteById(id: Int): Mono<Void> {
        return appUpdateDao.findById(id)
                .map { fileService.deleteFile(it.downloadUrlAndroid) }
                .flatMap { appUpdateDao.deleteById(id) }
    }

    fun update(appUpdate: AppUpdate) = appUpdateDao.save(appUpdate)
}