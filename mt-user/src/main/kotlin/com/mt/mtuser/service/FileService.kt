package com.mt.mtuser.service

import com.mt.mtuser.common.Util
import com.mt.mtuser.dao.AppUpdateDao
import com.mt.mtuser.entity.AppUpdate
import com.mt.mtuser.entity.BaseUser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.util.FileCopyUtils
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.io.File
import java.lang.IllegalStateException
import java.nio.file.Files
import java.util.*

/**
 * Created by gyh on 2020/5/11.
 */
@Service
class FileService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${HEAD_FILE_PATH}")
    private lateinit var headPath: String

    @Value("\${FILE_HOST}")
    private lateinit var fileHost: String
    private final val separator = File.separatorChar
    val picture = "${separator}pictures"
    val document = "${separator}documents"

    @Value("\${user.name}")
    lateinit var userName: String

    @Autowired
    lateinit var appUpdateDao: AppUpdateDao

    fun getFile(pattern: String, id: Int, fileName: String): File {
        val year = Util.createDate("yyyy", System.currentTimeMillis())
        val month = Util.createDate("MM", System.currentTimeMillis())
        val uuid = UUID.randomUUID()
        val path = when (pattern) {
            picture -> {
                // 文件后缀
                var suffixName = ""
                if (fileName.isNotBlank()) {
                    suffixName = fileName.substring(fileName.lastIndexOf("."))
                }
                "$headPath$separator$year$separator$month$pattern$separator$id-$uuid$suffixName"
            }
            document -> "$headPath$separator$year$separator$month$pattern$separator$id.html"
            else -> error("")
        }
        val file = File(path)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        return file
    }

    fun uploadImg(filePart: FilePart): Mono<String> {
        return if (filePart.filename().isNotBlank()) {
            BaseUser.getcurrentUser()
                    .map { getFile(picture, it.id!!, filePart.filename()) }
                    .flatMap { newFile ->
                        filePart.transferTo(newFile).then(Mono.just(fileHost + newFile.absolutePath.replaceFirst(headPath, "").trim()))
                    }
        } else Mono.error(IllegalStateException("请选择一个文件"))
    }

    fun deleteFile(path: String): Boolean {
        val filePath = headPath + path.replaceFirst(fileHost, "").trim()
        return File(filePath).delete()
    }

    fun addCompanyInfo(info: String, companyId: Int): Mono<Boolean> {
        val file = getFile(document, companyId, document)
        return Mono.fromCallable {
            file.outputStream().use { fos ->
                fos.write(info.toByteArray())
                fos.flush()
                true
            }
        }
    }

    fun getCompanyInfo(companyId: Int): Mono<String> {
        val file = getFile(document, companyId, document)
        return Mono.fromCallable {
            if (file.exists()) {
                file.inputStream().use { fis ->
                    FileCopyUtils.copyToString(fis.reader())
                }
            } else ""
        }
    }

    fun deleteCompanyInfo(companyId: Int) {
        getFile(document, companyId, document).delete()
    }

    fun uploadFile(filePart: FilePart): Mono<String> {
        return if (filePart.filename().isNotBlank()) {
            BaseUser.getcurrentUser()
                    .map { getFile(document, it.id!!, filePart.filename()) }
                    .flatMap { newFile ->
                        filePart.transferTo(newFile).then(Mono.just(fileHost + newFile.absolutePath.replaceFirst(headPath, "").trim()))
                    }
        } else Mono.error(IllegalStateException("请选择一个文件"))
    }

    fun uploadWgt(filePart: FilePart, appUpdate: AppUpdate): Mono<AppUpdate> {
        return uploadFile(filePart)
                .flatMap {
                    appUpdate.downloadUrlAndroid = it
                    appUpdateDao.save(appUpdate)
                }
    }

    fun appVersion(version: String, type: String): Mono<Unit> {
        return appUpdateDao.getVersionByVersionCode(version)
                .map { it.downloadUrl = if (type == "ios") it.downloadUrlIos else it.downloadUrlAndroid }
    }

}