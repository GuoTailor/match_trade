package com.mt.mtuser.service

import com.mt.mtuser.common.Util
import com.mt.mtuser.entity.BaseUser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.util.FileCopyUtils
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.io.File
import java.lang.IllegalStateException
import java.util.*

/**
 * Created by gyh on 2020/5/11.
 */
@Service
class FileService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${HEAD_FILE_PATH}")
    private lateinit var headPath: String
    private final val separator = File.separatorChar
    val picture = "${separator}pictures"
    val document = "${separator}documents"

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
            document -> "$headPath$separator$year$separator$month$pattern$separator$id-$uuid.html"
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
                        filePart.transferTo(newFile).then(Mono.just(newFile.absolutePath.replaceFirst(headPath, "").trim()))
                    }
        } else Mono.error(IllegalStateException("请选择一个文件"))
    }

    fun deleteFile(path: String): Boolean {
        return File("$headPath$path").delete()
    }

    fun addCompanyInfo(info: String, companyId: Int) {
        val file = getFile(document, companyId, document)

    }

    fun uploadFile(filePart: FilePart): Mono<String> {
        return BaseUser.getcurrentUser()
                .map { getFile(document, it.id!!, filePart.filename()) }
                .flatMap { newFile ->
                    filePart.transferTo(newFile).map { newFile.absolutePath.replaceFirst(headPath, "").trim() }
                }
    }

}